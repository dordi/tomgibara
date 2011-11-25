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
	public long getRecordOrdinal() {
		return current().getRecordOrdinal();
	}

	@Override
	public long getRecordPosition() {
		return current().getRecordPosition();
	}

	@Override
	public boolean hasNext() {
		return a.hasNext() || b.hasNext();
	}

	@Override
	public String nextString() {
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
	public void exhaust() {
		while (current().hasNext()) current().exhaust();
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
