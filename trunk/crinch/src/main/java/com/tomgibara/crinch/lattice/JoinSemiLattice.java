package com.tomgibara.crinch.lattice;

public interface JoinSemiLattice<E> extends SemiLattice<E> {

	E join(E a, E b);
	
	BoundedJoinSemiLattice<E> boundedJoinSemiLattice(E top);

}
