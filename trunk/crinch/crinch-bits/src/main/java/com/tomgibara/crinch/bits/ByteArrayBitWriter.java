/*
 * Copyright 2011 Tom Gibara
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

import java.util.Arrays;

/**
 * Writes bits to an array of bytes,
 * 
 * @author Tom Gibara
 *
 */

public class ByteArrayBitWriter extends ByteBasedBitWriter {

	private final byte[] bytes;
	private int index;

	public ByteArrayBitWriter(byte[] bytes) {
		if (bytes == null) throw new IllegalArgumentException("null bytes");
		this.bytes = bytes;
		index = 0;
	}
	
	@Override
	protected void writeByte(int value) throws BitStreamException {
		if (index == bytes.length) throw new EndOfBitStreamException();
		bytes[index++] = (byte) value;
	}

	@Override
	protected long padBytes(boolean padWithOnes, long count) throws BitStreamException {
		count = Math.min(count, bytes.length - index);
		byte value = padWithOnes ? (byte) 255 : (byte) 0;
		int newIndex = index + (int) count;
		Arrays.fill(bytes, index, newIndex, value);
		index = newIndex;
		return count;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
}
