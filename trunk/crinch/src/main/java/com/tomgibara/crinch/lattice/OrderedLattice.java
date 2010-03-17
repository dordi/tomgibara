package com.tomgibara.crinch.lattice;

import java.util.Comparator;

public class OrderedLattice<T> implements Lattice<T> {

	final Comparator<T> comparator;

	public OrderedLattice() {
		comparator = null;
	}

	public OrderedLattice(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public boolean contains(T e) {
		return true;
	};
	
	public T join(T a, T b) {
		return compare(a,b) >= 0 ? a : b;
	};

	public T meet(T a, T b) {
		return compare(a,b) <= 0 ? a : b;
	};
	
	public BoundedJoinSemiLattice<T> boundedJoinSemiLattice(T top) {
		return new BoundedOrderedLattice<T>(top, null);
	}
	
	public BoundedMeetSemiLattice<T> boundedMeetSemiLattice(T bottom) {
		return new BoundedOrderedLattice<T>(null, bottom);
	};
	
	public BoundedLattice<T> boundedLattice(T top, T bottom) {
		return new BoundedOrderedLattice<T>(top, bottom);
	};
	
	int compare(T a, T b) {
		if (comparator == null) {
			return ((Comparable<T>)a).compareTo(b);
		} else {
			return comparator.compare(a, b);
		}
	}
}
