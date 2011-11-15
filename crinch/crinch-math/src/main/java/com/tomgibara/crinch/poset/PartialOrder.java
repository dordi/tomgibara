package com.tomgibara.crinch.poset;

public interface PartialOrder<E> {

	enum Comparison {

		EQUAL, // if a<=b and b<=a
		GREATER_THAN, // if b<=a but not a<=b
		LESS_THAN, // if a<=b but not b<=a
		INCOMPARABLE; //if neither a<=b or b<=a

		//TODO how to describe this ??
		public Comparison combine(Comparison that) {
			if (this == that) return this;
			if (this == INCOMPARABLE) return this;
			if (that == INCOMPARABLE) return that;
			if (this == EQUAL) return that;
			if (that == EQUAL) return this;
			return INCOMPARABLE;
		}
		
	}

	boolean isOrdered(E a, E b);
	
	Comparison compare(E a, E b);
	
}
