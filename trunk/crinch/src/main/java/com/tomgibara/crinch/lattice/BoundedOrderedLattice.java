package com.tomgibara.crinch.lattice;

import java.util.Comparator;

public class BoundedOrderedLattice<E> extends OrderedLattice<E> implements BoundedLattice<E> {

	private final E top;
	private final E bottom;
	
	public BoundedOrderedLattice(E top, E bottom) {
		this(top, bottom, null);
	}
	
	public BoundedOrderedLattice(E top, E bottom, Comparator<E> comparator) {
		super(comparator);
		checkOrder(top, bottom);
		this.top = top;
		this.bottom = bottom;
	}
	
	@Override
	public E getBottom() {
		return bottom;
	}
	
	@Override
	public E getTop() {
		return top;
	}
	
	public boolean contains(E e) {
		return (top == null || compare(e, top) <= 0) && (bottom == null || compare(bottom, e) <= 0);
	};
	
	private void checkOrder(E a, E b) {
		if (a == null || b == null) return;
		if (compare(a, b) < 0) throw new IllegalArgumentException();
	}
	
	private void checkBounds(E a) {
		if (top != null) checkOrder(top, a);
		if (bottom != null) checkOrder(a, bottom);
	}
	
}
