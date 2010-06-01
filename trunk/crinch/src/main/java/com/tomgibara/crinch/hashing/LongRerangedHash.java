package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

class LongRerangedHash<T> extends BigIntRerangedHash<T> {

	private final long oldMin;
	private final long newMin;
	private final long newSize;

	public LongRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash, newRange);
		oldMin = oldRange.getMinimum().longValue();
		newMin = newRange.getMinimum().longValue();
		newSize = newRange.getSize().longValue();
	}

	@Override
	protected BigInteger adaptedBigIntHash(T value) {
		return BigInteger.valueOf(adaptedLongHash(value));
	}


	@Override
	protected BigInteger adaptedBigIntHash(HashList list, int index) {
		return BigInteger.valueOf(adaptedLongHash(list, index));
	}


	@Override
	protected int adaptedIntHash(T value) {
		return (int) adaptedLongHash(value);
	}


	@Override
	protected int adaptedIntHash(HashList list, int index) {
		return (int) adaptedIntHash(list, index);
	}


	@Override
	protected long adaptedLongHash(T value) {
		return reranged(multiHash.hashAsInt(value));
	}

	@Override
	protected long adaptedLongHash(HashList list, int index) {
		return reranged(list.getAsInt(index));
	}

	private long reranged(long h) {
		h -= oldMin;
		if (isSmaller) {
			return (h % newSize) + newMin;
		} else {
			return h + newMin;
		}
	}
	
}
