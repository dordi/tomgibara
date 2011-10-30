package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.record.LinearRecord;

class CompactRecord implements LinearRecord {

	private final ColumnCompactor[] compactors;
	private final BitReader reader;
	private int index = 0;
	private boolean nullFlag;
	
	CompactRecord(ColumnCompactor[] compactors, BitReader reader) {
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
		return (char) next.decodeInt(reader);
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
		next.decodeNull(reader);
		switch (next.getStats().getClassification()) {
		case INTEGRAL:
			next.decodeLong(reader);
			break;
		case ENUMERATED:
			//TODO assumes boolean ATM
			next.decodeBoolean(reader);
		case FLOATING:
			//TODO no way of knowing correct encoding (float/double?)
			throw new UnsupportedOperationException();
		case TEXTUAL:
			next.decodeString(reader);
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
