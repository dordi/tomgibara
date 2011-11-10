package com.tomgibara.crinch.bits;


public class ByteArrayBitReaderTest extends AbstractBitReaderTest {

	@Override
	BitReader readerFor(BitVector vector) {
		vector = vector.mutableCopy();
		vector.reverse();
		return new ByteArrayBitReader(vector.toByteArray());
	}
	
}
