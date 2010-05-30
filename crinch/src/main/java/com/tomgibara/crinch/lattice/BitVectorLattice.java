package com.tomgibara.crinch.lattice;

import com.tomgibara.crinch.bits.BitVector;

public class BitVectorLattice implements Lattice<BitVector> {

	private static BitVector makeTop(int size) {
		BitVector vector = new BitVector(size);
		vector.set(true);
		return vector;
	}
	
	private static BitVector makeBottom(int size) {
		return new BitVector(size);
	}
	
	private final BitVector top;
	private final BitVector bottom;
	
	public BitVectorLattice(int vectorSize) {
		this(makeTop(vectorSize), makeBottom(vectorSize));
	}

	public BitVectorLattice(BitVector top) {
		this(top, makeBottom(top.size()));
	}

	public BitVectorLattice(BitVector top, BitVector bottom) {
		if (top == null) throw new IllegalArgumentException("null top");
		if (bottom == null) throw new IllegalArgumentException("null bottom");
		if (top.size() != bottom.size()) throw new IllegalArgumentException("top and bottom are different sizes");
		if (!top.testContains(bottom)) throw new IllegalArgumentException("top does not contain bottom");
		this.top = top;
		this.bottom = bottom;
	}

	// lattice methods
	
	@Override
	public BitVector getTop() {
		return top;
	}
	
	@Override
	public BitVector getBottom() {
		return bottom;
	}
	
	@Override
	public boolean isBounded() {
		return true;
	}
	
	@Override
	public boolean isBoundedAbove() {
		return true;
	}
	
	@Override
	public boolean isBoundedBelow() {
		return true;
	}
	
	@Override
	public Lattice<BitVector> bounded(BitVector top, BitVector bottom) {
		if (top == null) throw new IllegalArgumentException("null top");
		if (bottom == null) throw new IllegalArgumentException("null bottom");
		if (!this.top.testContains(top)) throw new IllegalArgumentException("top out of bounds");
		if (!bottom.testContains(this.bottom)) throw new IllegalArgumentException("bottom out of bounds");
		return new BitVectorLattice(top, bottom);
	}
	
	@Override
	public Lattice<BitVector> boundedAbove(BitVector top) {
		if (top == null) throw new IllegalArgumentException("null top");
		if (!this.top.testContains(top)) throw new IllegalArgumentException("top out of bounds");
		return new BitVectorLattice(top, bottom);
	}
	
	@Override
	public Lattice<BitVector> boundedBelow(BitVector bottom) {
		if (bottom == null) throw new IllegalArgumentException("null bottom");
		if (!bottom.testContains(this.bottom)) throw new IllegalArgumentException("bottom out of bounds");
		return new BitVectorLattice(top, bottom);
	}
	
	@Override
	public boolean contains(BitVector vector) {
		if (vector == null) throw new IllegalArgumentException("null vector");
		return top.testContains(vector) && vector.testContains(bottom);
	}
	
	@Override
	public BitVector join(BitVector a, BitVector b) {
		BitVector v = a.mutableCopy();
		v.orVector(b);
		return v;
	}
	
	@Override
	public BitVector meet(BitVector a, BitVector b) {
		BitVector v = a.mutableCopy();
		v.andVector(b);
		return v;
	}
}
