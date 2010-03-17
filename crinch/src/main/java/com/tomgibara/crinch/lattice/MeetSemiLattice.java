package com.tomgibara.crinch.lattice;

public interface MeetSemiLattice<E> extends SemiLattice<E> {

	E meet(E a, E b);

	BoundedMeetSemiLattice<E> boundedMeetSemiLattice(E bottom);

}
