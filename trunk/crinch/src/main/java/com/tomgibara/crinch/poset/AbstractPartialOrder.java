package com.tomgibara.crinch.poset;

public abstract class AbstractPartialOrder<E> implements PartialOrder<E> {

	@Override
	public Comparison compare(E a, E b) {
		final boolean alteb = isOrdered(a, b);
		final boolean bltea = isOrdered(b, a);
		if (alteb) {
			return bltea ? Comparison.EQUAL : Comparison.LESS_THAN;
		} else {
			return bltea ? Comparison.GREATER_THAN : Comparison.INCOMPARABLE;
		}
	}
	
}
