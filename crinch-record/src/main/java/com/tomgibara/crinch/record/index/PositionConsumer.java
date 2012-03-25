/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record.index;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.NullBitWriter;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.FibonacciCoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.process.ProcessContext;

public class PositionConsumer implements RecordConsumer<LinearRecord> {

	// statics
	
	private static void writeEntry(CodedWriter coded, int ordinal, long position) {
//		//TODO we don't guard against overflow here
		coded.writePositiveInt(2 * ordinal);
		coded.writePositiveLong(2 * position + 1);
	}

	
	// prepared state
	private ProcessContext context;
	private PositionStats posStats;
	private RecordStats recStats;
	private File file;
	// pass state
	private long[] positions;

	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		posStats = new PositionStats(context);
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalStateException("no stats");
		if (recStats.getRecordCount() > Integer.MAX_VALUE) throw new UnsupportedOperationException("long record counts not currently supported.");
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		file = context.file("positions", false, posStats.definition);
		if (context.isClean()) file.delete();
	}

	@Override
	public int getRequiredPasses() {
		return file.isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		context.setPassName("Building position index");
		//TODO support larger counts
		int count = (int) recStats.getRecordCount();
		positions = new long[count];
	}

	@Override
	public void consume(LinearRecord record) {
		long position = record.getPosition();
		int ordinal = (int) record.getOrdinal();
		//TODO could deal with this by maintaining own count of records and decrementing
		if (position < 0L) throw new IllegalArgumentException("record without position");
		if (ordinal < 0) throw new IllegalArgumentException("record without ordinal");
		positions[ordinal] = position;
	}

	@Override
	public void endPass() {
		posStats.bottomPosition = positions[0];
		posStats.topPosition = positions[positions.length - 1];
		calcOffsets(0, positions.length - 1);
		positions[positions.length - 1] = 0;
		chooseFixedBitSize();
		posStats.write();
		writeFile();
	}

	@Override
	public void complete() {
		positions = null;
	}

	@Override
	public void quit() {
		positions = null;
	}

	private void calcOffsets(int bottom, int top) {
		if (bottom == top || bottom + 1 == top) return;
		int index = (bottom + top) / 2;
		long pos = positions[index];
		long est = (positions[bottom] + positions[top]) / 2;
		calcOffsets(bottom, index);
		calcOffsets(index, top);
		positions[index] = pos - est;
	}

	private void chooseFixedBitSize() {
		// count errors that fit into fixed sizes
		long[] errCounts = new long[65];
		for (int i = 0; i < positions.length; i++) {
			long pos = positions[i];
			long err = Math.abs(pos);
			int bits = 65 - Long.numberOfLeadingZeros(err); // 64+1 is because we need sign
			errCounts[bits]++;
		}
		
		// measure overflow record
		long[] sizes = new OverSizedMeasurer().sizeEntries();
		
		// identify the best fixed size
		long bestSize = Long.MAX_VALUE;
		int bestFixed = 0;
		// TODO could implement more efficiently backwards (to accumulate frees)
		for (int fixed = 0; fixed < 65; fixed++) {
			long size = fixed * positions.length;
			for (int free = fixed + 1; free < 65; free++) {
				size += sizes[free];
			}
			if (size < bestSize) {
				bestSize = size;
				bestFixed = fixed;
			}
		}
		
		// record the best fixed size
		posStats.fixedBitSize = bestFixed;
		posStats.bitsWritten = bestSize;
		context.getLogger().log("Fixed bit size: " + bestFixed);
	}
	
	private void writeFile() {
		CodedStreams.writeToFile(new CodedStreams.WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				final BitWriter w = writer.getWriter();
				final long[] positions = PositionConsumer.this.positions;
				final int fixedBitSize = PositionConsumer.this.posStats.fixedBitSize;
				final long invalid = 1 << (fixedBitSize - 1);
				for (int i = 0; i < positions.length; i++) {
					long err = positions[i];
					int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
					long value = bits <= fixedBitSize ? err : invalid;
					w.write(value, fixedBitSize);
				}
				long fixedSize = w.getPosition();
				new OversizedWriter(w).writeEntries();
				long totalSize = w.getPosition();
				long variableSize = totalSize - fixedSize;
				context.getLogger().log("Non-fixed percentage: " + String.format("%.2f", variableSize / (double) totalSize * 100.0));
			}
		}, posStats.coding, file);
	}
	
	private abstract class OversizedBase {
		
		private final long[] positions = PositionConsumer.this.positions;
		
		void writeEntries() {
			writeEntry(0, posStats.bottomPosition, 0L);
			writeEntries(0, posStats.bottomPosition, positions.length - 1, posStats.topPosition);
			writeEntry(0, posStats.topPosition, 0L);
		}

		private void writeEntries(int bottomOrdinal, long bottomPosition, int topOrdinal, long topPosition) {
			if (bottomOrdinal == topOrdinal || bottomOrdinal + 1 == topOrdinal) return;
			int index = (bottomOrdinal + topOrdinal) / 2;
			long err = positions[index];
			long est = (bottomPosition + topPosition) / 2;
			long pos = est + err;
			writeEntries(bottomOrdinal, bottomPosition, index, pos);
			writeEntry(index, pos, err);
			writeEntries(index, pos, topOrdinal, topPosition);
		}

		abstract void writeEntry(int ordinal, long position, long err);
		
	}

	private class OversizedWriter extends OversizedBase {
		
		private final CodedWriter coded;
		private final int fixedBitSize = posStats.fixedBitSize;

		OversizedWriter(BitWriter writer) {
			this.coded = new CodedWriter(writer, FibonacciCoding.extended);
		}
		
		void writeEntry(int ordinal, long position, long err) {
			int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
			if (bits > fixedBitSize) PositionConsumer.writeEntry(coded, ordinal, position);
		}
		
	}
	
	private class OverSizedMeasurer extends OversizedBase {
		
		// bit writer at posn n records storage for all entries that need at least n bits to store 'fixed'
		final CodedWriter[] coded = new CodedWriter[65];
		
		public OverSizedMeasurer() {
			ExtendedCoding coding = FibonacciCoding.extended;
			for (int i = 0; i < coded.length; i++) {
				coded[i] = new CodedWriter(new NullBitWriter(), coding);
			}
		}

		long[] sizeEntries() {
			writeEntries();
			long[] sizes = new long[coded.length];
			for (int i = 0; i < sizes.length; i++) {
				sizes[i] = coded[i].getWriter().getPosition();
			}
			return sizes;
		}
		
		@Override
		void writeEntry(int ordinal, long position, long err) {
			int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
			PositionConsumer.writeEntry(coded[bits], ordinal, position);
			
		}
		
	}
	
}
