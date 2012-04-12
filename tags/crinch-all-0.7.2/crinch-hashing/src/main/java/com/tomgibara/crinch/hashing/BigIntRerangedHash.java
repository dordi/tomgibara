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

class BigIntRerangedHash<T> extends RerangedHash<T> {

	public BigIntRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash, newRange);
	}

	@Override
	public int hashAsInt(T value) {
		return hashAsBigInt(value).intValue();
	}
	
	@Override
	public int[] hashAsInts(T value, int multiplicity) {
		BigInteger[] bigInts = multiHash.hashAsBigInts(value, multiplicity);
		int[] array = new int[bigInts.length];
		return AbstractMultiHash.copy(bigInts, array);
	}
	
	@Override
	public int[] hashAsInts(T value, int[] array) {
		BigInteger[] bigInts = multiHash.hashAsBigInts(value, new BigInteger[array.length]);
		return AbstractMultiHash.copy(bigInts, array);
	}
	
	@Override
	public long hashAsLong(T value) {
		return hashAsBigInt(value).longValue();
	}
	
	@Override
	public long[] hashAsLongs(T value, int multiplicity) {
		BigInteger[] bigInts = multiHash.hashAsBigInts(value, multiplicity);
		long[] array = new long[bigInts.length];
		return AbstractMultiHash.copy(bigInts, array);
	}
	
	@Override
	public long[] hashAsLongs(T value, long[] array) {
		BigInteger[] bigInts = multiHash.hashAsBigInts(value, new BigInteger[array.length]);
		return AbstractMultiHash.copy(bigInts, array);
	}
	
	@Override
	protected int adapt(int h) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected long adapt(long h) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected BigInteger adapt(BigInteger h) {
		h = h.subtract(bigOldMin);
		if (isSmaller) h = h.mod(bigNewSize);
		return h.add(bigNewMin);
	}

}