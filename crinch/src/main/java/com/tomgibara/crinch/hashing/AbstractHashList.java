package com.tomgibara.crinch.hashing;

import java.math.BigInteger;
import java.util.AbstractList;

/**
 * Convenience base class for implementing the {@link HashList} interface. At a
 * minimum, one of {@link #get(int)} or {@link #getAsLong(int)} must be
 * implemented in addition to the {@link #size()} method.
 * 
 * @author tomgibara
 * 
 */

public abstract class AbstractHashList extends AbstractList<BigInteger> implements HashList {

	@Override
	public BigInteger get(int index) {
		return BigInteger.valueOf(getAsLong(index));
	}

	@Override
	public int getAsInt(int index) throws IndexOutOfBoundsException {
		return get(index).intValue();
	}

	@Override
	public long getAsLong(int index) throws IndexOutOfBoundsException {
		return get(index).longValue();
	}

}
