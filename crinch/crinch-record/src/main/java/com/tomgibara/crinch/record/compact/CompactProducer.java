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
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordDefinition;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;

public class CompactProducer implements RecordProducer<LinearRecord> {

	private final boolean ordered;
	
	public CompactProducer(boolean ordered) {
		this.ordered = ordered;
	}
	
	@Override
	public RecordSequence<LinearRecord> open(ProcessContext context) {
		return new Sequence(context);
	}

	private class Sequence implements RecordSequence<LinearRecord> {
		
		final InputStream in;
		final BitReader reader;
		final CodedReader coded;
		final long recordCount;
		final RecordDecompactor decompactor;
		
		long recordsRead = 0;
		
		Sequence(ProcessContext context) {
			RecordStats stats = context.getRecordStats();
			if (stats == null) throw new IllegalStateException("no statistics available");
			recordCount = stats.getRecordCount();
			decompactor = new RecordDecompactor(stats);

			RecordDefinition def = new RecordDefinition(true, true, context.getColumnTypes(), ordered ? context.getColumnOrders() : null);
			File file = new File(context.getOutputDir(), context.getDataName() + ".compact." + def.getId());
			try {
				in = new BufferedInputStream(new FileInputStream(file), 1024);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			reader = new InputStreamBitReader(in);
			coded = new CodedReader(reader, context.getCoding());
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
			return decompactor.decompact(coded, recordsRead++);
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
	
}
