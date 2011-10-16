package com.tomgibara.crinch.perm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.bits.BitVector;

public final class Permutation {

	private static final int[] NO_CYCLES = {};
	
	public static Permutation identity(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		int[] correspondence = new int[size];
		for (int i = 0; i < size; i++) {
			correspondence[i] = i;
		}
		return new Permutation(correspondence, NO_CYCLES);
	}

	public static Permutation reverse(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		int[] correspondence = new int[size];
		for (int i = 0; i < size; i++) {
			correspondence[i] = size - i - 1;
		}
		int h = size / 2;
		int[] cycles = new int[h * 2];
		for (int i = 0, j = 0; i < h; i++) {
			cycles[j++] = i;
			cycles[j++] = i - size;
		}
		return new Permutation(correspondence, cycles);
	}
	
	public static Permutation rotate(int size, int distance) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (size < 2) return identity(size);
		distance = distance % size;
		if (distance == 0) return identity(size);
		int[] correspondence = new int[size];
		if (distance < 0) distance += size;
		//TODO lazy, remove repeated %
		for (int i = 0; i < size; i++) {
			correspondence[i] = (i + distance) % size;
		}
		int[] cycles = correspondence.clone();
		cycles[size - 1] = -1 - cycles[size - 1];
		return new Permutation(correspondence, cycles);
	}
	
	public static Permutation transpose(int size, int i, int j) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (i < 0 || j < 0 || i >= size || j >= size) throw new IllegalArgumentException("invalid indices");
		if (i == j) return identity(size);
		int[] correspondence = new int[size];
		for (int k = 0; k < size; k++) {
			correspondence[k] = k;
		}
		correspondence[i] = j;
		correspondence[j] = i;
		int[] cycles = {i, -1 - j };
		return new Permutation(correspondence, cycles);
	}
	
	private final int[] correspondence;
	//cycles is so important, we keep that on permutation
	private int[] cycles = null;
	//everything else is secondary, and we store it separately
	private Info info = null;

	private Permutation(int[] correspondence, int[] cycles) {
		this.correspondence = correspondence;
		this.cycles = cycles;
	}
		
	Permutation(PermutationGenerator generator) {
		this.correspondence = generator.correspondence.clone();
	}
	
	public Permutation(int... correspondence) {
		if (correspondence == null) throw new IllegalArgumentException("null correspondence");
		this.correspondence = correspondence;
		cycles = computeCycles(true);
	}

	public int getSize() {
		return correspondence.length;
	}

	public int[] getCorrespondence() {
		return correspondence.clone();
	}
	
	public PermutationGenerator generator() {
		return new PermutationGenerator(correspondence.clone());
	}
	
	public Info getInfo() {
		return info == null ? info = new Info() : info;
	}

	public <P extends Permutable> P permute(P permutable) {
		if (permutable == null) throw new IllegalArgumentException("null permutable");
		if (permutable.getPermutableSize() != correspondence.length) throw new IllegalArgumentException("size mismatched");

		int[] cycles = getCycles();
		for (int i = 0, initial = -1, previous = -1; i < cycles.length; i++) {
			int next = cycles[i];
			if (initial < 0) {
				initial = next;
			} else {
				if (next < 0) {
					next = -1 - next;
					initial = -1;
				}
				//TODO somewhat dicey assumption here
				permutable = (P) permutable.transpose(previous, next);
			}
			previous = next;
		}
		
		return permutable;
	}
	
	// object methods
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(correspondence);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Permutation)) return false;
		Permutation that = (Permutation) obj;
		return Arrays.equals(this.correspondence, that.correspondence);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(correspondence);
	}

	// package scoped methods

	void generator(PermutationGenerator generator) {
		System.arraycopy(this.correspondence, 0, generator.correspondence, 0, this.correspondence.length);
	}

	// private utility methods
	
	private int[] getCycles() {
		if (cycles == null) {
			cycles = computeCycles(false);
		}
		return cycles;
	}
	
	private int[] computeCycles(boolean verify) {
		if (verify) {
			for (int i = 0; i < correspondence.length; i++) {
				int c = correspondence[i];
				if (c < 0 || c >= correspondence.length) throw new IllegalArgumentException("invalid correspondence");
			}
		}
		int[] array = correspondence.clone();
		int[] cycles = new int[array.length + 1];
		int index = 0;
		outer: while (true) {
			for (int i = 0; i < array.length; i++) {
				int a = array[i];
				if (a == -1) {
					continue;
				}
				if (a == i) {
					array[i] = -1;
					continue;
				}
				int[] correspondence = new int[array.length];
				for (int k = 0; k < array.length; k++) {
					correspondence[k] = k;
				}
				for (int j = i;;) {
					int b = array[j];
					if (verify && b == -1) throw new IllegalArgumentException("invalid correspondence");
					array[j] = -1;
					if (b == i) {
						cycles[index++] = -1 - b;
						break;
					}
					cycles[index++] = b;
					j = b;
				}
				continue outer;
			}
			break;
		}
		return cycles.length > index ? Arrays.copyOf(cycles, index) : cycles;
	}
	
	// innner classes
	
	public final class Info {
		
		// computed eagerly
		private final int numberOfCycles;
		private final int numberOfTranspositions;
		private final boolean identity;
		private final boolean odd;
		
		// computed lazily
		BitVector fixedPoints;
		Set<Permutation> disjointCycles;
		
		public Info() {
			// ensure number of cycles has been computed
			// set properties that are cheap, eagerly
			int numberOfCycles = 0;
			int[] cycles = getCycles();
			for (int i = 0; i < cycles.length; i++) {
				if (cycles[i] < 0) numberOfCycles++;
			}
			this.numberOfCycles = numberOfCycles;
			numberOfTranspositions = cycles.length - numberOfCycles;
			identity = numberOfTranspositions == 0;
			odd = (numberOfTranspositions % 2) == 1;
		}
		
		public boolean isIdentity() {
			return identity;
		}

		public int getNumberOfTranspositions() {
			return numberOfTranspositions;
		}
		
		public boolean isOdd() {
			return odd;
		}
		
		public BitVector getFixedPoints() {
			if (fixedPoints == null) {
				int[] array = correspondence;
				fixedPoints = new BitVector(array.length);
				for (int i = 0; i < array.length; i++) {
					fixedPoints.setBit(i, array[i] == i);
				}
			}
			return fixedPoints;
		}
		
		public int getNumberOfCycles() {
			return numberOfCycles;
		}
		
		public Set<Permutation> getDisjointCycles() {
			if (disjointCycles == null) {
				switch (numberOfCycles) {
				case 0 :
					disjointCycles = Collections.emptySet();
					break;
				case 1 :
					disjointCycles = Collections.singleton(Permutation.this);
					break;
				default :
					Set<Permutation> set = new HashSet<Permutation>();
					int[] array = null;
					for (int i = 0; i < cycles.length; i++) {
						if (array == null) {
							array = new int[correspondence.length];
							for (int j = 0; j < array.length; j++) {
								array[j] = j;
							}
						}
						int a = cycles[i];
						if (a < 0) {
							a = -1 - a;
							array[a] = correspondence[a];
							set.add(new Permutation(array));
							array = null;
						} else {
							array[a] = correspondence[a];
						}
					}
					disjointCycles = Collections.unmodifiableSet(set);
				}
			}
			return disjointCycles;
		}
		
	}
	
}
