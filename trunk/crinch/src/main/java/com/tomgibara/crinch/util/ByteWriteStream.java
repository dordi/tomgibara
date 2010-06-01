/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.util;

import java.util.Arrays;

public class ByteWriteStream extends AbstractWriteStream {

	private static final int DEFAULT_CAPACITY = 32;

	private static final int MAX_CAPACITY_INCR = 1024 * 1024;
	
	private int position;
	private byte[] bytes;
	
	public ByteWriteStream() {
		this(DEFAULT_CAPACITY);
	}

	public ByteWriteStream(int initialCapacity) {
		position = 0;
		bytes = new byte[initialCapacity];
	}
	
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, position);
	}
	
	@Override
	public void writeByte(byte v) {
		ensureFurtherCapacity(1);
		bytes[position++] = v;
	}
	
	@Override
	public void writeBytes(byte[] vs) {
		final int length = vs.length;
		ensureFurtherCapacity(length);
		System.arraycopy(vs, 0, bytes, position, length);
		position += length;
	}
	
	@Override
	public void writeBytes(byte[] vs, int off, int len) {
		ensureFurtherCapacity(len);
		System.arraycopy(vs, off, bytes, position, len);
		position += len;
	}

	@Override
	public void writeBoolean(boolean v) {
		ensureFurtherCapacity(1);
		bytes[position++] = (byte) (v ? -1 : 0);
	}

	@Override
	public void writeInt(int v) {
		ensureFurtherCapacity(4);
		bytes[position++] = (byte) (v >> 24);
		bytes[position++] = (byte) (v >> 16);
		bytes[position++] = (byte) (v >>  8);
		bytes[position++] = (byte) (v      );
	}
	
	@Override
	public void writeChar(char v) {
		ensureFurtherCapacity(2);
		bytes[position++] = (byte) (v >>  8);
		bytes[position++] = (byte) (v      );
	}
	
	@Override
	public void writeChars(char[] vs, int off, int len) {
		ensureFurtherCapacity(len * 2);
		final int lim = off + len;
		for (int i = off; i < lim; i++) {
			final char v = vs[i];
			bytes[position++] = (byte) (v >>  8);
			bytes[position++] = (byte) (v      );
		}
	}

	@Override
	public void writeShort(short v) {
		ensureFurtherCapacity(2);
		bytes[position++] = (byte) (v >>  8);
		bytes[position++] = (byte) (v      );
	}

	@Override
	public void writeLong(long v) {
		ensureFurtherCapacity(8);
		bytes[position++] = (byte) (v >> 56);
		bytes[position++] = (byte) (v >> 48);
		bytes[position++] = (byte) (v >> 40);
		bytes[position++] = (byte) (v >> 32);
		bytes[position++] = (byte) (v >> 24);
		bytes[position++] = (byte) (v >> 16);
		bytes[position++] = (byte) (v >>  8);
		bytes[position++] = (byte) (v      );
	}
	
	private void ensureFurtherCapacity(int n) {
		if (position + n > bytes.length) {
			int c = bytes.length;
			c += c < DEFAULT_CAPACITY ? DEFAULT_CAPACITY : c;
			if (c - bytes.length > MAX_CAPACITY_INCR) c = bytes.length + MAX_CAPACITY_INCR;
			bytes = Arrays.copyOf(bytes, c);
		}
		
	}
	
	
}
