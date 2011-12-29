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

// TODO could generalize into a base class that will accomodate pairs and arrays
public class CombinedRecord implements LinearRecord {

	private final LinearRecord a;
	private final LinearRecord b;
	private LinearRecord last = null;
	private LinearRecord mark = null;
	
	public CombinedRecord(LinearRecord firstRecord, LinearRecord secondRecord) {
		if (firstRecord == null) throw new IllegalArgumentException("null firstRecord");
		if (secondRecord == null) throw new IllegalArgumentException("null SecondRecord");
		a = firstRecord;
		b = secondRecord;
	}

	@Override
	public long getOrdinal() {
		return current().getOrdinal();
	}

	@Override
	public long getPosition() {
		return current().getPosition();
	}

	@Override
	public boolean hasNext() {
		return a.hasNext() || b.hasNext();
	}

	@Override
	public CharSequence nextString() {
		return next().nextString();
	}

	@Override
	public char nextChar() {
		return next().nextChar();
	}

	@Override
	public boolean nextBoolean() {
		return next().nextBoolean();
	}

	@Override
	public byte nextByte() {
		return next().nextByte();
	}

	@Override
	public short nextShort() {
		return next().nextShort();
	}

	@Override
	public int nextInt() {
		return next().nextInt();
	}

	@Override
	public long nextLong() {
		return next().nextLong();
	}

	@Override
	public float nextFloat() {
		return next().nextFloat();
	}

	@Override
	public double nextDouble() {
		return next().nextDouble();
	}

	@Override
	public void skipNext() {
		current().skipNext();
		last = null;
	}

	@Override
	public boolean wasNull() {
		if (last == null) throw new IllegalStateException("no value requested");
		return last.wasNull();
	}

	@Override
	public boolean wasInvalid() {
		if (last == null) throw new IllegalStateException("no value requested");
		return last.wasInvalid();
	}

	@Override
	public void mark() {
		if (current() == a) a.mark();
		b.mark();
	}

	@Override
	public void reset() {
		if (mark == null) throw new IllegalStateException("not marked");
		if (mark == a) a.reset();
		b.reset();
		last = null;
	}

	@Override
	public void release() {
		while (current().hasNext()) current().release();
	}

	private LinearRecord current() {
		return a.hasNext() ? a : b;
	}
	
	private LinearRecord next() {
		return last = a.hasNext() ? a : b;
	}

	@Override
	public String toString() {
		return a + "+" + b;
	}
	
}
