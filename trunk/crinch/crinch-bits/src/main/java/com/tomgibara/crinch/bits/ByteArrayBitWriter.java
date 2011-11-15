package com.tomgibara.crinch.bits;

import java.util.Arrays;

public class ByteArrayBitWriter extends ByteBasedBitWriter {

	private final byte[] bytes;
	private int index;

	public ByteArrayBitWriter(byte[] bytes) {
		if (bytes == null) throw new IllegalArgumentException("null bytes");
		this.bytes = bytes;
		index = 0;
	}
	
	@Override
	protected void writeByte(int value) throws BitStreamException {
		if (index == bytes.length) throw new EndOfBitStreamException();
		bytes[index++] = (byte) value;
	}

	@Override
	protected long padBytes(boolean padWithOnes, long count) throws BitStreamException {
		count = Math.min(count, bytes.length - index);
		byte value = padWithOnes ? (byte) 255 : (byte) 0;
		int newIndex = index + (int) count;
		Arrays.fill(bytes, index, newIndex, value);
		index = newIndex;
		return count;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
}
