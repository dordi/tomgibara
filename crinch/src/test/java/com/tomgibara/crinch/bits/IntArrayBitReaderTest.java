package com.tomgibara.crinch.bits;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class IntArrayBitReaderTest extends AbstractBitReaderTest {

	@Override
	BitReader readerFor(BitVector vector) {
		vector = vector.mutableCopy();
		vector.reverse();
		byte[] bytes = vector.toByteArray();
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		final IntBuffer intBuffer = byteBuffer.asIntBuffer();
		int[] ints = new int[ intBuffer.capacity() ];
		intBuffer.get(ints);
		return new IntArrayBitReader(ints);
	}
	
}
