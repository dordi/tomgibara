/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


//must override write(int, int) or writeBit(int)
public abstract class AbstractBitWriter implements BitWriter {

    public int writeBoolean(boolean bit) {
        return writeBit(bit ? 1 : 0);
    }

    public int writeBit(int bit) {
        return write(bit, 1);
    }
    
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

    public int write(int bits, int count) {
        if (count == 0) return 0;
        int c = 0;
        for (count--; count >= 0; count--) {
            c += writeBit(bits >>> count);
        }
        return c;
    }

    public int write(long bits, int count) {
    	if (count <= 32) {
    		return write((int) bits, count);
    	} else {
    		return write((int)(bits >> 32), count - 32) + write((int) bits, count);
    	}
    }

    public void flush() {
    }
	
}
