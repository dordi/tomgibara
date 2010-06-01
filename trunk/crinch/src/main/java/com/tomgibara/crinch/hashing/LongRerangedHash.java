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

class LongRerangedHash<T> extends BigIntRerangedHash<T> {

	private final long oldMin;
	private final long newMin;
	private final long newSize;

	public LongRerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash, newRange);
		oldMin = oldRange.getMinimum().longValue();
		newMin = newRange.getMinimum().longValue();
		newSize = newRange.getSize().longValue();
	}

	@Override
	protected BigInteger adaptedBigIntHash(T value) {
		return BigInteger.valueOf(adaptedLongHash(value));
	}


	@Override
	protected BigInteger adaptedBigIntHash(HashList list, int index) {
		return BigInteger.valueOf(adaptedLongHash(list, index));
	}


	@Override
	protected int adaptedIntHash(T value) {
		return (int) adaptedLongHash(value);
	}


	@Override
	protected int adaptedIntHash(HashList list, int index) {
		return (int) adaptedIntHash(list, index);
	}


	@Override
	protected long adaptedLongHash(T value) {
		return reranged(multiHash.hashAsInt(value));
	}

	@Override
	protected long adaptedLongHash(HashList list, int index) {
		return reranged(list.getAsInt(index));
	}

	private long reranged(long h) {
		h -= oldMin;
		if (isSmaller) {
			return (h % newSize) + newMin;
		} else {
			return h + newMin;
		}
	}
	
}
