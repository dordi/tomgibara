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

class BigIntCombinator extends AbstractCombinator {

	private final int n;
	private final int k;
	private final BigInteger size;
	
	private final BigInteger[][] cs;
	
	BigIntCombinator(int n, int k) {
		//relies on Combinators to validate arguments
		this.n = n;
		this.k = k;
		cs = new BigInteger[k + 1][n + 1];
		for (int i = 0; i <= k; i++) {
			for (int j = 0; j <= n; j++) {
				cs[i][j] = Combinators.chooseAsBigInt(j, i);
			}
		}
		size = cs[k][n] /* choose(n, k) */;
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
	public int[] getCombination(long m) throws IndexOutOfBoundsException, IllegalArgumentException {
		return getCombination(valueOf(m));
	}

	@Override
	public int[] getCombination(long m, int[] as) throws IndexOutOfBoundsException, IllegalArgumentException {
		return getCombination(valueOf(m), as);
	}
	
	@Override
	public int[] getCombination(BigInteger m) {
		return getCombination(m, new int[k]);
	}
	
	@Override
	public int[] getCombination(BigInteger m, int[] as) {
		final BigInteger[][] cs = this.cs;
		if (m.signum() < 0) throw new IndexOutOfBoundsException();
		if (m.compareTo(cs[k][n] /* choose(n, k) */) >= 0) throw new IndexOutOfBoundsException();
		if (as.length < k) throw new IllegalArgumentException();
		
		int a = n;
		int b = k;
		BigInteger x = (cs[b][a] /*choose(a, b)*/).subtract(BigInteger.ONE).subtract(m);
		
		for (int i = 0; i < as.length; i++) {
			a = largest(a, b, x);
			x = x.subtract(cs[b][a] /* choose(a, b) */);
			as[i] = a;
			b --;
		}
		
		for (int i = 0; i < as.length; i++) {
			as[i] = n - 1 - as[i];
		}
		
		return as;
	}
	
	private int largest(int a, int b, BigInteger x) {
		int v;
		for (v = a - 1; x.compareTo(cs[b][v] /*choose(v, b)*/) < 0; v--);
		return v;
	}
	
}