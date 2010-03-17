package com.tomgibara.crinch.lattice;

public interface BoundedLattice<E> extends Lattice<E>, BoundedJoinSemiLattice<E>, BoundedMeetSemiLattice<E> {

}
