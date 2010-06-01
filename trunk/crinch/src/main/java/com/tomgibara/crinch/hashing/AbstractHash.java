package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * Convenience base class for implementing the {@link Hash} interface. At a
 * minimum, one of {@link #hashAsBigInt(Object)} or {@link #hashAsLong(Object)}
 * needs to be implemented in addition to the {@link #getRange()} method.
 * 
 * @author tomgibara
 * 
 * @param <T> the type of object over which hashes may be generated
 */

public abstract class AbstractHash<T> implements Hash<T> {

	@Override
	public BigInteger hashAsBigInt(T value) {
		return BigInteger.valueOf(hashAsLong(value));
	}

	@Override
	public int hashAsInt(T value) {
		return hashAsBigInt(value).intValue();
	}

	@Override
	public long hashAsLong(T value) {
		return hashAsBigInt(value).longValue();
	}

}
