/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.bits.BitVector;

/**
 * <p>
 * A convenient base class for {@link BloomFilter} implementations.
 * </p>
 * 
 * @author Tom Gibara
 * 
 * @param <E>
 *            the type of element contained in the filter
 */

public abstract class AbstractBloomFilter<E> implements BloomFilter<E> {

	@Override
	public boolean isEmpty() {
		return getBitVector().isAllZeros();
	}
	
	@Override
	public double getFalsePositiveProbability() {
		return Math.pow( (double) getBitVector().countOnes() / getBitVector().size(), getHashCount());
	}
	
	@Override
	public int getCapacity() {
		return getBitVector().size();
	}
	
	@Override
	public boolean addAll(Iterable<? extends E> elements) {
		if (elements == null) throw new IllegalArgumentException("null elements");
		boolean mutated = false;
		for (E element : elements) if ( add(element) ) mutated = true;
		return mutated;
	}

	@Override
	public boolean mightContain(E element) {
		final int hashCount = getHashCount();
		final int[] hashes = getMultiHash().hashAsInts(element, hashCount);
		final BitVector bitVector = getBitVector();
		for (int i = 0; i < hashCount; i++) {
			if (!bitVector.getBit(hashes[i])) return false;
		}
		return true;
	}
	
	@Override
	public boolean mightContainAll(Iterable<? extends E> elements) {
		if (elements == null) throw new IllegalArgumentException("null elements");
		for (E element : elements) if (!mightContain(element)) return false;
		return true;
	}

	@Override
	public boolean containsAll(BloomFilter<?> filter) {
		checkCompatible(filter);
		return getBitVector().testContains(filter.getBitVector());
	}

	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BloomFilter<?>)) return false;
		final BloomFilter<?> that = (BloomFilter<?>) obj;
		if (this.getHashCount() != that.getHashCount()) return false;
		if (!this.getMultiHash().equals(that.getMultiHash())) return false;
		if (!this.getBitVector().equals(that.getBitVector())) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return getBitVector().hashCode();
	}
	
	@Override
	public String toString() {
		return getBitVector().toString();
	}

	// package scoped methods

	void checkCompatible(BloomFilter<?> that) {
		if (that == null) throw new IllegalArgumentException("null filter");
		if (this.getHashCount() != that.getHashCount()) throw new IllegalArgumentException("Incompatible filter, hashCount was " + that.getHashCount() +", expected " + this.getHashCount());
		if (!this.getMultiHash().equals(that.getMultiHash())) throw new IllegalArgumentException("Incompatible filter, multiHashes were not equal");
	}
	
}
