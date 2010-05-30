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
	
	public void testMeet() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(2,3), lattice.meet(set(1,2,3), set(2,3,4)));
		assertEquals(set(1), lattice.meet(set(1,2,3), set(1)));
		assertEquals(set(1), lattice.meet(set(1), set(1,2,3)));
		assertEquals(set(), lattice.meet(set(1), set(2)));
		assertEquals(set(), lattice.meet(set(1), set()));
		assertEquals(set(), lattice.meet(set(), set(1)));
	}
	
	public void testJoin() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(1,2,3,4), lattice.join(set(1,2,3), set(2,3,4)));
		assertEquals(set(1,2,3), lattice.join(set(1,2,3), set(1)));
		assertEquals(set(1,2,3), lattice.join(set(1), set(1,2,3)));
		assertEquals(set(1,2), lattice.join(set(1), set(2)));
		assertEquals(set(1), lattice.join(set(1), set()));
		assertEquals(set(1), lattice.join(set(), set(1)));
	}
}
