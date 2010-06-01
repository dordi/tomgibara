package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

class BigIntRerangedHash<T> extends AdaptedMultiHash<T> {

	final HashRange oldRange;
	final HashRange newRange;
	final boolean isSmaller;

	public BigIntRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash);
		this.newRange = newRange;
		oldRange = hash.getRange();
		isSmaller = newRange.getSize().compareTo(oldRange.getSize()) < 0;
	}

	@Override
	public HashRange getRange() {
		return newRange;
	}
	
	@Override
	protected BigInteger adaptedBigIntHash(T value) {
		return reranged(multiHash.hashAsBigInt(value));
	}


	@Override
	protected BigInteger adaptedBigIntHash(HashList list, int index) {
		return reranged(list.get(index));
	}


	@Override
	protected int adaptedIntHash(T value) {
		return adaptedBigIntHash(value).intValue();
	}


	@Override
	protected int adaptedIntHash(HashList list, int index) {
		return adaptedBigIntHash(list, index).intValue();
	}


	@Override
	protected long adaptedLongHash(T value) {
		return adaptedBigIntHash(value).longValue();
	}


	@Override
	protected long adaptedLongHash(HashList list, int index) {
		return adaptedBigIntHash(list, index).intValue();
	}


	private BigInteger reranged(BigInteger h) {
		h = h.subtract(oldRange.getMinimum());
		if (isSmaller) {
			return h.mod(newRange.getSize()).add(newRange.getMinimum());
		} else {
			return h.add(newRange.getMinimum());
		}
	}


}