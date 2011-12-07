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

import java.security.MessageDigest;

public class DigestWriteStream extends AbstractWriteStream {

	private final MessageDigest digest;
	
	public DigestWriteStream(MessageDigest digest) {
		this.digest = digest;
	}
	
	@Override
	public void writeByte(byte v) {
		digest.update(v);
	}
	
	@Override
	public void writeBytes(byte[] vs) {
		digest.update(vs);
	}
	
	@Override
	public void writeBytes(byte[] vs, int off, int len) {
		digest.update(vs, off, len);
	}
	
	@Override
	public void writeBoolean(boolean v) {
		digest.update((byte) (v ? -1 : 0));
	}
	
	@Override
	public void writeInt(int v) {
		digest.update((byte) (v >> 24));
		digest.update((byte) (v >> 16));
		digest.update((byte) (v >>  8));
		digest.update((byte) (v      ));
	}
	
	@Override
	public void writeShort(short v) {
		digest.update((byte) (v >>  8));
		digest.update((byte) (v      ));
	}

	@Override
	public void writeLong(long v) {
		digest.update((byte) (v >> 56));
		digest.update((byte) (v >> 48));
		digest.update((byte) (v >> 40));
		digest.update((byte) (v >> 32));
		digest.update((byte) (v >> 24));
		digest.update((byte) (v >> 16));
		digest.update((byte) (v >>  8));
		digest.update((byte) (v      ));
	}
	
	@Override
	public void writeChar(char v) {
		digest.update((byte) (v >>  8));
		digest.update((byte) (v      ));
	}

	@Override
	public void writeChars(char[] vs, int off, int len) {
		final int lim = off + len;
		for (int i = off; i < lim; i++) {
			final char v = vs[i];
			digest.update((byte) (v >>  8));
			digest.update((byte) (v      ));
		}
	}

}
