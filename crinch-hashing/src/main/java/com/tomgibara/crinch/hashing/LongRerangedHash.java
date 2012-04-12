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

class LongRerangedHash<T> extends RerangedHash<T> {

	private final long lngOldMin;
	private final long lngNewMin;
	private final long lngNewSize;
	
	public LongRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash, newRange);
		lngOldMin = bigOldMin.longValue();
		lngNewMin = bigNewMin.longValue();
		lngNewSize = bigNewSize.longValue();
	}

	@Override
	public int hashAsInt(T value) {
		return (int) hashAsLong(value);
	}
	
	@Override
	public int[] hashAsInts(T value, int multiplicity) {
		long[] longs = multiHash.hashAsLongs(value, multiplicity);
		int[] array = new int[longs.length];
		return AbstractMultiHash.copy(longs, array);
	}
	
	@Override
	public int[] hashAsInts(T value, int[] array) {
		long[] longs = multiHash.hashAsLongs(value, new long[array.length]);
		return AbstractMultiHash.copy(longs, array);
	}
	
	@Override
	protected int adapt(int h) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected long adapt(long h) {
		h -= lngOldMin;
		if (isSmaller) h = h % lngNewSize;
		return h + lngNewMin;
	}

	@Override
	protected BigInteger adapt(BigInteger h) {
		h = h.subtract(bigOldMin);
		if (isSmaller) h = h.mod(bigNewSize);
		return h.add(bigNewMin);
	}
	
}
