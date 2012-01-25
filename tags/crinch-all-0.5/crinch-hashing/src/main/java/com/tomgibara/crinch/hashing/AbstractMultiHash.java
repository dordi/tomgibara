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

	private static final int[] EMPTY_INTS = {};
	private static final long[] EMPTY_LONGS = {};
	private static final BigInteger[] EMPTY_BIGINTS = {};

	static long[] copy(int[] ints, long[] longs) {
		for (int i = 0; i < longs.length; i++) {
			longs[i] = ints[i];
		}
		return longs;
	}
	
	static int[] copy(long[] longs, int[] ints) {
		for (int i = 0; i < ints.length; i++) {
			ints[i] = (int) longs[i];
		}
		return ints;
	}
	
	static BigInteger[] copy(int[] ints, BigInteger[] bigInts) {
		for (int i = 0; i < bigInts.length; i++) {
			bigInts[i] = BigInteger.valueOf(ints[i]);
		}
		return bigInts;
	}
	
	static int[] copy(BigInteger[] bigInts, int[] ints) {
		for (int i = 0; i < ints.length; i++) {
			ints[i] = bigInts[i].intValue();
		}
		return ints;
	}
	
	static long[] copy(BigInteger[] bigInts, long[] longs) {
		for (int i = 0; i < longs.length; i++) {
			longs[i] = bigInts[i].longValue();
		}
		return longs;
	}
	
	@Override
	public int getMaxMultiplicity() {
		return 1;
	}
	
	@Override
	public int hashAsInt(T value) {
		return hashAsInts(value, 1)[0];
	}

	@Override
	public long hashAsLong(T value) {
		return hashAsLongs(value, 1)[0];
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return hashAsBigInts(value, 1)[0];
	}

	@Override
	public int[] hashAsInts(T value, int multiplicity) throws IllegalArgumentException {
		if (multiplicity < 0) throw new IllegalArgumentException("negative multiplicity");
		if (multiplicity == 0) return EMPTY_INTS;
		return hashAsInts(value, new int[multiplicity]);
	}
	
	@Override
	public int[] hashAsInts(T value, int[] array) throws IllegalArgumentException {
		if (array == null) throw new IllegalArgumentException("null array");
		if (array.length != 1) throw new IllegalArgumentException("only one hash supported");
		array[0] = hashAsInt(value);
		return array;
	}
	
	@Override
	public long[] hashAsLongs(T value, int multiplicity) throws IllegalArgumentException {
		if (multiplicity < 0) throw new IllegalArgumentException("negative multiplicity");
		if (multiplicity == 0) return EMPTY_LONGS;
		return hashAsLongs(value, new long[multiplicity]);
	}
	
	@Override
	public long[] hashAsLongs(T value, long[] array) throws IllegalArgumentException {
		if (array == null) throw new IllegalArgumentException("null array");
		if (array.length != 1) throw new IllegalArgumentException("only one hash supported");
		array[0] = hashAsLong(value);
		return array;
	}
	
	@Override
	public BigInteger[] hashAsBigInts(T value, int multiplicity) throws IllegalArgumentException {
		if (multiplicity < 0) throw new IllegalArgumentException("negative multiplicity");
		if (multiplicity == 0) return EMPTY_BIGINTS;
		return hashAsBigInts(value, new BigInteger[multiplicity]);
	}
	
	@Override
	public BigInteger[] hashAsBigInts(T value, BigInteger[] array) throws IllegalArgumentException {
		if (array == null) throw new IllegalArgumentException("null array");
		if (array.length != 1) throw new IllegalArgumentException("only one hash supported");
		array[0] = hashAsBigInt(value);
		return array;
	}
	
}
