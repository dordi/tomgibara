package com.tomgibara.crinch.perm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.bits.BitVector;

public final class Permutation {

	private static final int[] NO_SWAPS = {};
	
	public static Permutation identity(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		return new Permutation(size, false);
	}

	public static Permutation reverse(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		return new Permutation(size, true);
	}
	
	public static Permutation rotate(int size) {
		//TODO
		throw new UnsupportedOperationException();
	}
	
	private final int[] correspondence;
	//swaps is so important, we keep that on correspondence
	private int[] swaps = null;
	//everything else is secondary, and we store it separately
	private Info info = null;

	Permutation(int size, boolean reverse) {
		correspondence = new int[size];
		if (reverse) {
			for (int i = 0; i < size; i++) {
				correspondence[i] = size - i - 1;
			}
		} else {
			for (int i = 0; i < size; i++) {
				correspondence[i] = i;
			}
			swaps = NO_SWAPS;
		}
	}
	
	public Permutation(int... correspondence) {
		if (correspondence == null) throw new IllegalArgumentException("null correspondence");
		this.correspondence = correspondence;
		swaps = computeSwaps(true);
	}
	
	Permutation(PermutationGenerator generator) {
		this.correspondence = generator.correspondence.clone();
	}
	
	public int getSize() {
		return correspondence.length;
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
		applyForward(permutable);
		return permutable;
	}
	
	public <P extends Permutable> P unpermute(P permutable) {
		if (permutable == null) throw new IllegalArgumentException("null permutable");
		if (permutable.getPermutableSize() != correspondence.length) throw new IllegalArgumentException("size mismatched");
		applyBackward(permutable);
		return permutable;
	}

	//convenience method
	
	// equivalent to: permutation.permute(generator()).permutation();
	public Permutation compose(Permutation permutation) {
		return permutation.permute(generator()).permutation(); 
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
		return Arrays.toString(correspondence) + " " + Arrays.toString(getSwaps());
	}

	// package scoped methods

	void generator(PermutationGenerator generator) {
		System.arraycopy(this.correspondence, 0, generator.correspondence, 0, this.correspondence.length);
	}

	// private utility methods
	
	private int[] getSwaps() {
		if (swaps == null) {
			swaps = computeSwaps(false);
		}
		return swaps;
	}
	
	private int[] computeSwaps(boolean verify) {
		final int[] array = correspondence.clone();
		final int length = array.length;
		int[] swaps = NO_SWAPS;
		int index = 0;
		outer: while (true) {
			// find a swap that moves two elements closer
			for (int i = 0; i < length - 1; i++) {
				int a = array[i];
				// only consider 'forward' swaps
				if (a <= i) continue;
				int limit = Math.min(2 * a - i, length);
				for (int j = i + 1; j < limit; j++) {
					int b = array[j];
					if (Math.abs(b - i) < Math.abs(b - j)) {
						if (index == swaps.length) {
							swaps = Arrays.copyOf(swaps, Math.max(swaps.length * 2, 16));
						}
						swaps[index ++] = i;
						swaps[index ++] = j;
						array[i] = b;
						array[j] = a;
						continue outer;
					}
				}
			}
			// find a swap that moves one element closer
			for (int i = 0; i < length - 1; i++) {
				int a = array[i];
				// only consider 'forward' swaps
				if (a <= i) continue;
				array[i] = array[a];
				array[a] = a;
				continue outer;
			}
			// no swaps, we must be done
			break;
		}
		if (verify) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] != i) throw new IllegalArgumentException("invalid correspondence array");
			}
		}
		return swaps.length > index ? Arrays.copyOf(swaps, index) : swaps;
	}
	
	private void applyForward(Permutable p) {
		int[] swaps = getSwaps();
		for (int i = swaps.length; i > 0; i-=2) {
			p = p.swap(swaps[i-1], swaps[i-2]);
		}
	}

	private void applyBackward(Permutable p) {
		int[] swaps = getSwaps();
		for (int i = 0; i < swaps.length; i+=2) {
			p = p.swap(swaps[i], swaps[i+1]);
		}
	}

	// innner classes
	
	public final class Info {
		
		// computed eagerly
		private final int numberOfSwaps;
		private final boolean identity;
		private final boolean odd;
		
		// computed lazily
		BitVector fixedElements;
		int numberOfCycles = -1;
		Set<Permutation> cycles;
		
		public Info() {
			int[] swaps = getSwaps();
			// set properties that are cheap, eagerly
			numberOfSwaps = swaps.length >> 1;
			identity = numberOfSwaps == 0;
			odd = (numberOfSwaps % 2) == 1;
		}
		
		public boolean isIdentity() {
			return identity;
		}

		public int getNumberOfSwaps() {
			return numberOfSwaps;
		}
		
		public boolean isOdd() {
			return odd;
		}
		
		public BitVector getFixedElements() {
			if (fixedElements == null) {
				int[] array = correspondence;
				fixedElements = new BitVector(array.length);
				for (int i = 0; i < array.length; i++) {
					fixedElements.setBit(i, array[i] == i);
				}
			}
			return fixedElements;
		}
		
		public int getNumberOfCycles() {
			if (numberOfCycles == -1) {
				numberOfCycles = correspondence.length - getFixedElements().countOnes() - numberOfSwaps;
			}
			return numberOfCycles;
		}
		
		public Set<Permutation> getCycles() {
			if (cycles == null) {
				int numberOfCycles = getNumberOfCycles();
				switch (numberOfCycles) {
				case 0 :
					cycles = Collections.emptySet();
					break;
				case 1 :
					cycles = Collections.singleton(Permutation.this);
					break;
				default :
					Set<Permutation> set = new HashSet<Permutation>();
					int[] array = correspondence.clone();
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
								array[j] = -1;
								correspondence[j] = b;
								j = b;
								if (j == i) break;
							}
							set.add(new Permutation(correspondence));
							continue outer;
						}
						break;
					}
					cycles = Collections.unmodifiableSet(set);
				}
			}
			return cycles;
		}
		
	}
	
}
