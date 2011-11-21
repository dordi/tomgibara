package com.tomgibara.crinch.record.compact;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.FibonacciCoding;
import com.tomgibara.crinch.record.EmptyRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;

public class PositionProducer implements RecordProducer<EmptyRecord> {

	private RecordStats recStats;
	private PositionStats posStats;
	private File file;
	private byte[] data;
	private long oversizedStart;
	private long oversizedFinish;
	
	@Override
	public void prepare(ProcessContext context) {
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalArgumentException("no record stats");
		posStats = new PositionStats(context);
		posStats.read();

		file = new File(context.getOutputDir(), context.getDataName() + ".positions." + posStats.definition.getId());
		int size = (int) file.length();
		byte[] data = new byte[size];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			new DataInputStream(in).readFully(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				context.log("Failed to close file", e);
			}
		}
		this.data = data;
		
		oversizedStart = posStats.fixedBitSize * recStats.getRecordCount();
		oversizedFinish = posStats.bitsWritten;
	}
	
	@Override
	public RecordSequence<EmptyRecord> open() {
		return new PositionSequence();
	}

	@Override
	public void complete() {
	}

	private class PositionSequence implements RecordSequence<EmptyRecord> {

		private final ByteArrayBitReader reader;
		private final CodedReader coded;
		private final long recordCount = recStats.getRecordCount();
		private final int fixedBitSize = posStats.fixedBitSize;
		private final long invalid = 1 << (fixedBitSize - 1);
		private final long negativeBoundary = 1 << (fixedBitSize - 1);
		private final long negativeMask = -1L << fixedBitSize;
		private final int maxDepth = 64 - Long.numberOfLeadingZeros(recordCount);
		private final long[] stack = new long[maxDepth * 4];
		//frames in stack are:
		//  bottomOrdinal
		//  bottomPosition
		//  topOrdinal
		//  topPosition
		
		private long count;
		private int depth;
		
		PositionSequence() {
			reader = new ByteArrayBitReader(data);
			coded = new CodedReader(reader, FibonacciCoding.extended);
			stack[0] = 0L;
			stack[1] = posStats.bottomPosition;
			stack[2] = recordCount - 1L;
			stack[3] = posStats.topPosition;
			depth = 0;
		}
		
		@Override
		public boolean hasNext() {
			return count < recordCount;
		}

		@Override
		public EmptyRecord next() {
			if (!hasNext()) throw new NoSuchElementException();
			
			// every ordinal appears as a 'bottom' in some interval except the topmost
			if (count == recordCount - 1L) {
				return new EmptyRecord(count++, posStats.topPosition);
			}
			
			//walk back 
			while (depth > 0 && stack[depth * 4 + 2] == stack[(depth-1) * 4 + 2]) {
				depth--;
			}
			// move exposed half from top to bottom
			if (depth > 0) {
				stack[depth * 4 + 0] = stack[depth * 4 + 2];
				stack[depth * 4 + 1] = stack[depth * 4 + 3];
				stack[depth * 4 + 2] = stack[(depth-1) * 4 + 2];
				stack[depth * 4 + 3] = stack[(depth-1) * 4 + 3];
			}
			// walk forward with bottom halves
			while (true) {
				long bottomOrdinal = stack[depth * 4 + 0];
				long bottomPosition = stack[depth * 4 + 1];
				long topOrdinal = stack[depth * 4 + 2];
				long topPosition = stack[depth * 4 + 3];

				long ord = (bottomOrdinal + topOrdinal) / 2;
				if (ord == bottomOrdinal || ord == topOrdinal) {
					count++;
					return new EmptyRecord(bottomOrdinal, bottomPosition);
				}

				reader.setPosition(ord * fixedBitSize);
				long err = reader.readLong(fixedBitSize);
				long pos;
				if (err == invalid) {
					pos = findPosition(ord);
				} else {
					// fill in truncated negative bits
					if (err >= negativeBoundary) err |= negativeMask;
					long est = (topPosition + bottomPosition) / 2;
					pos = est + err;
				}

				depth ++;
				stack[depth * 4 + 0] = bottomOrdinal;
				stack[depth * 4 + 1] = bottomPosition;
				stack[depth * 4 + 2] = ord;
				stack[depth * 4 + 3] = pos;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
		}

		//TODO switch to using binary search
		private long findPosition(long ordinal) {
			reader.setPosition(oversizedStart);
			while (reader.getPosition() < oversizedFinish) {
				long ord = coded.readPositiveLong() / 2 - 1;
				long pos = (coded.readPositiveLong() - 1L) / 2;
				if (ord == ordinal) return pos;
			}
			return -1L;
		}
		
	}

}
