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
	public long getRecordOrdinal() {
		return ordinal;
	}

	@Override
	public long getRecordPosition() {
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
	public void exhaust() {
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
