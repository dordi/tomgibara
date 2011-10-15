package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

class RerangedHash<T> extends AdaptedMultiHash<T> {

	final HashRange oldRange;
	final HashRange newRange;
	final boolean isSmaller;

	final BigInteger bigOldMin;
	final BigInteger bigNewMin;
	final BigInteger bigNewSize;


	RerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash);
		this.newRange = newRange;
		oldRange = hash.getRange();
		isSmaller = newRange.getSize().compareTo(oldRange.getSize()) < 0;
		
		bigOldMin = oldRange.getMinimum();
		bigNewMin = newRange.getMinimum();
		bigNewSize = newRange.getSize();
	}
	
	@Override
	public HashRange getRange() {
		return newRange;
	}

}
