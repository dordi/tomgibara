package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.Hash;

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
	
	Hash<? super E> getMultiHash();
	
	//may be live, or may be a snapshot - no guarantee, use BitVector.copy() if you need certainty
	BitVector getBits();
	
}
