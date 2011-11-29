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
import com.tomgibara.crinch.record.def.RecordDefinition;

public class CompactProducer implements RecordProducer<LinearRecord> {

	private final boolean ordered;
	
	private ExtendedCoding coding;
	private long recordCount;
	private RecordDecompactor decompactor;
	private File file;
	
	public CompactProducer(boolean ordered) {
		this.ordered = ordered;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		RecordDefinition def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		if (!ordered) def = def.getBasisOrSelf();
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
