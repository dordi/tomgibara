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

import java.math.BigInteger;
import java.util.Random;

class LongCombinator extends AbstractCombinator implements PackedCombinator {

	private final int n;
	private final int k;
	private final long longSize;
	private final BigInteger size;
	private final int nBits;
	private final long nMask;
	
	private final long[][] cs;
	
	//relies on Combinators to validate arguments
	LongCombinator(int n, int k, boolean packed) {
		this.n = n;
		this.k = k;
		cs = new long[k + 1][n + 1];
		for (int i = 0; i <= k; i++) {
			for (int j = 0; j <= n; j++) {
				cs[i][j] = Combinators.chooseAsLong(j, i);
			}
		}
		longSize = cs[k][n] /* choose(n, k) */;
		size = BigInteger.valueOf(longSize);
		if (packed) {
			nMask = (n == 1 ? 1 : (Long.highestOneBit(n - 1) << 1)) - 1;
			nBits = Long.bitCount(nMask);
			if (nBits * k > 64) throw new IllegalArgumentException("Too many bits for packing");
		} else {
			nBits = -1;
			nMask = -1L;
		}
	}

	LongCombinator(int n, int k) {
		this(n, k, false);
	}

	// combinator methods
	
	@Override
	public int getElementCount() {
		return n;
	}

	@Override
	public int getTupleLength() {
		return k;
	}
	
	@Override
	public BigInteger size() {
		return size;
	}
	
	@Override
	public int[] getCombination(BigInteger m, int[] as) throws IndexOutOfBoundsException, IllegalArgumentException {
		if (m.signum() < 0) throw new IndexOutOfBoundsException();
		if (m.compareTo(size) >= 0) throw new IndexOutOfBoundsException();
		if (as.length < k) throw new IllegalArgumentException();
		return getCombinationImpl(m.longValue(), as);
	}
	
	@Override
	public int[] getCombination(long m, int[] as) {
		if (m < 0) throw new IndexOutOfBoundsException();
		if (m >= longSize) throw new IndexOutOfBoundsException();
		if (as.length < k) throw new IllegalArgumentException();
		return getCombinationImpl(m, as);
	}
	
	// packed methods
	
	@Override
	public int getBitsPerElement() {
		return nBits;
	}
	
	@Override
	public long getPackedCombination(long m) throws IllegalStateException, IndexOutOfBoundsException {
		final long[][] cs = this.cs;
		if (m < 0) throw new IndexOutOfBoundsException();
		if (m >= longSize) throw new IndexOutOfBoundsException();
		int a = n;
		int b = k;
		long x = cs[b][a] /*choose(a, b)*/ - 1 - m;
		long as = 0L;
		
		for (int i = 0; i < k; i++) {
			a = largest(a, b, x);
			x -= cs[b][a] /* choose(a, b) */;
			as = (as << nBits) | (n - 1 - a);
			b --;
		}
		
		return as;
	}
	
	@Override
	public int getPackedElement(long packed, int i) throws IllegalArgumentException {
		if (i < 0 || i >= k) throw new IllegalArgumentException("invalid tuple index");
		return (int) ((packed >> ((k - i - 1) * nBits)) & nMask);
	}
	
	@Override
	public int[] getRandomCombination(Random random, int[] as) throws IllegalArgumentException {
		if (random == null) throw new IllegalArgumentException("null random");
		if (as.length < k) throw new IllegalArgumentException();
		//TODO could cache
		long mask = -1L >>> Long.numberOfLeadingZeros(longSize);
		while (true) {
			long r = random.nextLong() & mask;
			if (r < longSize) return getCombinationImpl(r, as);
		}
		
	}

	private int[] getCombinationImpl(long m, int[] as) {
		int a = n;
		int b = k;
		long x = cs[b][a] /*choose(a, b)*/ - 1 - m;
		
		for (int i = 0; i < as.length; i++) {
			a = largest(a, b, x);
			x -= cs[b][a] /* choose(a, b) */;
			as[i] = a;
			b --;
		}
		
		for (int i = 0; i < as.length; i++) {
			as[i] = n - 1 - as[i];
		}
		
		return as;
	}

	private int largest(int a, int b, long x) {
		int v;
		for (v = a - 1; cs[b][v] /*choose(v, b)*/ > x; v--);
		return v;
	}
	
}