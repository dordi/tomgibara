package com.tomgibara.crinch.record.util;

import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.collections.BasicBloomFilter;
import com.tomgibara.crinch.collections.BloomFilter;
import com.tomgibara.crinch.hashing.Hash;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.Murmur3_32Hash;
import com.tomgibara.crinch.hashing.IntegerMultiHash;
import com.tomgibara.crinch.hashing.ObjectHash;

//TODO want to support primitive collections
public class UniquenessChecker<T> {

	private static final int PHASE_PRE_PASS_1 = 0;
	private static final int PHASE_IN_PASS_1 = 1;
	private static final int PHASE_BETWEEN_PASSES = 2;
	private static final int PHASE_IN_PASS_2 = 3;
	private static final int PHASE_POST_PASS_2 = 4;
	
	private static final double LOG_2 = Math.log(2);
	private static final int BLOOM_MIN_SIZE = 256;

	private final int hashCount;
	private final MultiHash<T> multiHash;

	private int phase = PHASE_PRE_PASS_1;
	private Boolean unique = null;

	private BloomFilter<T> filter = null;
	private Set<T> candidates = null;
	private Set<T> witnesses = null;
	
	public UniquenessChecker(long expectedObjectCount, double averageObjectSizeInBytes) {
		this(expectedObjectCount, averageObjectSizeInBytes, null);
	}
	
	public UniquenessChecker(long expectedObjectCount, double averageObjectSizeInBytes, HashSource<T> hashSource) {
		if (expectedObjectCount < 0) throw new IllegalArgumentException("null expectedObjectCount");
		if (averageObjectSizeInBytes <= 0.0) throw new IllegalArgumentException("non-positive averageObjectSizeInBytes");
		
		double bitsPerObject = 8.0 * averageObjectSizeInBytes;
		double optimalBloomSize = expectedObjectCount * Math.log( bitsPerObject * LOG_2 * LOG_2 ) / LOG_2;
		int bloomSize = Math.max(BLOOM_MIN_SIZE, (int) Math.min(Integer.MAX_VALUE, optimalBloomSize));
		hashCount = Math.max(1, Math.round( (float) LOG_2 * bloomSize / expectedObjectCount) );
		final Hash<T> hash;
		if (hashSource == null) {
			hash = new ObjectHash<T>();
		} else {
			//TODO would be nice to make this configurable somehow
			hash = new Murmur3_32Hash<T>(hashSource);
		}
		multiHash = new IntegerMultiHash<T>(hash, bloomSize - 1);
	}

	public boolean isUniquenessDetermined() {
		return unique != null;
	}
	
	public void beginPass() {
		switch (phase) {
		case PHASE_PRE_PASS_1:
			filter = new BasicBloomFilter<T>(multiHash, hashCount);
			candidates = new HashSet<T>();
			phase = PHASE_IN_PASS_1;
			break;
		case PHASE_BETWEEN_PASSES:
			witnesses = new HashSet<T>();
			phase = PHASE_IN_PASS_2;
			break;
			default: throw new IllegalStateException();
		}
	}
	
	// returns false if pass can end early
	public boolean add(T value) {
		if (unique != null) return false;
		switch (phase) {
		case PHASE_IN_PASS_1: {
			if (filter.add(value) || candidates.add(value)) return true;
			// we've found a definite dupe, release memory
			candidates = null;
			filter = null;
			unique = false;
			return false;
		}
		case PHASE_IN_PASS_2: {
			if (!candidates.contains(value) || witnesses.add(value)) return true;
			// we've found a definite dupe, release memory
			candidates = null;
			witnesses = null;
			unique = false;
			return false;
		}
			default: throw new IllegalStateException("Not in pass");
		}
	}
	
	public void endPass() {
		switch (phase) {
		case PHASE_IN_PASS_1:
			if (unique == null && candidates.isEmpty()) unique = true;
			filter = null;
			phase = PHASE_BETWEEN_PASSES;
			break;
		case PHASE_IN_PASS_2:
			if (unique == null) unique = true;
			candidates = null;
			witnesses = null;
			phase = PHASE_POST_PASS_2;
			break;
			default: throw new IllegalStateException();
		}
	}
	
	public boolean isUnique() {
		if (unique == null) throw new IllegalStateException("Uniqueness not determined");
		return unique;
	}
	
}
