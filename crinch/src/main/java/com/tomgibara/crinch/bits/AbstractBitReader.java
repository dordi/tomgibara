/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


public abstract class AbstractBitReader implements BitReader {

    public int readBit() {
        return read(1);
    }
    
    @Override
    public boolean readBoolean() {
    	return readBit() == 1;
    }
    
    public int read(int count) {
        if (count == 0) return 0;
        int acc = readBit();
        while (--count > 0) {
            acc = acc << 1 | readBit();
        }
        return acc;
    }

    //TODO untested
    public long readLong(int count) {
        if (count == 0) return 0;
        if (count <= 32) return read(count);
        return (((long)read(32)) << 32) | read(count-32);
    }
    
}
