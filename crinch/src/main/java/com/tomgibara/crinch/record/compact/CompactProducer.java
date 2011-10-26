package com.tomgibara.crinch.record.compact;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.InputStreamBitReader;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;

public class CompactProducer implements RecordProducer<LinearRecord> {

	private final File file;
	
	public CompactProducer(File file) {
		if (file == null) throw new IllegalArgumentException("null file");
		this.file = file;
	}
	
	@Override
	public RecordSequence<LinearRecord> open(ProcessContext context) {
		Sequence seq = new Sequence();
		context.setProgressScale(seq.recordCount);
		return seq;
	}

	private class Sequence implements RecordSequence<LinearRecord> {
		
		final InputStream in;
		final BitReader reader;
		final long recordCount;
		final RecordDecompactor decompactor;
		
		long recordsRead = 0;
		
		Sequence() {
			try {
				in = new BufferedInputStream(new FileInputStream(file), 1024);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			reader = new InputStreamBitReader(in);
			recordCount = EliasOmegaEncoding.decodeLong(reader) - 1;
			System.out.println("Record count: " + recordCount);
			decompactor = RecordDecompactor.read(reader);
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
			recordsRead++;
			return decompactor.decompact(reader);
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
