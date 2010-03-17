package com.tomgibara.crinch.lattice;

public interface BoundedJoinSemiLattice<E> extends JoinSemiLattice<E> {

	E getTop();

}
