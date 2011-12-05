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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.InputStreamBitReader;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;

public class CompactProducer implements RecordProducer<LinearRecord> {

	private final SubRecordDef subRecDef;
	
	private ExtendedCoding coding;
	private long recordCount;
	private RecordDecompactor decompactor;
	private File file;
	
	public CompactProducer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		if (subRecDef != null) def = def.asSubRecord(subRecDef);
		RecordStats stats = context.getRecordStats();
		if (stats == null) throw new IllegalStateException("no statistics available");
		stats = stats.adaptFor(def);
		coding = context.getCoding();
		recordCount = stats.getRecordCount();
		decompactor = new RecordDecompactor(stats, 0);
		file = context.file("compact", false, def);
	}
	
	@Override
	public RecordSequence<LinearRecord> open() {
		return new Sequence();
	}

	private class Sequence implements RecordSequence<LinearRecord> {
		
		// local copies for possible performance gain
		final long recordCount = CompactProducer.this.recordCount;
		final RecordDecompactor decompactor = CompactProducer.this.decompactor;
		
		final InputStream in;
		final BitReader reader;
		final CodedReader coded;
		long recordsRead = 0;
		
		Sequence() {
			try {
				in = new BufferedInputStream(new FileInputStream(file), 1024);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			reader = new InputStreamBitReader(in);
			coded = new CodedReader(reader, coding);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean hasNext() {
			return recordsRead < recordCount;
		}
		
		@Override
		public LinearRecord next() {
			if (recordsRead == recordCount) throw new NoSuchElementException();
			return decompactor.decompact(coded, recordsRead++, reader.getPosition());
		}
		
		@Override
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@Override
	public void complete() {
		coding = null;
		recordCount = 0L;
		decompactor = null;
		file = null;
	}
	
}
