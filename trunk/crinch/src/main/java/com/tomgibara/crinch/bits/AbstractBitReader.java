/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

import java.math.BigInteger;


public abstract class AbstractBitReader implements BitReader {

    @Override
    public int readBit() {
        return read(1);
    }
    
    @Override
    public boolean readBoolean() {
    	return readBit() == 1;
    }
    
    @Override
    public int read(int count) {
    	if (count < 0) throw new IllegalArgumentException("negative count");
    	if (count > 32) throw new IllegalArgumentException("count too great");
        if (count == 0) return 0;
        int acc = readBit();
        while (--count > 0) {
            acc = acc << 1 | readBit();
        }
        return acc;
    }

    @Override
    public long readLong(int count) {
    	if (count < 0) throw new IllegalArgumentException("negative count");
    	if (count > 64) throw new IllegalArgumentException("count too great");
        if (count == 0) return 0;
        if (count <= 32) return read(count) & 0x00000000ffffffffL;
        return (((long)read(count - 32)) << 32) | (read(32) & 0x00000000ffffffffL);
    }

    //TODO knowing alignment of bit vector would allow this to be optimized
    @Override
    public void readBits(BitVector bits) throws BitStreamException {
    	if (bits == null) throw new IllegalArgumentException("null bits");
    	for (int i = bits.size() - 1; i >= 0; i--) {
			bits.setBit(i, readBoolean());
		}
    }

    @Override
    public BigInteger readBigInt(int count) throws BitStreamException {
    	BitVector bits = new BitVector(count);
    	readBits(bits);
    	return bits.toBigInteger();
    }
    
	@Override
	public int skipToBoundary(BitBoundary boundary) {
		if (boundary == null) throw new IllegalArgumentException("null boundary");
		int count = bitsToBoundary(boundary);
		skipBits(count);
		return count;
	}

	@Override
	public long skipBits(long count) {
		if (count < 0L) throw new IllegalArgumentException("negative count");
		long remaining = count;
		for (; remaining > 0; remaining--) {
			try {
				readBit();
			} catch (EndOfBitStreamException e) {
				return count - remaining;
			}
		}
		return count;
	}
	
	@Override
	public long getPositionInStream() {
		return -1;
	}
	
	int bitsToBoundary(BitBoundary boundary) {
		long position = getPositionInStream();
		if (position < 0) throw new UnsupportedOperationException("reader does not support position");
		return -(int)position & boundary.mask;
	}
    
}
