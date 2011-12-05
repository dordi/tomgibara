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

public interface WriteStream {

	void writeByte(byte v);

	void writeBytes(byte bs[]);
	
	void writeBytes(byte bs[], int off, int len);

	void writeInt(int v);
	
	void writeBoolean(boolean v);

	void writeShort(short v);
	
	void writeLong(long v);
	
	void writeFloat(float v);
	
	void writeDouble(double v);
	
	void writeChar(char v);
	
	void writeChars(char[] cs);

	void writeChars(char[] cs, int off, int len);
	
	void writeString(String v);

}
