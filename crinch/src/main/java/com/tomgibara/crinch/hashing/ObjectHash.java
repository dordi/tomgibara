package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * Generic hash generator, produces a single hash which is the result of calling
 * {@link #hashCode()} on the object, or zero for null.
 * 
 * @author tomgibara
 * 
 */

public class ObjectHash<T> extends AbstractMultiHash<T> {

	@Override
	public HashRange getRange() {
		return HashRange.FULL_INT_RANGE;
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return BigInteger.valueOf(hashAsInt(value));
	}
	
	@Override
	public int hashAsInt(T value) {
		return value == null ? 0 : value.hashCode();
	}
	
	@Override
	public long hashAsLong(T value) {
		return hashAsInt(value);
	}
	
}
