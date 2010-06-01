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

public abstract class AbstractWriteStream implements WriteStream {

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}
	
	@Override
	public void writeDouble(float v) {
		writeLong(Double.doubleToLongBits(v));
	}
	
	@Override
	public void writeBytes(byte[] bs) {
		writeBytes(bs, 0, bs.length);
	}

	@Override
	public void writeChars(char[] cs) {
		writeChars(cs, 0, cs.length);
	}

	@Override
	public void writeString(String v) {
		final int length = v.length();
		writeInt(length);
		writeChars(v.toCharArray());
	}

}
