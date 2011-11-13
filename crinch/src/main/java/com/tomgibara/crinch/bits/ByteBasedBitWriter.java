package com.tomgibara.crinch.bits;


//TODO optimize write
public abstract class ByteBasedBitWriter extends AbstractBitWriter {

	private int buffer = 0;
	private int count = 0;
	private long position = 0;
	
	// methods for implementation
	
	protected abstract void writeByte(int value) throws BitStreamException;
	
	protected abstract long padBytes(boolean padWithOnes, long count) throws BitStreamException;
	
	// bit writer methods
	
	@Override
	public long writeBooleans(boolean value, long count) {
		if (count < 0L) throw new IllegalArgumentException("negative count");
		int boundary = bitsToBoundary(BitBoundary.BYTE);
		int bits = value ? -1 : 0;
		if (count <= boundary) return write(bits, (int) count);
		
		long c = write(bits, boundary);
		long d = padBytes(value, (count - c) >> 3) << 3;
		position += d;
		c += d;
		c += write(bits, (int) (count - c));

		return c;
	}
	
	@Override
	public int writeBit(int bit) {
		buffer = (buffer << 1) | (bit & 1);
		if (++count == 8) {
			writeByte(buffer);
			count = 0;
		}
		position++;
		return 1;
	}
	
	@Override
	public void flush() {
		if (count == 8) {
			writeByte(buffer);
			count = 0;
		}
	}

	@Override
	public long getPosition() {
		return position;
	}

	
}
