package com.tomgibara.crinch.bits;

public class ByteArrayBitReader extends ByteBasedBitReader {

	private final byte[] bytes;
	private int index;
	
	public ByteArrayBitReader(byte[] bytes) {
		if (bytes == null) throw new IllegalArgumentException("null bytes");
		this.bytes = bytes;
		index = 0;
	}
	
	@Override
	protected int readByte() throws BitStreamException {
		return index == bytes.length ? -1 : bytes[index++] & 0xff;
	}
	
	@Override
	protected long skipBytes(long count) throws BitStreamException {
		long limit = bytes.length - index;
		if (count >= limit) {
			index = bytes.length;
			return limit;
		}
		index += count;
		return count;
	}
	
	@Override
	protected long seekByte(long index) throws BitStreamException {
		if (index >= bytes.length) {
			this.index = bytes.length;
			return bytes.length;
		} else {
			this.index = (int) index;
			return index;
		}
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
}
