package com.tomgibara.crinch.bits;

import java.io.ByteArrayInputStream;

public class InputStreamBitReaderTest extends AbstractBitReaderTest {

	@Override
	BitReader readerFor(BitVector vector) {
		vector = vector.mutableCopy();
		vector.reverse();
		return new InputStreamBitReader(new ByteArrayInputStream(vector.toByteArray()));
	}
	
}
