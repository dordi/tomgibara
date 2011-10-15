/*
 * Copyright 2010 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * Utility methods for working with objects in this package.
 * 
 * @author tomgibara
 *
 */

public class Hashes {

	private Hashes() {}

	/**
	 * Converts an integer array into a {@link HashList}. The supplied array is
	 * not copied by this method and therefore should not be modified after
	 * being supplied to this method.
	 * 
	 * @param values
	 *            an array of hashes
	 * 
	 * @return a {@link HashList} that returns the supplied values
	 * @throws IllegalArgumentException
	 *             if the supplied values are null
	 */
	
	public static HashList asHashList(int[] values) {
		return new IntHashList(values);
	}

	/**
	 * Converts a long array into a {@link HashList}. The supplied array is not
	 * copied by this method and therefore should not be modified after being
	 * supplied to this method.
	 * 
	 * @param values
	 *            an array of hashes
	 * 
	 * @return a {@link HashList} that returns the supplied values
	 * @throws IllegalArgumentException
	 *             if the supplied values are null
	 */
	
	public static HashList asHashList(long[] values) {
		return new LongHashList(values);
	}

	/**
	 * Converts an array of BigIntegers into a {@link HashList}. The supplied
	 * array is not copied by this method and therefore should not be modified
	 * after being supplied to this method.
	 * 
	 * @param values
	 *            an array of hashes
	 * 
	 * @return a {@link HashList} that returns the supplied values
	 * @throws IllegalArgumentException
	 *             if the supplied values are null
	 */
	
	public static HashList asHashList(BigInteger[] values) {
		return new BigIntHashList(values);
	}

	/**
	 * Returns a {@link MultiHash} implementation that returns hashes in a
	 * specified range, based on the hash values produced by another
	 * {@link MultiHash} implementation. If the range is being widened, there is
	 * no guarantee that every value in the new range will be used. If the new
	 * range is equal to that of the supplied {@link MultiHash}, the original
	 * hash will be returned, unmodified. To use this method with a plain
	 * {@link Hash}, first pass it to {@link #asMultiHash(Hash)}.
	 * 
	 * @param <T>
	 *            the type of objects for which hashes may be generated
	 * @param newRange
	 *            the range to which generated hash values should be constrained
	 * @param multiHash
	 *            supplies the hash values
	 * @return a {@link MultiHash} that returns values within the specified
	 *         range
	 * @throws IllegalArgumentException
	 *             if any of the arguments to the method is null
	 */
	
	public static <T> MultiHash<T> rangeAdjust(HashRange newRange, MultiHash<T> multiHash) throws IllegalArgumentException {
		if (newRange == null) throw new IllegalArgumentException("null newRange");
		if (multiHash == null) throw new IllegalArgumentException("null multiHash");
		final HashRange oldRange = multiHash.getRange();
		if (oldRange.equals(newRange)) return multiHash;
		if (newRange.isIntBounded() && newRange.isIntSized() && oldRange.isIntBounded() && oldRange.isIntSized()) return new IntRerangedHash<T>(multiHash, newRange);
		if (newRange.isLongBounded() && newRange.isLongSized() && oldRange.isLongBounded() && oldRange.isLongSized()) return new LongRerangedHash<T>(multiHash, newRange);
		return new BigIntRerangedHash<T>(multiHash, newRange);
	}
	
	/**
	 * Adapts a {@link Hash} implementation into a {@link MultiHash}
	 * implementation. If the supplied object already implements the
	 * {@link MultiHash} interface, the original object is returned, otherwise,
	 * a new object will be created that returns the same hash values through
	 * the {@link MultiHash} interface.
	 * 
	 * @param <T>
	 *            the type of objects for which hashes may be generated
	 * @param hash
	 *            the {@link Hash} implementation for which a {@link MultiHash}
	 *            is needed
	 * @return a {@link MultiHash} implementation that returns the hash values
	 *         from the supplied {@link Hash}
	 * @throws IllegalArgumentException
	 *             if the supplied hash is null
	 */
	
	public static <T> MultiHash<T> asMultiHash(Hash<T> hash) {
		if (hash == null) throw new IllegalArgumentException("null hash");
		if (hash instanceof MultiHash<?>) return (MultiHash<T>) hash;
		return new SingletonMultiHash<T>(hash);
	}
	
	private static class IntHashList extends AbstractHashList {
		
		private final int[] values;

		public IntHashList(int[] values) {
			if (values == null) throw new IllegalArgumentException("null values");
			this.values = values;
		}
		
		@Override
		public int size() {
			return values.length;
		}

		@Override
		public int getAsInt(int index) throws IndexOutOfBoundsException {
			if (index < 0 || index >= values.length) throw new IndexOutOfBoundsException("index " + index);
			return values[index];
		}

		@Override
		public long getAsLong(int index) throws IndexOutOfBoundsException {
			return getAsInt(index);
		}
		
	}
	
	private static class LongHashList extends AbstractHashList {
		
		private final long[] values;

		public LongHashList(long[] values) {
			if (values == null) throw new IllegalArgumentException("null values");
			this.values = values;
		}
		
		@Override
		public int size() {
			return values.length;
		}

		@Override
		public int getAsInt(int index) throws IndexOutOfBoundsException {
			return (int) getAsLong(index);
		}

		@Override
		public long getAsLong(int index) throws IndexOutOfBoundsException {
			if (index < 0 || index >= values.length) throw new IndexOutOfBoundsException("index " + index);
			return values[index];
		}
		
	}
	
	private static class BigIntHashList extends AbstractHashList {
		
		private final BigInteger[] values;

		public BigIntHashList(BigInteger[] values) {
			if (values == null) throw new IllegalArgumentException("null values");
			this.values = values;
		}
		
		@Override
		public int size() {
			return values.length;
		}

		@Override
		public BigInteger get(int index) {
			if (index < 0 || index >= values.length) throw new IndexOutOfBoundsException("index " + index);
			return values[index];
		}
		
	}
	
}
