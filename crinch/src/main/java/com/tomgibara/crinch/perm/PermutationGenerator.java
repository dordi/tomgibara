package com.tomgibara.crinch.perm;

import java.util.Random;

public class PermutationGenerator implements Permutable {

	final int[] correspondence;

	PermutationGenerator(int[] correspondence) {
		this.correspondence = correspondence;
	}
	
	public boolean hasNext() {
		for (int i = 1; i < correspondence.length; i++) {
			if (correspondence[i] > correspondence[i - 1]) return true;
		}
		return false;
	}
	
	public boolean hasPrevious() {
		for (int i = 1; i < correspondence.length; i++) {
			if (correspondence[i] < correspondence[i - 1]) return true;
		}
		return false;
	}
	
	public PermutationGenerator nextByNumber() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	public PermutationGenerator nextBySwap() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	public PermutationGenerator previousByNumber() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	public PermutationGenerator previousBySwap() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	public PermutationGenerator set(Permutation permutation) {
		if (permutation == null) throw new IllegalArgumentException("null permutation");
		if (permutation.getSize() != correspondence.length) throw new IllegalArgumentException("incorrect size");
		permutation.generator(this);
		return this;
	}
	
	public PermutationGenerator invert() {
		int h = correspondence.length / 2;
		for (int i = 0, j = correspondence.length - 1; i < h; i++, j--) {
			swap(i, j);
		}
		return this;
	}
	
	public PermutationGenerator shuffle(Random random) {
		for (int i = correspondence.length - 1; i > 0 ; i--) {
			swap(i, random.nextInt(i + 1));
		}
		return this;
	}
	
	public Permutation permutation() {
		return new Permutation(this);
	}
	
	// permutable interface
	
	@Override
	public int getPermutableSize() {
		return correspondence.length;
	}
	
	@Override
	public PermutationGenerator swap(int i, int j) {
		if (i < 0) throw new IllegalArgumentException("negative i");
		if (j < 0) throw new IllegalArgumentException("negative j");
		if (i > correspondence.length) throw new IllegalArgumentException("i greater than or equal to size");
		if (j > correspondence.length) throw new IllegalArgumentException("j greater than or equal to size");
		
		if (j != i) {
			int t = correspondence[i];
			correspondence[i] = correspondence[j];
			correspondence[j] = t;
		}
		return this;
	}
	
	
}
