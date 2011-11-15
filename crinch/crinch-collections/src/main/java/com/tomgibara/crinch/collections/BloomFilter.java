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
import com.tomgibara.crinch.hashing.MultiHash;

//see http://code.google.com/p/guava-libraries/issues/detail?id=12
public interface BloomFilter<E> {

	boolean mightContain(E element);
	
	double getFalsePositiveProbability();
	
	boolean add(E newElement);
	
	boolean addAll(Iterable<? extends E> elements);
	
	boolean isEmpty();
	
	void clear();
	
	boolean mightContainAll(Iterable<? extends E> elements);
	
	boolean containsAll(BloomFilter<?> filter);
	
	boolean addAll(BloomFilter<? extends E> filter);

	int getCapacity();
	
	int getHashCount();
	
	MultiHash<? super E> getMultiHash();
	
	//may be live, or may be a snapshot - no guarantee, use BitVector.copy() if you need certainty
	BitVector getBitVector();
	
}
