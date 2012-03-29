/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.math;

import static com.tomgibara.crinch.math.Combinators.chooseAsLong;
import static java.math.BigInteger.valueOf;
import junit.framework.TestCase;

public class CombinatorsTest extends TestCase {

	public void testChooseAsLong() {
		assertEquals(1, chooseAsLong(0, 0));
		assertEquals(1, chooseAsLong(1, 1));
		assertEquals(1, chooseAsLong(1, 0));
		assertEquals(1, chooseAsLong(2, 0));
		assertEquals(2, chooseAsLong(2, 1));
		assertEquals(1, chooseAsLong(2, 2));
		assertEquals(10 * 9 * 8 * 7 / 4 / 3 / 2 / 1, Combinators.chooseAsLong(10, 4));
	}
	
	public void testSymmetry() {
		for (int n = 0; n < 20; n++) {
			for (int k = 0; k <= n; k++) {
				assertEquals(chooseAsLong(n, k), chooseAsLong(n, n - k));
			}
		}
	}

	public void testConsistency() {
		for (int n = 0; n < 20; n++) {
			for (int k = 0; k <= n; k++) {
				assertEquals(valueOf(chooseAsLong(n, k)), Combinators.chooseAsBigInt(n, k));
			}
		}
	}

	public void testValidPackings() {
		Combinators.newPackedCombinator(65536, 4);
		Combinators.newPackedCombinator(256, 8);
		Combinators.newPackedCombinator(16, 16);
		Combinators.newPackedCombinator(1, 1);
	}

	public void testInvalidPackings() {
		try {
			Combinators.newPackedCombinator(65537, 4);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			Combinators.newPackedCombinator(257, 8);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			Combinators.newPackedCombinator(65536, 5);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			Combinators.newPackedCombinator(256, 9);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

}
