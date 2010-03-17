package com.tomgibara.crinch.lattice;

import java.util.Comparator;

public class OrderedLattice<E> implements Lattice<E> {

	private final Comparator<E> comparator;
	private final E top;
	private final E bottom;
	

	public OrderedLattice() {
		this(null, null);
	}

	public OrderedLattice(Comparator<E> comparator) {
		this(null, null, comparator);
	}

	public OrderedLattice(E top, E bottom) {
		checkOrder(top, bottom);
		this.top = top;
		this.bottom = bottom;
		comparator = null;
	}
	
	public OrderedLattice(E top, E bottom, Comparator<E> comparator) {
		if (comparator == null) throw new NullPointerException();
		checkOrder(top, bottom);
		this.top = top;
		this.bottom = bottom;
		this.comparator = comparator;
	}

	public Comparator<E> getComparator() {
		return comparator;
	}
	
	@Override
	public E getBottom() {
		return bottom;
	}
	
	@Override
	public E getTop() {
		return top;
	}

	@Override
	public boolean isBoundedAbove() {
		return top != null;
	}
	
	@Override
	public boolean isBoundedBelow() {
		return bottom != null;
	}

	@Override
	public boolean isBounded() {
		return top != null && bottom != null;
	}
	
	public boolean contains(E e) {
		return (top == null || compare(e, top) <= 0) && (bottom == null || compare(bottom, e) <= 0);
	};
	
	public E join(E a, E b) {
		return compare(a,b) >= 0 ? a : b;
	};

	public E meet(E a, E b) {
		return compare(a,b) <= 0 ? a : b;
	};
	
	public JoinSemiLattice<E> boundedJoinSemiLattice(E top) {
		return new OrderedLattice<E>(top, null);
	}
	
	public MeetSemiLattice<E> boundedMeetSemiLattice(E bottom) {
		return new OrderedLattice<E>(null, bottom);
	};
	
	public Lattice<E> boundedLattice(E top, E bottom) {
		return new OrderedLattice<E>(top, bottom);
	};
	
	private int compare(E a, E b) {
		if (comparator == null) {
			return ((Comparable<E>)a).compareTo(b);
		} else {
			return comparator.compare(a, b);
		}
	}

	private void checkOrder(E a, E b) {
		if (a == null || b == null) return;
		if (compare(a, b) < 0) throw new IllegalArgumentException();
	}
	
	private void checkBounds(E a) {
		if (top != null) checkOrder(top, a);
		if (bottom != null) checkOrder(a, bottom);
	}
	
}
