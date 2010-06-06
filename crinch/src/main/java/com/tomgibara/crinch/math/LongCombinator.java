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

class LongCombinator extends AbstractCombinator {

	private final int n;
	private final int k;
	private final BigInteger size;
	
	private final long[][] cs;
	
	//relies on Combinators to validate arguments
	LongCombinator(int n, int k) {
		this.n = n;
		this.k = k;
		cs = new long[k + 1][n + 1];
		for (int i = 0; i <= k; i++) {
			for (int j = 0; j <= n; j++) {
				cs[i][j] = Combinators.chooseAsLong(j, i);
			}
		}
		size = BigInteger.valueOf(cs[k][n] /* choose(n, k) */);
	}
	
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
	public int[] getCombination(BigInteger m) throws IndexOutOfBoundsException, IllegalArgumentException {
		return getCombination(m, new int[k]);
	}
	
	@Override
	public int[] getCombination(BigInteger m, int[] as) throws IndexOutOfBoundsException, IllegalArgumentException {
		if (m.signum() < 0) throw new IndexOutOfBoundsException();
		if (m.compareTo(BigInteger.valueOf(cs[k][n] /* choose(n, k) */)) >= 0) throw new IndexOutOfBoundsException();
		if (as.length < k) throw new IllegalArgumentException();
		return getCombinationImpl(m.longValue(), as);
	}
	
	@Override
	public int[] getCombination(long m) {
		return getCombination(m, new int[k]);
	}
	
	@Override
	public int[] getCombination(long m, int[] as) {
		final long[][] cs = this.cs;
		if (m < 0) throw new IndexOutOfBoundsException();
		if (m >= cs[k][n] /* choose(n, k) */) throw new IndexOutOfBoundsException();
		if (as.length < k) throw new IllegalArgumentException();
		return getCombinationImpl(m, as);
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