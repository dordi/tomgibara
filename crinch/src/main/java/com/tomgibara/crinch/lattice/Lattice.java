package com.tomgibara.crinch.lattice;

//TODO can optimize implementation of bounded methods by checking for equality with top/bottom
public interface Lattice<E> extends MeetSemiLattice<E>, JoinSemiLattice<E> {

	Lattice<E> boundedAbove(E top);

	Lattice<E> boundedBelow(E bottom);

	Lattice<E> boundedLattice(E top, E bottom);

	boolean isBounded();

}
