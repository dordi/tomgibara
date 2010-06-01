package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * Convenience base class for implementing the {@link MultiHash} interface.
 * 
 * Either the hashAsXXX(Object) methods or the
 * {@link #hashAsList(Object, int)} method must be reimplemented by any
 * concrete extension of the class since this class defines them in terms of
 * each other.
 * 
 * @author tomgibara
 * 
 * @param <T>
 *            the type of objects for which hashes may be generated
 */
public abstract class AbstractMultiHash<T> implements MultiHash<T> {

	@Override
	public int getMaxMultiplicity() {
		return 1;
	}
	
	@Override
	public BigInteger hashAsBigInt(T value) {
		return hashAsList(value, 1).get(0);
	}

	@Override
	public int hashAsInt(T value) {
		return hashAsList(value, 1).getAsInt(0);
	}

	@Override
	public long hashAsLong(T value) {
		return hashAsList(value, 1).getAsLong(0);
	}

	@Override
	public HashList hashAsList(final T value, final int multiplicity) {
		if (multiplicity < 0) throw new IllegalArgumentException("Negative multiplicity");
		if (multiplicity > 1) throw new IllegalArgumentException("Only one hash supported");

		return new AbstractHashList() {
			
			@Override
			public int size() {
				return multiplicity;
			}
			
			@Override
			public BigInteger get(int index) {
				checkIndex(index);
				return hashAsBigInt(value);
			}
			
			@Override
			public int getAsInt(int index) throws IndexOutOfBoundsException {
				checkIndex(index);
				return hashAsInt(value);
			}

			@Override
			public long getAsLong(int index) throws IndexOutOfBoundsException {
				checkIndex(index);
				return hashAsLong(value);
			}
			
			private void checkIndex(int index) {
				if (index < 0 || index >= multiplicity) throw new IndexOutOfBoundsException("index " + index);
			}
			
		};
	}
		
}
