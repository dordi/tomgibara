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

import java.math.BigInteger;

/**
 * A null bit stream that counts the number of bits written.
 * 
 * This class is intended to be used in circumstances where adjusting writer
 * capacity may be less efficient than writing twice to a stream.
 * 
 * @author Tom Gibara
 * 
 */

public class NullBitWriter extends AbstractBitWriter {

	private long position = 0;
	
	@Override
	public int writeBit(int bit) {
		position ++;
		return 1;
	}

	@Override
	public int writeBoolean(boolean bit) {
		position ++;
		return 1;
	}

	@Override
	public int write(int bits, int count) {
		position += count;
		return count;
	}

	@Override
	public int writeOnes(int count) {
		position += count;
		return count;
	}
	
	@Override
	public int writeZeros(int count) {
		position += count;
		return count;
	}
	
	@Override
	public int write(long bits, int count) {
		position += count;
		return count;
	}
	
	@Override
	public int write(BigInteger bits, int count) {
		position += count;
		return count;
	}
	
	@Override
	public int write(BitVector bits) {
		int count = bits.size();
		position += count;
		return count;
	}
	
	@Override
	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		if (position < 0L) throw new IllegalArgumentException("negative position");
		this.position = position;
	}
	
}
