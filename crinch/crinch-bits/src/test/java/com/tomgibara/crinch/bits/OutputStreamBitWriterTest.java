package com.tomgibara.crinch.bits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class OutputStreamBitWriterTest extends AbstractBitWriterTest {

	@Override
	BitWriter newBitWriter(long size) {
		return new Writer(new ByteArrayOutputStream((int) size));
	}

	@Override
	BitReader bitReaderFor(BitWriter writer) {
		Writer w = (Writer) writer;
		byte[] bytes = w.out.toByteArray();
		return new InputStreamBitReader(new ByteArrayInputStream(bytes));
	}

	private static class Writer extends OutputStreamBitWriter {
		
		final ByteArrayOutputStream out;
		
		Writer(ByteArrayOutputStream out) {
			super(out);
			this.out = out;
		}
		
	}
	
}
