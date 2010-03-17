package com.tomgibara.crinch.lattice;

public interface Lattice<E> extends MeetSemiLattice<E>, JoinSemiLattice<E> {

	BoundedLattice<E> boundedLattice(E top, E bottom);

}
