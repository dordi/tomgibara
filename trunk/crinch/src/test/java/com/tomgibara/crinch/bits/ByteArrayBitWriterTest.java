package com.tomgibara.crinch.bits;

public class ByteArrayBitWriterTest extends AbstractByteBasedBitWriterTest {

	@Override
	BitWriter newBitWriter(long size) {
		return new ByteArrayBitWriter(new byte[(int) ((size + 7) / 8)]);
	}
	
	@Override
	BitReader bitReaderFor(BitWriter writer) {
		ByteArrayBitWriter bw = (ByteArrayBitWriter) writer;
		return new ByteArrayBitReader(bw.getBytes());
	}
	
}
