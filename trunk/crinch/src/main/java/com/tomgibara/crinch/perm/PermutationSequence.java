package com.tomgibara.crinch.perm;

public interface PermutationSequence {

	boolean hasNext();
	
	boolean hasPrevious();
	
	PermutationSequence next();
	
	PermutationSequence previous();
	
	// the generator this sequence is manipluating
	
	PermutationGenerator getGenerator();
}
