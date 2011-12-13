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
import com.tomgibara.crinch.hashing.MultiHash;

/**
 * <p>
 * A Bloom filter. See <a href="http://en.wikipedia.org/wiki/Bloom_filter">the
 * Wikipedia article on Bloom filters</a>.
 * </p>
 * 
 * <p>
 * Operations involving two <code>BloomFilter</code>s are generally only defined
 * for compatible instances. Two <code>BloomFilter</code> instances are
 * compatible if they have the same capacity, hashCount and equal multiHashes.
 * </p>
 * 
 * <p>
 * Two <code>BloomFilter</code>s are equal if they are compatible and their
 * bitVectors are equal.
 * </p>
 * 
 * @author Tom Gibara
 * 
 * @param <E>
 *            the type of element stored in the bloom filter
 */

//see http://code.google.com/p/guava-libraries/issues/detail?id=12
public interface BloomFilter<E> {

	/**
	 * Whether the Bloom filter might contain the specified element. As per the
	 * characteristics of Bloom filters this method may return true for elements
	 * that were never explicitly added to the filter, though it will never
	 * return false for an element that was.
	 * 
	 * @param element
	 *            an element that could be in the filter
	 * @return false only if the element was never added to the filter
	 */
	
	boolean mightContain(E element);
	
	/**
	 * An estimate of the probability that {@link #mightContain(Object)} will
	 * return true for an element that was not added to the filter. This
	 * estimate will change as the number of elements in the filter increases
	 * and is based on an assumption that hashing is optimal.
	 * 
	 * @return a probability between 0 and 1 inclusive
	 */
	
	double getFalsePositiveProbability();

	/**
	 * Adds an element to the filter. Null elements should be supported if the
	 * underlying {@link MultiHash} supports nulls.
	 * 
	 * @param newElement
	 *            the element to add
	 * @return true if the state of the bloom filter was modified by the
	 *         operation, false otherwise
	 */
	
	boolean add(E newElement);

	/**
	 * Adds every element of the supplied iterable to the filter.
	 * 
	 * @param elements
	 *            the elements to add, not null
	 * @return true if the state of the bloom filter was modified by the
	 *         operation, false otherwise
	 * @throws IllegalArgumentException
	 *             if a null iterable is supplied
	 */
	
	boolean addAll(Iterable<? extends E> elements) throws IllegalArgumentException;

	/**
	 * Whether the filter is empty.
	 * 
	 * @return true if the filter has never had an element added to it, false
	 *         otherwise
	 */
	
	boolean isEmpty();

	/**
	 * Removes all elements from the filter.
	 */
	
	void clear();
	
	/**
	 * Whether the Bloom filter might contain all the elements in the supplied
	 * iterable.
	 * 
	 * @param elements
	 *            elements that may be contained in the filter
	 * @return true if the filter might contain every element of the iterable,
	 *         false otherwise
	 * @throws IllegalArgumentException
	 *             if a null iterable is supplied
	 */
	
	boolean mightContainAll(Iterable<? extends E> elements) throws IllegalArgumentException;
	
	/**
	 * Whether the Bloom filter contains all of the elements contained in
	 * another compatible bloom filter
	 * 
	 * @param filter
	 *            a compatible Bloom filter
	 * @return true if every element of the compatible filter is necessarily
	 *         contained in this filter, false otherwise
	 * @throws IllegalArgumentException
	 *             if the supplied filter is null or not compatible with this
	 *             filter
	 */
	
	boolean containsAll(BloomFilter<?> filter) throws IllegalArgumentException;

	/**
	 * Adds all of the elements of a compatible bloom filter.
	 * 
	 * @param filter
	 *            a compatible Bloom filter
	 * @return true if the state of the bloom filter was modified by the
	 *         operation, false otherwise
	 * @throws IllegalArgumentException
	 *             if the supplied filter is null or not compatible with this
	 *             filter
	 */
	
	boolean addAll(BloomFilter<? extends E> filter) throws IllegalArgumentException;

	/**
	 * The number of bits in the bloom filter. This value will match the length
	 * of the {@link BitVector} returned by {@link #getBitVector()}.
	 * 
	 * @return the number of bits in the filter, always positive
	 */

	int getCapacity();

	/**
	 * The number of hashes used to mark bits in the Bloom filter.
	 * 
	 * @return the hash count, always positive
	 */
	
	int getHashCount();
	
	/**
	 * The multi-hash that generates hash values for this Bloom filter.
	 * 
	 * @return a {@link MultiHash} instance, never null
	 */
	
	MultiHash<? super E> getMultiHash();
	
	/**
	 * The bits of the Bloom filter. The returned {@link BitVector} may be live,
	 * or may be a snapshot, there's no guarantee. Use {@link BitVector#copy()}
	 * if you must have a guaranteed static snapshot.
	 * 
	 * @return a {@link BitVector} that contains the state of the filter, never
	 *         null
	 */
	
	BitVector getBitVector();
	
}
