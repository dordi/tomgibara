package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

class IntRerangedHash<T> extends BigIntRerangedHash<T> {

	private final int oldMin;
	private final int newMin;
	private final int newSize;

	public IntRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash, newRange);
		oldMin = oldRange.getMinimum().intValue();
		newMin = newRange.getMinimum().intValue();
		newSize = newRange.getSize().intValue();
	}

	@Override
	protected BigInteger adaptedBigIntHash(T value) {
		return BigInteger.valueOf(adaptedIntHash(value));
	}


	@Override
	protected BigInteger adaptedBigIntHash(HashList list, int index) {
		return BigInteger.valueOf(adaptedIntHash(list, index));
	}


	@Override
	protected int adaptedIntHash(T value) {
		return reranged(multiHash.hashAsInt(value));
	}


	@Override
	protected int adaptedIntHash(HashList list, int index) {
		return reranged(list.getAsInt(index));
	}


	@Override
	protected long adaptedLongHash(T value) {
		return adaptedIntHash(value);
	}

	@Override
	protected long adaptedLongHash(HashList list, int index) {
		return adaptedIntHash(list, index);
	}

	private int reranged(int h) {
		h -= oldMin;
		if (isSmaller) {
			return (h % newSize) + newMin;
		} else {
			return h + newMin;
		}
	}
	
}
