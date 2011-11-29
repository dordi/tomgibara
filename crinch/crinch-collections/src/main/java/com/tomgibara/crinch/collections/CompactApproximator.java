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

import java.util.List;

import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.lattice.Lattice;

public interface CompactApproximator<K, V> {

	V put(K key, V value);

	V getSupremum(K key);
	
	boolean mightContain(K key);

	boolean mightContainAll(Iterable<? extends K> keys);
	
	boolean bounds(CompactApproximator<K, V> ca);
	
	CompactApproximator<K, V> boundedAbove(V upperBound);
	
	//bit true if corresponding value attains top
	BloomFilter<K> asBloomFilter();
	
	void clear();

	boolean isEmpty();

	Lattice<V> getLattice();
	
	int getCapacity();

	int getHashCount();

	MultiHash<? super K> getMultiHash();

	List<V> getValueList();
	
}