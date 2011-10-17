package com.tomgibara.crinch.perm;

import java.util.Arrays;
import java.util.Random;

public final class PermutationGenerator implements Permutable {

	final int[] correspondence;
	private OrderedSequence orderedSequence = null;

	PermutationGenerator(int[] correspondence) {
		this.correspondence = correspondence;
	}
	
	// accessors
	
	public OrderedSequence getOrderedSequence() {
		return orderedSequence == null ? orderedSequence = new OrderedSequence() : orderedSequence;
	}
	
	// mutators
	
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
	
	// factory methods
	
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
	
	// object methods

	// equality predicated on strict object equality
	
	@Override
	public String toString() {
		return Arrays.toString(correspondence);
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
	
	private class OrderedSequence implements PermutationSequence {

		public boolean hasNext() {
			int[] array = correspondence;
			for (int i = 1; i < array.length; i++) {
				if (array[i] > array[i - 1]) return true;
			}
			return false;
		}
		
		public boolean hasPrevious() {
			int[] array = correspondence;
			for (int i = 1; i < array.length; i++) {
				if (array[i] < array[i - 1]) return true;
			}
			return false;
		}

		@Override
		public PermutationSequence next() {
			nextByNumber(true);
			return this;
		}

		@Override
		public PermutationSequence previous() {
			nextByNumber(false);
			return this;
		}

		@Override
		public PermutationGenerator getGenerator() {
			return PermutationGenerator.this;
		}

		@Override
		public String toString() {
			return "OrderSequence at " + PermutationGenerator.this.toString();
		}
		
	}
}
