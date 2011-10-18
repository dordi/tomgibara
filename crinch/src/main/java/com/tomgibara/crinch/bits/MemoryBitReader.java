/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


public class MemoryBitReader extends AbstractBitReader {

    // statics
    
    //hope that this will be inlined...
    //i is number of 1's in lsbs
    private static int mask(int i) {
        return i == 0 ? 0 : -1 >>> (32 - i);
    }
    
    // fields
    
    private final int[] memory;
    private long size;
    private long position;
    
    // constructors
    
    public MemoryBitReader(int[] memory, long size, long position) {
        this.memory = memory;
        setSize(size);
        setPosition(position);
    }

    // bit reader methods
    
    @Override
    public int readBit() {
        if (position >= size) throw new IllegalStateException();
        int k = (memory[(int)(position >> 5)] >> (31 - (((int)position) & 31))) & 1;
        position++;
        return k;
    }

    @Override
    public int read(int count) {
        if (count == 0) return 0;
        if (position + count > size) throw new IllegalStateException(String.format("position: %d, size: %d, count %d", position, size, count));
        int frontBits = ((int)position) & 31;
        int firstInt = (int)(position >> 5);
        int value;

        int sumBits = count + frontBits;
        if (sumBits <= 32) {
            value = (memory[firstInt] >> (32 - sumBits)) & mask(count);
        } else {
            value = ((memory[firstInt] << (sumBits - 32)) | (memory[firstInt + 1] >>> (64 - sumBits))) & mask(count);
        }

        position += count;
        return value;
    }

    // accessors
    
    public int[] getMemory() {
        return memory;
    }

    public void setSize(long size) {
        if (size < 0) throw new IllegalArgumentException();
        if (size > memory.length << 5) throw new IllegalArgumentException();
        if (size < position) position = size;
        this.size = size;
    }
    
    public long getSize() {
        return size;
    }
    
    public long getPosition() {
        return position;
    }
    
    public void setPosition(long position) {
        if (position < 0) throw new IllegalArgumentException();
        if (position > size) throw new IllegalArgumentException();
        this.position = position;
    }
    
}
