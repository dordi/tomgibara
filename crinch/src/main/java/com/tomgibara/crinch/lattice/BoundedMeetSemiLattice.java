package com.tomgibara.crinch.lattice;

public interface BoundedMeetSemiLattice<E> extends JoinSemiLattice<E> {

	E getBottom();
	
}
