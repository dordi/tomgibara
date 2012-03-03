package com.tomgibara.crinch.bits;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class BitVectorDeBruijnTest extends TestCase {

	public void testDeBruijn() {
		for (int size = 1; size < 8; size++) {
			testDeBruijn(size);
		}
	}

	private void testDeBruijn(int size) {
		BitVector sequence = generateDeBruijn(size);
		System.out.println(sequence);
		HashSet<Integer> values = new HashSet<Integer>();
		for (int i = 0; i < sequence.size() - size; i++) {
			int value = (int) sequence.getBits(i, size);
			values.add(value);
		}
		assertEquals(1 << size, values.size());
	}

	private BitVector generateDeBruijn(int size) {
		if (size > 31) throw new IllegalArgumentException("size too big");
		int count = 1 << size;
		Set<Integer> memory = new BitVector(count).asSet();
		BitVector sequence = new BitVector(count + size);
		sequence.setRange(0, size, true);
		for (int i = 0; i < count; i++) {
			int bits = (int) sequence.getBits(i, size);
			memory.add(bits);
			bits >>= 1;
			sequence.setBit(i + size, memory.contains(bits));
		}
		return sequence;
	}
	
}
