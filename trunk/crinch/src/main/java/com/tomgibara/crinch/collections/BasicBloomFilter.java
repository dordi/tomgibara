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
package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashList;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.hashing.MultiHash;


public class BasicBloomFilter<E> implements BloomFilter<E>, Cloneable {

	// fields
	
	private final MultiHash<? super E> multiHash;
	private final int hashCount;
	private final BitVector bits;
	private BitVector publicBits = null;
	
	// constructors
	
	public BasicBloomFilter(MultiHash<? super E> multiHash, int hashCount) {
		this(null, multiHash, hashCount);
	}

	public BasicBloomFilter(BitVector bits, MultiHash<? super E> multiHash, int hashCount) {
		if (multiHash == null) throw new IllegalArgumentException("null multiHash");
		if (hashCount < 1) throw new IllegalArgumentException("hashCount not positive");
		if (multiHash.getMaxMultiplicity() < hashCount) throw new IllegalArgumentException("hashCount exceeds maximum hash multiplicity");
		
		if (bits != null) { // adapt the multiHash to match bits size (if possible)
			final int bitSize = bits.size();
			multiHash = Hashes.rangeAdjust(new HashRange(0, bitSize - 1), multiHash);
		} else { // ensure that the multiHash is small enough fit into bits
			final HashRange range = multiHash.getRange();
			if (range == null) throw new IllegalArgumentException("null multiHash range");
			if (!range.isIntRange()) throw new IllegalArgumentException("multiHash not int ranged");
			multiHash = Hashes.rangeAdjust(range.zeroBased(), multiHash);
		}
		
		this.multiHash = multiHash;
		this.hashCount = hashCount;
		this.bits = bits == null ? new BitVector(multiHash.getRange().getSize().intValue()) : bits.alignedCopy(true);
	}
	
	private BasicBloomFilter(BasicBloomFilter<E> that) {
		this.multiHash = that.multiHash;
		this.hashCount = that.hashCount;
		this.bits = that.bits.copy();
	}
	
	// bloom filter methods
	
	@Override
	public boolean isEmpty() {
		return bits.isAllZeros();
	}
	
	@Override
	public double getFalsePositiveProbability() {
		return Math.pow( (double) bits.countOnes() / bits.size(), hashCount);
	}
	
	@Override
	public int getCapacity() {
		return bits.size();
	}
	
	@Override
	public boolean addAll(Iterable<? extends E> elements) {
		boolean mutated = false;
		for (E element : elements) if ( add(element) ) mutated = true;
		return mutated;
	}

	@Override
	public boolean mightContainAll(Iterable<? extends E> elements) {
		for (E element : elements) if (!mightContain(element)) return false;
		return true;
	}
	
	@Override
	public void clear() {
		bits.set(false);
	}

	@Override
	public boolean containsAll(BloomFilter<?> filter) {
		return false;
	}

	@Override
	public boolean addAll(BloomFilter<? extends E> filter) {
		checkCompatible(filter);
		boolean contains = bits.testContains(filter.getBits());
		if (contains) return false;
		bits.orVector(filter.getBits());
		return true;
	}
	
	@Override
	public boolean mightContain(E element) {
		final HashList hashList = multiHash.hashAsList(element, hashCount);
		for (int i = 0; i < hashCount; i++) {
			if (!bits.getBit( hashList.getAsInt(i) )) return false;
		}
		return true;
	}
	
	@Override
	public boolean add(E element) {
		final HashList hashList = multiHash.hashAsList(element, hashCount);
		boolean mutated = false;
		for (int i = 0; i < hashCount; i++) {
			final int hash = hashList.getAsInt(i);
			if (mutated) {
				bits.setBit(hash, true);
			} else if (!bits.getBit(hash)) {
				bits.setBit(hash, true);
				mutated = true;
			}
		}
		return mutated;
	}
	
	@Override
	public BitVector getBits() {
		return publicBits == null ? publicBits = bits.immutableView() : publicBits;
	}
	
	@Override
	public int getHashCount() {
		return hashCount;
	}
	
	@Override
	public MultiHash<? super E> getMultiHash() {
		return multiHash;
	}
	
	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BloomFilter<?>)) return false;
		final BloomFilter<?> that = (BloomFilter<?>) obj;
		if (this.hashCount != that.getHashCount()) return false;
		if (!this.getMultiHash().equals(that.getMultiHash())) return false;
		if (!this.bits.equals(that.getBits())) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return bits.hashCode();
	}
	
	@Override
	public String toString() {
		return bits.toString();
	}
	
	@Override
	public BasicBloomFilter<E> clone() {
		return new BasicBloomFilter<E>(this);
	}

	// private methods
	
	private void checkCompatible(BloomFilter<?> filter) {
		if (this.hashCount != filter.getHashCount()) throw new IllegalArgumentException("Incompatible filter, hashCount was " + filter.getHashCount() +", expected " + hashCount);
		if (!this.getMultiHash().equals(filter.getMultiHash())) throw new IllegalArgumentException("Incompatible filter, multiHashes were not equal");
	}
	
}
