/*
 * Copyright 2010 Tom Gibara
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
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.hashing.MultiHash;

/**
 * <p>
 * A straightforward {@link BloomFilter} implementation that persists live state
 * in a {@link BitVector}.
 * </p>
 * 
 * @author Tom Gibara
 * 
 * @param <E>
 *            the type of element contained in the filter
 */

public class BasicBloomFilter<E> extends AbstractBloomFilter<E> implements Cloneable {

	// fields
	
	private final MultiHash<? super E> multiHash;
	private final int hashCount;
	private final int[] hashes; 
	private final BitVector bits;
	private final BitVector publicBits;
	
	// constructors

	/**
	 * Constructs a {@link BasicBloomFilter} with the specified multi-hash and
	 * hash count. The capacity of the filter will be determined by the size of
	 * the hash range.
	 * 
	 * @param multiHash
	 *            generates hashes for elements added to the filter
	 * @param hashCount
	 *            the number hashes generated for each element
	 * @throws IllegalArgumentException
	 *             if the hashCount is less than 1, the multiHash is null, its
	 *             maximum multiplicity is exceeded by the hashCount or if the
	 *             hash range is too large to be accommodated by a
	 *             {@link BitVector}
	 */
	
	public BasicBloomFilter(MultiHash<? super E> multiHash, int hashCount) {
		this(null, multiHash, hashCount);
	}

	/**
	 * Constructs a {@link BasicBloomFilter} with the specified multi-hash and
	 * hash count. If a {@link BitVector} is supplied with a capacity that is
	 * inferior to the range of the {@link MultiHash}, a range-adjusted hash
	 * will be used that matches the capacity.
	 * 
	 * @param bits
	 *            a {@link BitVector} that will store the state of the filter,
	 *            or null
	 * @param multiHash
	 *            generates hashes for elements added to the filter
	 * @param hashCount
	 *            the number hashes generated for each element
	 * @throws IllegalArgumentException
	 *             if the hashCount is less than 1, the multiHash is null, its
	 *             maximum multiplicity is exceeded by the hashCount or if the
	 *             hash range is too large to be accommodated by a
	 *             {@link BitVector} or if the supplied {@link BitVector} is
	 *             immutable
	 */
	
	public BasicBloomFilter(BitVector bits, MultiHash<? super E> multiHash, int hashCount) {
		if (multiHash == null) throw new IllegalArgumentException("null multiHash");
		if (hashCount < 1) throw new IllegalArgumentException("hashCount not positive");
		if (multiHash.getMaxMultiplicity() < hashCount) throw new IllegalArgumentException("hashCount exceeds maximum hash multiplicity");
		
		if (bits != null) { // adapt the multiHash to match bits size (if possible)
			if (!bits.isMutable()) throw new IllegalArgumentException("bits not mutable");
			final int bitSize = bits.size();
			multiHash = Hashes.rangeAdjust(new HashRange(0, bitSize - 1), multiHash);
		} else { // ensure that the multiHash is small enough fit into bits
			final HashRange range = multiHash.getRange();
			if (range == null) throw new IllegalArgumentException("null multiHash range");
			if (!range.isIntBounded()) throw new IllegalArgumentException("multiHash not int bounded");
			multiHash = Hashes.rangeAdjust(range.zeroBased(), multiHash);
		}
		
		this.multiHash = multiHash;
		this.hashCount = hashCount;
		hashes = new int[hashCount];
		this.bits = bits == null ? new BitVector(multiHash.getRange().getSize().intValue()) : bits.alignedCopy(true);
		publicBits = this.bits.immutableView();
	}
	
	private BasicBloomFilter(BasicBloomFilter<E> that) {
		this.multiHash = that.multiHash;
		this.hashCount = that.hashCount;
		hashes = new int[hashCount];
		this.bits = that.bits.copy();
		publicBits = this.bits.immutableView();
	}
	
	// bloom filter methods
	
	@Override
	public void clear() {
		bits.set(false);
	}

	@Override
	public boolean addAll(BloomFilter<? extends E> filter) {
		checkCompatible(filter);
		boolean contains = bits.testContains(filter.getBitVector());
		if (contains) return false;
		bits.orVector(filter.getBitVector());
		return true;
	}
	
	@Override
	public boolean add(E element) {
		final int[] hashes = multiHash.hashAsInts(element, this.hashes);
		boolean mutated = false;
		for (int i = 0; i < hashCount; i++) {
			final int hash = hashes[i];
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
	public BitVector getBitVector() {
		return publicBits;
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
	public BasicBloomFilter<E> clone() {
		return new BasicBloomFilter<E>(this);
	}

}
