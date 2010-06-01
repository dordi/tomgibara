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
