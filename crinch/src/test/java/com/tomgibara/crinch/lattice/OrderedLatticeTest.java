package com.tomgibara.crinch.lattice;

import junit.framework.TestCase;

public class OrderedLatticeTest extends TestCase {

	public void testBounded() {
		OrderedLattice<Integer> lattice = new OrderedLattice<Integer>();
		final Lattice<Integer> bounded = lattice.bounded(5, 2);
		assertTrue(bounded.contains(3));
		assertTrue(bounded.contains(5));
		assertTrue(bounded.contains(2));
		assertFalse(bounded.contains(1));
		assertFalse(bounded.contains(6));
	}
	
}
