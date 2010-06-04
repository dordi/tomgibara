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