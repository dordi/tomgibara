/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

import java.math.BigInteger;


//must override write(int, int) or writeBit(int)
public abstract class AbstractBitWriter implements BitWriter {

    @Override
    public int writeBoolean(boolean bit) {
        return writeBit(bit ? 1 : 0);
    }

    @Override
    public int writeBit(int bit) {
        return write(bit, 1);
    }
    
    @Override
    public int writeZeros(int count) {
        if (count == 0) return 0;
        if (count <= 32) return write(0, count);

        int c = 0;
        while (count > 32) {
            c += write(0, 32);
            count -= 32;
        }
        return c;
    }

    @Override
    public int writeOnes(int count) {
        if (count == 0) return 0;
        if (count <= 32) return write(-1, count);

        int c = 0;
        while (count > 32) {
            c += write(-1, 32);
            count -= 32;
        }
        return c;
    }

    @Override
    public int write(int bits, int count) {
        if (count == 0) return 0;
        int c = 0;
        for (count--; count >= 0; count--) {
            c += writeBit(bits >>> count);
        }
        return c;
    }

    @Override
    public int write(long bits, int count) {
    	if (count <= 32) {
    		return write((int) bits, count);
    	} else {
    		return write((int)(bits >> 32), count - 32) + write((int) bits, 32);
    	}
    }

    @Override
    public int write(BigInteger bits, int count) {
    	if (count <= 32) return write(bits.intValue(), count);
    	if (count <= 64) return write(bits.longValue(), count);
    	int c = 0;
    	for (count--; count >= 0; count--) {
        	c += writeBoolean( bits.testBit(count) );
		}
    	return c;
    }
    
    @Override
    public int write(BitVector bits) {
    	int size = bits.size();
    	if (size <= 32) return write(bits.intValue(), size);
    	if (size <= 64) return write(bits.longValue(), size);
    	int c = 0;
    	for (size--; size >= 0; size--) {
        	c += writeBoolean( bits.getBit(size) );
		}
    	return c;
    }
    
    public void flush() {
    }
	
}
