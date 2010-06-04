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

public class ProductLattice extends AbstractLattice<Object[]> {

	private final Lattice<Object>[] lattices;
	
	public ProductLattice(Lattice... lattices) {
		if (lattices == null) throw new IllegalArgumentException();
		this.lattices = lattices;
	}

	@Override
	public boolean isBoundedAbove() {
		for (int i = 0; i < lattices.length; i++) {
			if (!lattices[i].isBoundedAbove()) return false;
		}
		return true;
	}
	
	@Override
	public boolean isBoundedBelow() {
		for (int i = 0; i < lattices.length; i++) {
			if (!lattices[i].isBoundedBelow()) return false;
		}
		return true;
	}
	
	@Override
	public boolean isBounded() {
		for (int i = 0; i < lattices.length; i++) {
			if (!lattices[i].isBounded()) return false;
		}
		return true;
	}
	
	@Override
	public Object[] getTop() {
		Object[] tuple = null;
		for (int i = 0; i < lattices.length; i++) {
			final Lattice<?> lattice = lattices[i];
			if (!lattice.isBoundedAbove()) continue;
			if (tuple == null) tuple = new Object[lattices.length];
			tuple[i] = lattice.getTop();
		}
		return tuple;
	}
	
	@Override
	public Object[] getBottom() {
		Object[] tuple = null;
		for (int i = 0; i < lattices.length; i++) {
			final Lattice<?> lattice = lattices[i];
			if (!lattice.isBoundedAbove()) continue;
			if (tuple == null) tuple = new Object[lattices.length];
			tuple[i] = lattice.getTop();
		}
		return tuple;
	}
	
	@Override
	public boolean contains(Object[] tuple) {
		checkTuple(tuple);
		for (int i = 0; i < tuple.length; i++) {
			if (!lattices[i].contains(tuple[i])) return false;
		}
		return true;
	}
	
	@Override
	public Lattice<Object[]> boundedAbove(Object[] top) {
		checkTuple(top);
		final Lattice[] lattices = new Lattice[this.lattices.length];
		for (int i = 0; i < lattices.length; i++) {
			lattices[i] = this.lattices[i].boundedAbove(top[i]);
		}
		return new ProductLattice(lattices);
	}

	@Override
	public Lattice<Object[]> boundedBelow(Object[] bottom) {
		checkTuple(bottom);
		final Lattice[] lattices = new Lattice[this.lattices.length];
		for (int i = 0; i < lattices.length; i++) {
			lattices[i] = this.lattices[i].boundedBelow(bottom[i]);
		}
		return new ProductLattice(lattices);
	}

	@Override
	public Lattice<Object[]> bounded(Object[] top, Object[] bottom) {
		checkTuple(top);
		checkTuple(bottom);
		final Lattice[] lattices = new Lattice[this.lattices.length];
		for (int i = 0; i < lattices.length; i++) {
			lattices[i] = this.lattices[i].bounded(top[i], bottom[i]);
		}
		return new ProductLattice(lattices);
	}
	
	@Override
	public Object[] join(Object[] tupleA, Object[] tupleB) {
		checkTuple(tupleA);
		checkTuple(tupleB);
		final Object[] tuple = new Object[lattices.length];
		for (int i = 0; i < tuple.length; i++) {
			tuple[i] = lattices[i].join(tupleA[i], tupleB[i]);
		}
		return tuple;
	}

	@Override
	public Object[] meet(Object[] tupleA, Object[] tupleB) {
		checkTuple(tupleA);
		checkTuple(tupleB);
		final Object[] tuple = new Object[lattices.length];
		for (int i = 0; i < tuple.length; i++) {
			tuple[i] = lattices[i].meet(tupleA[i], tupleB[i]);
		}
		return tuple;
	}
	
	@Override
	public boolean equalInLattice(Object[] tupleA, Object[] tupleB) {
		checkTuple(tupleA);
		checkTuple(tupleB);
		for (int i = 0; i < lattices.length; i++) {
			if (!lattices[i].equalInLattice(tupleA[i], tupleB[i])) return false;
		}
		return true;
	}

	@Override
	public boolean isOrdered(Object[] tupleA, Object[] tupleB) {
		checkTuple(tupleA);
		checkTuple(tupleB);
		for (int i = 0; i < lattices.length; i++) {
			if (!lattices[i].isOrdered(tupleA[i], tupleB[i])) return false;
		}
		return true;
	}

	@Override
	public Comparison compare(Object[] tupleA, Object[] tupleB) {
		checkTuple(tupleA);
		checkTuple(tupleB);
		Comparison comp = null;
		for (int i = 0; i < lattices.length; i++) {
			Comparison c = lattices[i].compare(tupleA[i], tupleB[i]);
			comp = comp == null ? c : comp.combine(c);
			if (comp == Comparison.INCOMPARABLE) break;
		}
		return comp;
	}
	
	private void checkTuple(Object[] tuple) {
		if (tuple == null) throw new IllegalArgumentException("null tuple");
		if (tuple.length != lattices.length) throw new IllegalArgumentException("wrong tuple length, expected " + lattices.length + " got " + tuple.length);
	}
	
}
