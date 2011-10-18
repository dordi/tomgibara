/*
 * Copyright (C) 2007  Tom Gibara
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.tomgibara.crinch.bits;

import java.nio.MappedByteBuffer;

public class MappedBitReader extends AbstractBitReader {

    private final MappedByteBuffer buffer;
    private long size;
    private long offset;
    
    private boolean currentSet = false;
    private byte current;

    // constructors
    
    public MappedBitReader(MappedByteBuffer buffer, long size, long position) {
        this.buffer = buffer;
        setSize(size);
        buffer.position();
        //setPosition(position);
    }

    public void setSize(long size) {
    	if ((size+7)/8 > buffer.limit()) throw new IllegalArgumentException();
		if (size < getPosition()) setPosition(size);
		this.size = size;
	}
    
    long getSize() {
    	return size;
    }
    
    long getPosition() {
    	if (currentSet) {
        	return (buffer.position()-1) * 8L + offset;
    	} else {
    		return buffer.position() * 8L + offset;
    	}
    }
    
    void setPosition(long position) {
        if (position < 0) throw new IllegalArgumentException();
        if (position > size) throw new IllegalArgumentException();
        buffer.position((int) (position/8));
        offset = position % 8;
        updateCurrent();
    }
    
	@Override
	public int read(int count) {
		if (count == 0) return 0;
		throw new UnsupportedOperationException();
	}

	private void updateCurrent() {
		if (buffer.hasRemaining()) {
			current = buffer.get();
			currentSet = true;
		} else {
			currentSet = false;
		}
	}
	
}
