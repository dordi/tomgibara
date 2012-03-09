/*
 * Copyright 2011 Tom Gibara
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
package com.tomgibara.crinch.record;

import com.tomgibara.crinch.hashing.Hashes;

public class EmptyRecord implements LinearRecord {

	private final long ordinal;
	private final long position;
	
	public EmptyRecord(long ordinal, long position) {
		if (ordinal < 0) throw new IllegalArgumentException("negative ordinal");
		if (position < 0) throw new IllegalArgumentException("negative position");
		this.ordinal = ordinal;
		this.position = position;
	}
	
	@Override
	public long getOrdinal() {
		return ordinal;
	}

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public String nextString() {
		noNext();
		return null;
	}

	@Override
	public char nextChar() {
		noNext();
		return 0;
	}

	@Override
	public boolean nextBoolean() {
		noNext();
		return false;
	}

	@Override
	public byte nextByte() {
		noNext();
		return 0;
	}

	@Override
	public short nextShort() {
		noNext();
		return 0;
	}

	@Override
	public int nextInt() {
		noNext();
		return 0;
	}

	@Override
	public long nextLong() {
		noNext();
		return 0;
	}

	@Override
	public float nextFloat() {
		noNext();
		return 0;
	}

	@Override
	public double nextDouble() {
		noNext();
		return 0;
	}

	@Override
	public void skipNext() {
		noNext();
	}

	@Override
	public boolean wasNull() {
		noPrevious();
		return false;
	}

	@Override
	public boolean wasInvalid() {
		noPrevious();
		return false;
	}
	
	@Override
	public void mark() {
	}
	
	@Override
	public void reset() {
	}

	@Override
	public void release() {
	}

	private final void noNext() {
		throw new IllegalStateException("no columns");
	}
	
	private final void noPrevious() {
		throw new IllegalStateException("no columns");
	}
	
	@Override
	public int hashCode() {
		return Hashes.hashCode(ordinal) ^ (31 * Hashes.hashCode(position));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof EmptyRecord)) return false;
		EmptyRecord that = (EmptyRecord) obj;
		if (this.ordinal != that.ordinal) return false;
		if (this.position != that.position) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Empty Record {ordinal: " + ordinal + ", position: " + position + "}";
	}
	
}
