/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


public class MemoryBitWriter extends AbstractBitWriter {

    // statics
    
    //hope that this will be inlined...
    private static int frontMask(int i) {
        return i == 0 ? 0 : -1 << (32 - i);
    }

    //hope that this will be inlined...
    private static int backMask(int i) {
        return i == 32 ? 0 : -1 >>> i;
    }

    //hope that this will be inlined...
    private static int preMask(int i) {
        return i == 0 ? 0 : -1 >>> (32 - i);
    }

    // fields
    
    private final int[] memory;
    private final long limit;
    private long size;
    private long position;
    private int bufferSize;
    private int bufferBits;
    
    // constructors
    
    public MemoryBitWriter(int[] memory, int size, int position) {
        this.memory = memory;
        this.limit = memory.length * 32;
        this.size = size;
        this.position = position;
        bufferSize = 0;
        bufferBits = 0;
    }

    // bit writer methods
    
    @Override
    public int write(int bits, int count) {
        if (count == 0) return 0;
        if (position + bufferSize + count > limit) throw new BitStreamException("memory full");

        if (bufferSize == 0) {
            bufferBits = bits;
            bufferSize = count;
        } else if (bufferSize + count <= 32) {
            bufferBits = (bufferBits << count) | bits & preMask(count);
            bufferSize += count;
        } else {
            flushBuffer();
            bufferBits = bits;
            bufferSize = count;
            
        }
        
        return count;
    }
    
    //optimized implementation
    @Override
    public int writeZeros(int count) {
        if (count == 0) return 0;
        if (position + bufferSize + count > limit) throw new BitStreamException("memory full");

//        if (count == 32) {
//            flushBuffer();
//            bufferBits = 0;
//            bufferSize = 32;
//            return 32;
//        }
        
        int c = count;
        while (c >= 32) {
            //writeZeros(32);
            flushBuffer();
            bufferBits = 0;
            bufferSize = 32;
            c -= 32;
        }

        if (c == 0) {
            /* do nothing */
//no advantage in this case
//        } else if (bufferSize == 0) {
//            bufferBits <<= count;
//            bufferSize = count;
        } else if (bufferSize + c <= 32) {
            bufferBits <<= c;
            bufferSize += c;
        } else {
            flushBuffer();
            bufferBits = 0;
            bufferSize = c;
        }

        return count;
    }
    
    //optimized implementation
    @Override
    public int writeOnes(int count) {
        if (count == 0) return 0;
        if (position + bufferSize + count > limit) throw new IllegalStateException(count + " " + position + " " + limit);

//        if (count == 32) {
//            flushBuffer();
//            bufferBits = -1;
//            bufferSize = 32;
//            return 32;
//        }
        
        int c = count;
        while (c >= 32) {
            //writeOnes(32);
            flushBuffer();
            bufferBits = -1;
            bufferSize = 32;
            c -= 32;
        }

        if (c == 0) {
            /* do nothing */
          } else if (bufferSize == 0) {
            bufferBits = -1;
            bufferSize = c;
        } else if (bufferSize + c <= 32) {
            bufferBits = (bufferBits << c) | preMask(count);
            bufferSize += c;
        } else {
            flushBuffer();
            bufferBits = -1;
            bufferSize = c;
        }

        return count;
    }
    
    @Override
    public long getPosition() {
        flushBuffer();
        return position;
    }
    
    // accessors
    
    public void setPosition(long position) {
        if (position < 0) throw new IllegalArgumentException();
        if (position > size) throw new IllegalArgumentException();
        flushBuffer();
        this.position = position;
    }
    
    public long getSize() {
    	flushBuffer();
        return size;
    }
    
    public void setSize(int size) {
        if (size < 0) throw new IllegalArgumentException();
        if (size > limit) throw new IllegalArgumentException();
        if (size < getPosition()) setPosition(size);
        this.size = size;
    }
    
    public long getLimit() {
        return limit;
    }
    
    public int[] getMemory() {
        return memory;
    }
    
    public void flush() {
        flushBuffer();
    }
    
    // object methods
    
    @Override
    public String toString() {
    	int length = (int) Math.min(size, 100);
        StringBuilder sb = new StringBuilder(length);
        MemoryBitReader reader = new MemoryBitReader(memory, length, 0);
        for (int i = 0; i < length; i++) {
            sb.append( reader.readBit() == 0 ? "0" : "1" );
        }
        if (length != size) sb.append("...");
        return sb.toString();
    }

    // private utlity methods
    
    private void flushBuffer() {
        if (bufferSize == 0) return;
        doWrite(bufferBits, bufferSize);
        position += bufferSize;
        if (position > size) size = position;
        //bufferBits = 0; we leave the buffer dirty
        bufferSize = 0;
    }
    
    //assumes count is non-zero
    private void doWrite(int bits, int count) {
        
        int frontBits = ((int) position) & 31;
        int firstInt = (int) (position >> 5);
        
        int sumBits = count + frontBits;
        if (sumBits <= 32) {
            int i = memory[firstInt];
            int mask = frontMask(frontBits) | backMask(sumBits);
            i &= mask;
            i |= (bits << (32 - sumBits)) & ~mask;
            memory[firstInt] = i;
        } else {
            int i = memory[firstInt];
            int mask = frontMask(frontBits);
            i &= mask;
            int lostBits = sumBits - 32;
            i |= (bits >> lostBits) & ~mask;
            memory[firstInt] = i;
            
            i = memory[firstInt + 1];
            mask = backMask(lostBits);
            i &= mask;
            i |= (bits << (32 - lostBits)) & ~mask;
            memory[firstInt + 1] = i;
        }
    }
    
}
