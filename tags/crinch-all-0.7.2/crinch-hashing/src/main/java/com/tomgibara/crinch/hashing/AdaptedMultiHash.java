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
 * Convenience base class for creating {@link MultiHash} implementations that
 * adjust the hash values returned by another {@link MultiHash} implementation.
 * 
 * @author tomgibara
 * 
 * @param <T> the type of object over which hashes may be generated
 */

public class AdaptedMultiHash<T> implements MultiHash<T> {

	protected final MultiHash<T> multiHash;

	public AdaptedMultiHash(MultiHash<T> multiHash) {
		if (multiHash == null) throw new IllegalArgumentException("null multiHash");
		this.multiHash = multiHash;
	}

	@Override
	public HashRange getRange() {
		return multiHash.getRange();
	}

	@Override
	public int getMaxMultiplicity() {
		return multiHash.getMaxMultiplicity();
	}

	@Override
	public int hashAsInt(T value) {
		return adapt(multiHash.hashAsInt(value));
	}

	@Override
	public long hashAsLong(T value) {
		return adapt(multiHash.hashAsLong(value));
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return adapt(multiHash.hashAsBigInt(value));
	}

	public int[] hashAsInts(T value, int multiplicity) {
		return multiHash.hashAsInts(value, multiplicity);
	}
	
	public int[] hashAsInts(T value, int[] array) {
		return multiHash.hashAsInts(value, array);
	}
	
	public long[] hashAsLongs(T value, int multiplicity) {
		return multiHash.hashAsLongs(value, multiplicity);
	}
	
	public long[] hashAsLongs(T value, long[] array) {
		return multiHash.hashAsLongs(value, array);
	}
	
	public BigInteger[] hashAsBigInts(T value, int multiplicity) {
		return multiHash.hashAsBigInts(value, multiplicity);
	}
	
	public BigInteger[] hashAsBigInts(T value, BigInteger[] array) {
		return multiHash.hashAsBigInts(value, array);
	}

	protected int adapt(int hash) {
		return hash;
	}

	protected long adapt(long hash) {
		return hash;
	}
	
	protected BigInteger adapt(BigInteger hash) {
		return hash;
	}
	
	private int[] adapt(int[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = adapt(array[i]);
		}
		return array;
	}

	private long[] adapt(long[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = adapt(array[i]);
		}
		return array;
	}

	private BigInteger[] adapt(BigInteger[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = adapt(array[i]);
		}
		return array;
	}

}
