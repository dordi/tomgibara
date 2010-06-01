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

public abstract class AdaptedMultiHash<T> implements MultiHash<T> {

	protected final MultiHash<T> multiHash;

	public AdaptedMultiHash(MultiHash<T> multiHash) {
		if (multiHash == null)
			throw new IllegalArgumentException("null multiHash");
		this.multiHash = multiHash;
	}

	@Override
	public HashRange getRange() {
		return multiHash.getRange();
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return adaptedBigIntHash(value);
	}

	@Override
	public int hashAsInt(T value) {
		return adaptedIntHash(value);
	}

	@Override
	public long hashAsLong(T value) {
		return adaptedLongHash(value);
	}

	@Override
	public int getMaxMultiplicity() {
		return multiHash.getMaxMultiplicity();
	}

	@Override
	public HashList hashAsList(T value, int multiplicity) {
		final HashList hashList = multiHash.hashAsList(value, multiplicity);
		return new AbstractHashList() {

			@Override
			public int size() {
				return hashList.size();
			}

			@Override
			public BigInteger get(int index) {
				return adaptedBigIntHash(hashList, index);
			}

			@Override
			public int getAsInt(int index) {
				return adaptedIntHash(hashList, index);
			}

			@Override
			public long getAsLong(int index) {
				return adaptedLongHash(hashList, index);
			}
		};
	}

	// protected methods

	protected abstract BigInteger adaptedBigIntHash(T value);

	protected abstract BigInteger adaptedBigIntHash(HashList list, int index);

	protected abstract int adaptedIntHash(T value);

	protected abstract int adaptedIntHash(HashList list, int index);

	protected abstract long adaptedLongHash(T value);

	protected abstract long adaptedLongHash(HashList list, int index);

}
