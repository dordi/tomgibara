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
package com.tomgibara.crinch.record.compact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.hashing.Hash;
import com.tomgibara.crinch.hashing.LongHash;
import com.tomgibara.crinch.hashing.LongSeededHashSource;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.Extended;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class SortConsumer implements RecordConsumer<LinearRecord> {

	private static Comparator<LinearRecord> sHashComparator = new Comparator<LinearRecord>() {
		@Override
		public int compare(LinearRecord a, LinearRecord b) {
			long ha = (Long) ((Extended) a).getExtension();
			long hb = (Long) ((Extended) b).getExtension();
			if (ha == hb) return ((Comparable) a).compareTo(b);
			return ha < hb ? -1 : 1;
		}
	};
	
	private final SubRecordDef subRecDef;
	
	private ProcessContext context;
	private CompactStats stats;
	private DynamicRecordFactory factory;
	private ClassConfig config;
	private Comparator<LinearRecord> comparator;
	private Hash<LinearRecord> hash;
	private PriorityQueue<LinearRecord> queue; 
	private File file;
	private OutputStream out;
	private BitWriter writer;
	private CodedWriter coded;
	private long bitsWritten;
	
	public SortConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		stats = new CompactStats("compact", context, subRecDef);
		factory = DynamicRecordFactory.getInstance(stats.definition);

		Long seed = stats.definition.getLongProperty("shuffle.hashSeed");
		if (seed != null) {
			comparator = sHashComparator;
			config = new ClassConfig(false, false, true);
			hash = new LongHash<LinearRecord>(new LongSeededHashSource<LinearRecord>(factory.getHashSource(config), seed));
		} else {
			comparator = null;
			config = new ClassConfig(false, false, false);
			hash = null;
		}
		file = file();
		if (context.isClean()) file.delete();
		
	}
	
	@Override
	public int getRequiredPasses() {
		return file.isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		if (hash == null) {
			context.setPassName("Sorting records");
		} else {
			context.setPassName("Shuffling records");
		}
		//TODO splitting
		//TODO need to size
		queue = new PriorityQueue<LinearRecord>(10000, comparator);
	}

	@Override
	public void consume(LinearRecord record) {
		LinearRecord r = factory.newRecord(config, record);
		if (hash != null) {
			long h = hash.hashAsLong(r);
			((Extended) r).setExtension(h);
		}
		queue.add(r);
	}

	@Override
	//TODO merging
	public void endPass() {
		open();
		try {
			RecordCompactor compactor = new RecordCompactor(context, stats.definition, 0);
			while (!queue.isEmpty()) {
				bitsWritten += compactor.compact(coded, queue.poll());
			}
		} finally {
			queue = null;
			factory = null;
			close();
		}
	}

	@Override
	public void complete() {
		cleanup();
	}

	@Override
	public void quit() {
		cleanup();
	}

	private File file() {
		return context.file("compact", false, stats.definition);
	}

	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(file), 1024);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer = new OutputStreamBitWriter(out);
		coded = new CodedWriter(writer, context.getCoding());
		bitsWritten = 0L;
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.getLogger().log(Level.ERROR, "Failed to flush writer", e);
			} finally {
				writer = null;
				coded = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				context.getLogger().log(Level.WARN, "Failed to close file", e);
			} finally {
				out = null;
			}
		}
		stats.bitsWritten = bitsWritten;
		stats.write();
	}

	private void cleanup() {
		if (context != null) {
			context = null;
			file = null;
		}
	}
	
}
