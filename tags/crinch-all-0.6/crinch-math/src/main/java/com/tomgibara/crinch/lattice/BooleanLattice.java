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

public class BooleanLattice extends AbstractLattice<Boolean> {

	private final boolean top;
	private final boolean bottom;
	
	public BooleanLattice() {
		top = true;
		bottom = false;
	}
	
	public BooleanLattice(boolean top, boolean bottom) {
		if (bottom && !top) throw new IllegalArgumentException();
		this.top = top;
		this.bottom = bottom;
	}
	
	@Override
	public boolean isBounded() {
		return true;
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
	public Boolean getTop() {
		return top;
	}
	
	@Override
	public Boolean getBottom() {
		return bottom;
	}
	
	@Override
	public boolean contains(Boolean bool) {
		if (bool == null) throw new IllegalArgumentException();
		final boolean b = bool;
		return b == top || b == bottom;
	}
	
	@Override
	public Lattice<Boolean> bounded(Boolean top, Boolean bottom) {
		final boolean t = top;
		final boolean b = bottom;
		if (t == this.top && b == this.bottom) return this;
		return new BooleanLattice(t, b);
	}
	
	@Override
	public Lattice<Boolean> boundedAbove(Boolean top) {
		final boolean t = top;
		return t == this.top ? this : new BooleanLattice(t, bottom);
	}

	@Override
	public Lattice<Boolean> boundedBelow(Boolean bottom) {
		final boolean b = bottom;
		return b == this.bottom ? this : new BooleanLattice(top, b);
	}
	
	@Override
	public Boolean join(Boolean a, Boolean b) {
		final boolean c = a || b;
		if (c != top && c != bottom) throw new IllegalArgumentException();
		return c;
	}
	
	@Override
	public Boolean meet(Boolean a, Boolean b) {
		final boolean c = a && b;
		if (c != top && c != bottom) throw new IllegalArgumentException();
		return c;
	}
	
	@Override
	public boolean equalInLattice(Boolean a, Boolean b) {
		if (!contains(a) || !contains(b)) throw new IllegalArgumentException();
		return a.booleanValue() == b.booleanValue();
	}
	
	@Override
	public boolean isOrdered(Boolean a, Boolean b) {
		if (!contains(a) || !contains(b)) throw new IllegalArgumentException();
		return b || !a;
	}
	
}
