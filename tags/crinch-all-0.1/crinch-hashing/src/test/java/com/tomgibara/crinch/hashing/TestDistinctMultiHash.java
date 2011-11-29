package com.tomgibara.crinch.hashing;

import junit.framework.TestCase;

public class TestDistinctMultiHash extends TestCase {

	public void testObjectHash() {
		
		ObjectHash<Integer> hash = new ObjectHash<Integer>();
		DistinctMultiHash<Integer> multiHash = new DistinctMultiHash<Integer>(1000, 3, hash);
		int[] ints = new int[3];
		for (int i = 0; i < 100000; i++) {
			checkDistinct3(multiHash.hashAsInts(i, ints));
		}
		
	}
	
	private void checkDistinct3(int[] ints) {
		if (ints.length != 3) throw new IllegalArgumentException();
		if (ints[0] == ints[1]) fail("duplicate at 0 and 1");
		if (ints[1] == ints[2]) fail("duplicate at 1 and 2");
		if (ints[2] == ints[0]) fail("duplicate at 0 and 2");
	}
	
}
