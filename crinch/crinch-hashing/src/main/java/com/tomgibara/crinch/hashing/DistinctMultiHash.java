/*
 * Copyright 2011 Tom Gibara
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
package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

import com.tomgibara.crinch.math.Combinator;
import com.tomgibara.crinch.math.Combinators;

public class DistinctMultiHash<E> extends AbstractMultiHash<E> {

	public static BigInteger requiredHashSize(int max, int multiplicity) {
		return Combinators.chooseAsBigInt(max, multiplicity);
	}
	
	private final Combinator combinator;
	private final HashRange range;
	private final Hash<E> hash;
	private final boolean longSized;
	
	public DistinctMultiHash(int max, int multiplicity, Hash<E> hash) {
		if (max < 0) throw new IllegalArgumentException();
		if (multiplicity > max) throw new IllegalArgumentException();
		
		final Combinator combinator = Combinators.newCombinator(max, multiplicity);
		final HashRange range = new HashRange(BigInteger.ZERO, combinator.size().subtract(BigInteger.ONE));
		
		this.combinator = combinator;
		this.range = new HashRange(0, max);
		this.hash = Hashes.rangeAdjust(range, Hashes.asMultiHash(hash));
		longSized = range.isLongSized();
	}
	
	@Override
	public HashRange getRange() {
		return range;
	}
	
	@Override
	public int getMaxMultiplicity() {
		return combinator.getTupleLength();
	}

	@Override
	public int[] hashAsInts(E value, int[] array) {
		return longSized ?
			combinator.getCombination(hash.hashAsLong(value), array) :
			combinator.getCombination(hash.hashAsBigInt(value), array);
	}
	
	@Override
	public long[] hashAsLongs(E value, long[] array) {
		return copy(hashAsInts(value, array.length), array);
	}
	
	@Override
	public BigInteger[] hashAsBigInts(E value, BigInteger[] array) {
		return copy(hashAsInts(value, array.length), array);
	}
	
}
