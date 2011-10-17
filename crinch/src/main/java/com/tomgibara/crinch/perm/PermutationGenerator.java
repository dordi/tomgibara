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
		nextByNumber(true);
		return this;
	}
	
	public PermutationGenerator previousByNumber() {
		nextByNumber(false);
		return this;
	}
	
	public PermutationGenerator nextBySwap() {
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
		int[] array = new int[correspondence.length];
		for (int i = 0; i < array.length; i++) {
			array[correspondence[i]] = i;
		}
		System.arraycopy(array, 0, correspondence, 0, array.length);
		return this;
	}
	
	public PermutationGenerator reverse() {
		int h = correspondence.length / 2;
		for (int i = 0, j = correspondence.length - 1; i < h; i++, j--) {
			swap(i, j);
		}
		return this;
	}
	
	public PermutationGenerator shuffle(Random random) {
		for (int i = correspondence.length - 1; i > 0 ; i--) {
			transpose(i, random.nextInt(i + 1));
		}
		return this;
	}
	
	// equivalent to: permutation.permute(generator);
	public PermutationGenerator apply(Permutation permutation) {
		permutation.permute(this);
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
	public PermutationGenerator transpose(int i, int j) {
		if (i < 0) throw new IllegalArgumentException("negative i");
		if (j < 0) throw new IllegalArgumentException("negative j");
		if (i > correspondence.length) throw new IllegalArgumentException("i greater than or equal to size");
		if (j > correspondence.length) throw new IllegalArgumentException("j greater than or equal to size");
		
		if (j != i) swap(i, j);
		
		return this;
	}
	
	// private utility methods
	
	private void swap(int i, int j) {
		int t = correspondence[i];
		correspondence[i] = correspondence[j];
		correspondence[j] = t;
	}
	
	private void nextByNumber(boolean ascending) {
		int len = correspondence.length;
		
		int j = -1;
		for (int i = len - 2; i >= 0; i--) {
			if (ascending ? correspondence[i] < correspondence[i + 1] : correspondence[i] > correspondence[i + 1]) {
				j = i;
				break;
			}
		}
		if (j == -1) throw new IllegalStateException("no such permutation");
		int c = correspondence[j];
		
		int k = 0;
		for (int i = len - 1; i > j; i--) {
			if (ascending ? c < correspondence[i] : c > correspondence[i]) {
				k = i;
				break;
			}
		}
		
		swap(j, k);
		
		int h = (j + 1 + len) / 2;
		for (int i = j + 1, m = len - 1; i < h; i++, m--) {
			swap(i, m);
		}
	}
}
