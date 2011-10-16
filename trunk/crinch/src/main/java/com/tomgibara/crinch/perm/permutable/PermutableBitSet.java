package com.tomgibara.crinch.perm.permutable;

import java.util.BitSet;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableBitSet implements Permutable {

	private final BitSet bitSet;
	
	public PermutableBitSet(BitSet bitSet) {
		if (bitSet == null) throw new IllegalArgumentException("null bitSet");
		this.bitSet = bitSet;
	}

	public BitSet getBitSet() {
		return bitSet;
	}
	
	@Override
	public int getPermutableSize() {
		return bitSet.size();
	}
	
	@Override
	public PermutableBitSet swap(int i, int j) {
		boolean b = bitSet.get(i);
		bitSet.set(i, bitSet.get(j));
		bitSet.set(j, b);
		return this;
	}
	
}
