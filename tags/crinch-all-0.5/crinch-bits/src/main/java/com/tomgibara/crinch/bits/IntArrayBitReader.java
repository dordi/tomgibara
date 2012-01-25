/*
 * Copyright 2007 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.bits;

//TODO optimize readUntil
public class IntArrayBitReader extends AbstractBitReader {

    // statics
    
    //hope that this will be inlined...
    //i is number of 1's in lsbs
    private static int mask(int i) {
        return i == 0 ? 0 : -1 >>> (32 - i);
    }
    
    // fields
    
    private final int[] ints;
    private final long size;
    private long position = 0L;
    
    // constructors

    public IntArrayBitReader(int[] ints) {
    	if (ints == null) throw new IllegalArgumentException("null ints");
    	this.ints = ints;
    	size = ((long) ints.length) << 5;
    }

    public IntArrayBitReader(int[] ints, long size) {
    	if (ints == null) throw new IllegalArgumentException("null ints");
        if (size < 0) throw new IllegalArgumentException("negative size");
        long maxSize = ((long) ints.length) << 5;
        if (size > maxSize) throw new IllegalArgumentException("size exceeds maximum permitted by array length");
        this.ints = ints;
        this.size = size;
    }

    // bit reader methods
    
    @Override
    public int readBit() {
        if (position >= size) throw new EndOfBitStreamException();
        int k = (ints[(int)(position >> 5)] >> (31 - (((int)position) & 31))) & 1;
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
            value = (ints[firstInt] >> (32 - sumBits)) & mask(count);
        } else {
            value = ((ints[firstInt] << (sumBits - 32)) | (ints[firstInt + 1] >>> (64 - sumBits))) & mask(count);
        }

        position += count;
        return value;
    }

    @Override
    public long skipBits(long count) {
    	if (count < 0) throw new IllegalArgumentException("negative count");
    	count = Math.min(size - position, count);
    	position += count;
    	return count;
    }
    
    @Override
    public long getPosition() {
        return position;
    }
    
    // accessors
    
    public int[] getInts() {
        return ints;
    }

    public long getSize() {
        return size;
    }
    
    public void setPosition(long position) {
        if (position < 0) throw new IllegalArgumentException();
        if (position > size) throw new IllegalArgumentException();
        this.position = position;
    }
    
}
