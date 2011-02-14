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
package com.tomgibara.crinch.math;

import static java.math.BigInteger.valueOf;

import java.math.BigInteger;

public class Combinators {

	private static final BigInteger MAX_LONG_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
	
	/**
	 * Create a new combinator that returns combinations of k items selected
	 * from n elements, without regard to order. The upfront costs of creating a
	 * new combinator may be significant for large values of n and k.
	 * 
	 * @param n
	 *            the number of elements chosen from
	 * @param k
	 *            the number of elements chosen
	 * 
	 * @return a new combinator
	 */
	
	public static Combinator newCombinator(int n, int k) {
		if (k < 1) throw new IllegalArgumentException();
		if (k > n || n == Integer.MAX_VALUE) throw new IllegalArgumentException();
		//TODO slight inefficiency here, (n,k) effectively gets computed twice
		return chooseAsBigInt(n, k).compareTo(MAX_LONG_VALUE) > 0 ?
			new BigIntCombinator(n, k) : new LongCombinator(n, k);
	}

	/**
	 * <p>
	 * Creates a new packed combinator which returns the same elements as a
	 * regular {@link Combinator} with the option of packing them into a
	 * single long. This can eliminates the need to allocate int arrays to
	 * store tuples and may provide improved performance.
	 * </p>
	 * 
	 * <p>
	 * For the packing to be possible, n cannot equal {@link Integer.MAX_VALUE}
	 * and k multiplied by the number of binary digits in n, cannot exceed 64. 
	 * </p>
	 * 
	 * @param n
	 *            the number of elements chosen from
	 * @param k
	 *            the number of elements chosen
	 * 
	 * @return a new packed combinator
	 */
	
	public static PackedCombinator newPackedCombinator(int n, int k) {
		if (k < 1) throw new IllegalArgumentException();
		if (k > n || n == Integer.MAX_VALUE) throw new IllegalArgumentException();
		return new LongCombinator(n, k, true);
	}
	
	/**
	 * Computes the number of ways that k items can be selected from a set of n
	 * elements. This method provides much better performance than
	 * {@link #chooseAsBigInt(int, int)} but may overflow.
	 * 
	 * @param n
	 *            the number of elements chosen from
	 * @param k
	 *            the number of elements chosen
	 * @return n choose k
	 */
	
	public static long chooseAsLong(int n, int k) {
		if (n < 0) throw new IllegalArgumentException(); 
		if (k < 0) throw new IllegalArgumentException(); 
		if (n < k) return 0;
		if (k == n || k == 0) return 1;
		
		final long delta, max;
		if (k < n - k) {
			delta = n - k;
			max = k;
		} else {
			delta = k;
			max = n - k;
		}
		
		long c = delta + 1;
		for (long i = 2; i <= max; i++) {
			 c  = c * (delta + i) / i;
		}
		
		return c;
	}
	
	/**
	 * Computes the number of ways that k items can be selected from a set of n
	 * elements.
	 * 
	 * @param n
	 *            the number of elements chosen from
	 * @param k
	 *            the number of elements chosen
	 * @return n choose k
	 */
	
	public static BigInteger chooseAsBigInt(int n, int k) {
		if (n < 0) throw new IllegalArgumentException(); 
		if (k < 0) throw new IllegalArgumentException(); 
		if (n < k) return BigInteger.ZERO;
		if (k == n || k == 0) return BigInteger.ONE;
		
		final long delta, max;
		if (k < n - k) {
			delta = n - k;
			max = k;
		} else {
			delta = k;
			max = n - k;
		}
		
		BigInteger c = valueOf(delta + 1);
		for (long i = 2; i <= max; i++) {
			 c  = c.multiply(BigInteger.valueOf(delta + i)).divide(valueOf(i));
		}
		
		return c;
	}
	

	
	private Combinators() {}
	
}
