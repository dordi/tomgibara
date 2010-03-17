package com.tomgibara.crinch.lattice;

public class ProductLattice implements Lattice<Object[]> {

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
	
	private void checkTuple(Object[] tuple) {
		if (tuple == null) throw new IllegalArgumentException("null tuple");
		if (tuple.length != lattices.length) throw new IllegalArgumentException("wrong tuple length, expected " + lattices.length + " got " + tuple.length);
	}
	
}
