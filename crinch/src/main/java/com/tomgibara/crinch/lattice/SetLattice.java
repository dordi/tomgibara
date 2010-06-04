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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//TODO consider whether unbounded sets could be supported
public class SetLattice<E> extends AbstractLattice<Set<E>> {

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
			c.retainAll(b);
			return c;
		} else {
			if (as == 0) return a;
			HashSet<E> c = new HashSet<E>(b);
			c.retainAll(a);
			return c;
		}
	}
	
	@Override
	public Lattice<Set<E>> boundedAbove(Set<E> top) {
		if (!top.containsAll(bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	@Override
	public Lattice<Set<E>> boundedBelow(Set<E> bottom) {
		if (!top.containsAll(bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	@Override
	public Lattice<Set<E>> bounded(Set<E> top, Set<E> bottom) {
		if (!this.top.containsAll(top)) throw new IllegalArgumentException();
		if (!bottom.containsAll(this.bottom)) throw new IllegalArgumentException();
		return new SetLattice<E>(top, bottom);
	}
	
	@Override
	public boolean equalInLattice(Set<E> a, Set<E> b) {
		if (!contains(a) || !contains(b)) throw new IllegalArgumentException();
		return a.equals(b);
	}
	
	@Override
	public boolean isOrdered(Set<E> a, Set<E> b) {
		if (!contains(a) && !(contains(b))) throw new IllegalArgumentException();
		return b.containsAll(a);
	}
	
	// object methods
	
	@Override
	public String toString() {
		return top + " to " + bottom;
	}
	
}
