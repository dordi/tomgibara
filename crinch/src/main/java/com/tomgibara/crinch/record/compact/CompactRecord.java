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
		switch (next.getType()) {
		case BYTE_PRIMITIVE:
		case BYTE_WRAPPER:
		case SHORT_PRIMITIVE:
		case SHORT_WRAPPER:
		case INT_PRIMITIVE:
		case INT_WRAPPER:
		case CHAR_PRIMITIVE:
		case CHAR_WRAPPER:
			next.decodeInt(reader);
			break;
		case LONG_PRIMITIVE:
		case LONG_WRAPPER:
			next.decodeLong(reader);
			break;
		case BOOLEAN_PRIMITIVE:
		case BOOLEAN_WRAPPER:
			next.decodeBoolean(reader);
			break;
		case FLOAT_PRIMITIVE:
		case FLOAT_WRAPPER:
			next.decodeFloat(reader);
			break;
		case DOUBLE_PRIMITIVE:
		case DOUBLE_WRAPPER:
			next.decodeDouble(reader);
			break;
		case STRING_OBJECT:
			next.decodeString(reader);
			break;
		default: throw new IllegalStateException();
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
