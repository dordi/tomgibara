package com.tomgibara.crinch.bits;

public class BitVectorBitReaderTest extends AbstractBitReaderTest {

	BitReader readerFor(BitVector vector) {
		BitVector copy = vector.mutableCopy();
		copy.reverse();
		return copy.openReader();
	}
	
}
