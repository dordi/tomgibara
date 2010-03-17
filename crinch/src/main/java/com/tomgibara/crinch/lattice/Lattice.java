package com.tomgibara.crinch.lattice;

public interface Lattice<E> extends MeetSemiLattice<E>, JoinSemiLattice<E> {

	Lattice<E> boundedAbove(E top);

	Lattice<E> boundedBelow(E bottom);

	Lattice<E> boundedLattice(E top, E bottom);

	boolean isBounded();

}
