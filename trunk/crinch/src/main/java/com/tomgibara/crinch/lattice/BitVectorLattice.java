/*
 * Copyright 2010 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.lattice;

import com.tomgibara.crinch.bits.BitVector;

public class BitVectorLattice extends AbstractLattice<BitVector> {

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
		if (vector.size() != top.size()) throw new IllegalArgumentException("incorrect vector size");
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
	
	@Override
	public boolean equalInLattice(BitVector a, BitVector b) {
		if (!contains(a) || !contains(b)) throw new IllegalArgumentException();
		return a.equals(b);
	}
	
	@Override
	public boolean isOrdered(BitVector a, BitVector b) {
		if (b == null) throw new IllegalArgumentException();
		return b.testContains(a);
	}
	
}
