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
        if (count == 0) return 0;
        int acc = readBit();
        while (--count > 0) {
            acc = acc << 1 | readBit();
        }
        return acc;
    }

    @Override
    public long readLong(int count) {
        if (count == 0) return 0;
        if (count <= 32) return read(count) & 0x00000000ffffffffL;
        return (((long)read(count - 32)) << 32) | (read(32) & 0x00000000ffffffffL);
    }
    
    @Override
    public void readBits(BitVector bits) throws BitStreamException {
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
    
}
