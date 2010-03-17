package com.tomgibara.crinch.lattice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class SetLatticeTest extends TestCase {

	private static Set<Integer> set(Integer... integers) {
		return new HashSet<Integer>(Arrays.asList(integers));
	}
	
	public void testBounded() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(1,2,3,4), lattice.getTop());
		assertEquals(set(), lattice.getBottom());
		Lattice<Set<Integer>> bounded = lattice.bounded(set(1,2,4), set(2));
		assertTrue(bounded.contains(set(1,2)));
		assertTrue(bounded.contains(set(2,4)));
		assertFalse(bounded.contains(set(1,4)));
	}
	
}
