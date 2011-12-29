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
package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.record.LinearRecord;

class CompactRecord implements LinearRecord {

	private final RecordDecompactor decompactor;
	private final ColumnCompactor[] compactors; // for performance
	
	private CodedReader reader;
	private long ordinal;
	private long position;
	private int index = 0;
	private boolean nullFlag;
	
	private CompactCharSequence charCache = null;
	
	CompactRecord(RecordDecompactor decompactor) {
		this.decompactor = decompactor;
		this.compactors = decompactor.getCompactors();
	}

	CompactRecord populate(CodedReader reader) {
		return populate(reader, -1L);
	}

	CompactRecord populate(CodedReader reader, long ordinal) {
		return populate(reader, ordinal, reader.getReader().getPosition());
	}
	
	CompactRecord populate(CodedReader reader, long ordinal, long position) {
		this.reader = reader;
		this.ordinal = ordinal;
		this.position = position;
		return this;
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
		return index < compactors.length;
	}

	@Override
	public boolean nextBoolean() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return false;
		return next.decodeBoolean(reader);
	}

	@Override
	public byte nextByte() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return (byte) 0;
		return (byte) next.decodeInt(reader);
	}

	@Override
	public char nextChar() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return '\0';
		return (char) next.decodeChar(reader);
	}

	@Override
	public double nextDouble() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return 0.0;
		return (double) next.decodeDouble(reader);
	}

	@Override
	public float nextFloat() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return 0.0f;
		return (float) next.decodeFloat(reader);
	}

	@Override
	public int nextInt() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return 0;
		return next.decodeInt(reader);
	}

	@Override
	public long nextLong() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return 0L;
		return next.decodeLong(reader);
	}

	@Override
	public short nextShort() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return (short) 0;
		return (short) next.decodeInt(reader);
	}

	@Override
	public CharSequence nextString() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return null;
		return cacheDecodedChars(next);
	}

	@Override
	public void skipNext() {
		ColumnCompactor next = next();
		if (next.decodeNull(reader)) return;
		switch (next.getStats().getClassification()) {
		case INTEGRAL:
			next.decodeLong(reader);
			break;
		case ENUMERATED:
			next.decodeString(reader);
			break;
		case FLOATING:
			next.decodeDouble(reader);
			break;
		case TEXTUAL:
			cacheDecodedChars(next);
			break;
		}
	}
	
	@Override
	public boolean wasNull() {
		if (index == 0) throw new IllegalStateException();
		return nullFlag;
	}

	@Override
	public boolean wasInvalid() {
		if (index == 0) throw new IllegalStateException();
		return false;
	}

	@Override
	public void mark() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void release() {
		while (index < compactors.length) {
			skipNext();
		}
		reader = null;
		ordinal = -1L;
		position = -1L;
		decompactor.spare(this);
		CompactCharSequence css = charCache;
		while (css != null) {
			CompactCharSequence tmp = css.next;
			css.recycle();
			css = tmp;
		}
	}
	
	private ColumnCompactor next() {
		if (index == compactors.length) throw new IllegalStateException("number of columns exceeded");
		return compactors[index++];
	}
	
	private CharSequence cacheDecodedChars(ColumnCompactor c) {
		CharSequence cs = c.decodeString(reader);
		if (cs instanceof CompactCharSequence) {
			CompactCharSequence ccs = (CompactCharSequence) cs;
			ccs.next = charCache;
			charCache = ccs;
		}
		return cs;
	}

}
