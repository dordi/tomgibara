package com.tomgibara.crinch.lattice;

import java.util.Comparator;

public class OrderedLattice<E> implements Lattice<E> {

	private final Comparator<? super E> comparator;
	private final E top;
	private final E bottom;
	

	public OrderedLattice() {
		this(null, null);
	}

	public OrderedLattice(Comparator<? super E> comparator) {
		this(null, null, comparator);
	}

	public OrderedLattice(E top, E bottom) {
		if (top != null && bottom != null && compare(top, bottom) < 0) throw new IllegalArgumentException();
		this.top = top;
		this.bottom = bottom;
		comparator = null;
	}
	
	public OrderedLattice(E top, E bottom, Comparator<? super E> comparator) {
		if (comparator == null) throw new NullPointerException();
		if (top != null && bottom != null && compare(top, bottom) < 0) throw new IllegalArgumentException();
		this.top = top;
		this.bottom = bottom;
		this.comparator = comparator;
	}

	public Comparator<? super E> getComparator() {
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
		if (e == null) throw new IllegalArgumentException();
		return (top == null || compare(e, top) <= 0) && (bottom == null || compare(bottom, e) <= 0);
	};
	
	public E join(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return compare(a,b) >= 0 ? a : b;
	};

	public E meet(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return compare(a,b) <= 0 ? a : b;
	};
	
	public Lattice<E> boundedAbove(E top) {
		final int cmp = this.top == null ? 1 : compare(this.top, top);
		if (cmp < 0) throw new IllegalArgumentException();
		return cmp == 0 ? this : new OrderedLattice<E>(top, bottom);
	}
	
	public Lattice<E> boundedBelow(E bottom) {
		final int cmp = this.bottom == null ? 1 : compare(bottom, this.bottom);
		if (cmp < 0) throw new IllegalArgumentException();
		return cmp == 0 ? this : new OrderedLattice<E>(top, bottom);
	};
	
	public Lattice<E> bounded(E top, E bottom) {
		final int cmpA = this.top == null ? 1 : compare(this.top, top);
		if (cmpA < 0) throw new IllegalArgumentException();
		final int cmpB = this.bottom == null ? 1 : compare(bottom, this.bottom);
		if (cmpB < 0) throw new IllegalArgumentException();
		return cmpA == 0 && cmpB == 0 ? this : new OrderedLattice<E>(top, bottom);
	};
	
	private int compare(E a, E b) {
		if (comparator == null) {
			return ((Comparable<? super E>)a).compareTo(b);
		} else {
			return comparator.compare(a, b);
		}
	}

	private void checkBounds(E a) {
		if (a == null) throw new IllegalArgumentException();
		if (top != null) checkOrder(top, a);
		if (bottom != null) checkOrder(a, bottom);
	}
	
	private void checkOrder(E a, E b) {
		if (a != null && compare(a, b) < 0) throw new IllegalArgumentException();
	}
	
}
