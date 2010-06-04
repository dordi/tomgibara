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
package com.tomgibara.crinch.lattice;

import java.util.Comparator;

public class OrderedLattice<E> extends AbstractLattice<E> {

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
		if (top != null && bottom != null && comp(top, bottom) < 0) throw new IllegalArgumentException();
		this.top = top;
		this.bottom = bottom;
		comparator = null;
	}
	
	public OrderedLattice(E top, E bottom, Comparator<? super E> comparator) {
		if (comparator == null) throw new NullPointerException();
		if (top != null && bottom != null && comp(top, bottom) < 0) throw new IllegalArgumentException();
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
	
	@Override
	public boolean contains(E e) {
		if (e == null) throw new IllegalArgumentException();
		return (top == null || comp(e, top) <= 0) && (bottom == null || comp(bottom, e) <= 0);
	};
	
	@Override
	public E join(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return comp(a,b) >= 0 ? a : b;
	};

	@Override
	public E meet(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return comp(a,b) <= 0 ? a : b;
	};
	
	@Override
	public Lattice<E> boundedAbove(E top) {
		final int cmp = this.top == null ? 1 : comp(this.top, top);
		if (cmp < 0) throw new IllegalArgumentException();
		return cmp == 0 ? this : new OrderedLattice<E>(top, bottom);
	}
	
	@Override
	public Lattice<E> boundedBelow(E bottom) {
		final int cmp = this.bottom == null ? 1 : comp(bottom, this.bottom);
		if (cmp < 0) throw new IllegalArgumentException();
		return cmp == 0 ? this : new OrderedLattice<E>(top, bottom);
	};
	
	@Override
	public Lattice<E> bounded(E top, E bottom) {
		final int cmpA = this.top == null ? 1 : comp(this.top, top);
		if (cmpA < 0) throw new IllegalArgumentException();
		final int cmpB = this.bottom == null ? 1 : comp(bottom, this.bottom);
		if (cmpB < 0) throw new IllegalArgumentException();
		return cmpA == 0 && cmpB == 0 ? this : new OrderedLattice<E>(top, bottom);
	};
	
	@Override
	public boolean equalInLattice(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return comp(a, b) == 0;
	}
	
	@Override
	public boolean isOrdered(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		return comp(a, b) <= 0;
	}
	
	@Override
	public Comparison compare(E a, E b) {
		checkBounds(a);
		checkBounds(b);
		final int c = comp(a, b);
		if (c < 0) return Comparison.LESS_THAN;
		if (c > 0) return Comparison.GREATER_THAN;
		return Comparison.EQUAL;
	}
	
	private void checkBounds(E a) {
		if (a == null) throw new IllegalArgumentException();
		if (top != null) checkOrder(top, a);
		if (bottom != null) checkOrder(a, bottom);
	}
	
	private void checkOrder(E a, E b) {
		if (a != null && comp(a, b) < 0) throw new IllegalArgumentException();
	}
	
	private int comp(E a, E b) {
		if (comparator == null) {
			return ((Comparable<? super E>)a).compareTo(b);
		} else {
			return comparator.compare(a, b);
		}
	}

}
