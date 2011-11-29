/*
 * Copyright 2010 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.AbstractWriteStream;

public class CondensingWriteStream extends AbstractWriteStream {

	private long condensedValue = 0L;
	
	@Override
	public void writeByte(byte v) {
		condensedValue = condensedValue * 31 + v & 0xff;
	}

	@Override
	public void writeChar(char v) {
		condensedValue = condensedValue * 31 + v;
	}

	@Override
	public void writeShort(short v) {
		condensedValue = condensedValue * 31 + v & 0xffff;
	}
	
	@Override
	public void writeInt(int v) {
		condensedValue = condensedValue * 31 + v & 0xffffffffL;
	}

	@Override
	public void writeLong(long v) {
		condensedValue = condensedValue * 31 + v;
	}

	public long getCondensedValue() {
		return condensedValue;
	}
	
}
