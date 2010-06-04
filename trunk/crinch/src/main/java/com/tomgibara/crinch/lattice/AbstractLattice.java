package com.tomgibara.crinch.lattice;

import com.tomgibara.crinch.poset.AbstractPartialOrder;

public abstract class AbstractLattice<E> extends AbstractPartialOrder<E> implements Lattice<E> {

	@Override
	public Lattice<E> bounded(E top, E bottom) {
		return boundedAbove(top).boundedBelow(bottom);
	}

	@Override
	public boolean equalInLattice(E e1, E e2) {
		if (!contains(e1) || !contains(e2)) throw new IllegalArgumentException();
		E m = meet(e1, e2);
		E j = join(e1, e2);
		return m.equals(j);
	}

	@Override
	public boolean isBounded() {
		return isBoundedBelow() && isBoundedAbove();
	}

	@Override
	public boolean isOrdered(E e1, E e2) {
		if (!contains(e1) || !contains(e2)) throw new IllegalArgumentException();
		return join(e1, e2).equals(e2);
	}

}
