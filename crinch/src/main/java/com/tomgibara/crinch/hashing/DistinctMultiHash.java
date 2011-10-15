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
