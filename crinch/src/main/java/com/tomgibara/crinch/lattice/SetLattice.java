package com.tomgibara.crinch.lattice;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetLattice<E> implements Lattice<Set<E>> {

	private final Set<E> top;
	private final Set<E> bottom;
	
	public SetLattice(Set<E> top) {
		this(top, (Set<E>) Collections.emptySet());
	}
	
	public SetLattice(Set<E> top, Set<E> bottom) {
		if (top == null) throw new IllegalArgumentException();
		if (bottom == null) throw new IllegalArgumentException();
		if (!top.containsAll(bottom)) throw new IllegalArgumentException();
		this.top = top;
		this.bottom = bottom;
	}
	
	@Override
	public Set<E> getTop() {
		return top;
	}
	
	@Override
	public Set<E> getBottom() {
		return bottom;
	}
	
	@Override
	public boolean isBoundedAbove() {
		return true;
	}
	
	@Override
	public boolean isBoundedBelow() {
		return true;
	}
	
	@Override
	public boolean isBounded() {
		return true;
	}
	
	@Override
	public boolean contains(Set<E> e) {
		return top.containsAll(e) && e.containsAll(bottom);
	}
	
	@Override
	public Set<E> join(Set<E> a, Set<E> b) {
		int as = a.size();
		int bs = b.size();
		if (as > bs) {
			if (bs == 0) return a;
			HashSet<E> c = new HashSet<E>(a);
			c.addAll(b);
			return c;
		} else {
			if (as == 0) return b;
			HashSet<E> c = new HashSet<E>(b);
			c.addAll(a);
			return c;
		}
	}

	@Override
	public Set<E> meet(Set<E> a, Set<E> b) {
		int as = a.size();
		int bs = b.size();
		if (as > bs) {
			if (bs == 0) return b;
			HashSet<E> c = new HashSet<E>(a);
			a.removeAll(b);
			return c;
		} else {
			if (as == 0) return a;
			HashSet<E> c = new HashSet<E>(b);
			c.removeAll(a);
			return c;
		}
	}
	
	@Override
	public JoinSemiLattice<Set<E>> boundedAbove(Set<E> top) {
		if (!top.containsAll(bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	@Override
	public MeetSemiLattice<Set<E>> boundedBelow(Set<E> bottom) {
		if (!top.containsAll(bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	@Override
	public Lattice<Set<E>> boundedLattice(Set<E> top, Set<E> bottom) {
		if (!this.top.containsAll(top)) throw new IllegalArgumentException();
		if (!bottom.containsAll(this.bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	// object methods
	
	@Override
	public String toString() {
		return top + " to " + bottom;
	}
	
}
