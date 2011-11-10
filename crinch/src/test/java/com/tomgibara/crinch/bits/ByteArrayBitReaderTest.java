package com.tomgibara.crinch.bits;


public class ByteArrayBitReaderTest extends AbstractByteBasedBitReaderTest {

	@Override
	ByteArrayBitReader readerFor(BitVector vector) {
		vector = vector.mutableCopy();
		vector.reverse();
		return new ByteArrayBitReader(vector.toByteArray());
	}
	
}
