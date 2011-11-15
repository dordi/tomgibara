package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.record.AbstractRecord;
import com.tomgibara.crinch.record.LinearRecord;

class CompactRecord extends AbstractRecord implements LinearRecord {

	private final ColumnCompactor[] compactors;
	private final CodedReader reader;
	private int index = 0;
	private boolean nullFlag;
	
	CompactRecord(ColumnCompactor[] compactors, CodedReader reader, long ordinal) {
		super(ordinal, reader.getReader().getPosition());
		this.compactors = compactors;
		this.reader = reader;
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
	public String nextString() {
		ColumnCompactor next = next();
		nullFlag = next.decodeNull(reader);
		if (nullFlag) return null;
		return next.decodeString(reader);
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
			next.decodeString(reader);
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
	public void exhaust() {
		while (index < compactors.length) {
			skipNext();
		}
	}
	
	private ColumnCompactor next() {
		if (index == compactors.length) throw new IllegalStateException("number of columns exceeded");
		return compactors[index++];
	}

}
