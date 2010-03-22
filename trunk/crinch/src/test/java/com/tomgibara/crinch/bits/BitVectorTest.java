package com.tomgibara.crinch.bits;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitVector;

import junit.framework.TestCase;

public class BitVectorTest extends TestCase {

	public void testSetBit() throws Exception {
		BitVector v = new BitVector(100);
		for (int i = 0; i < 100; i++) {
			v.set(i, true);
			for (int j = 0; j < 100; j++) {
				assertEquals("Mismatch at " + j + " during " + i, j == i, v.getBit(j));
			}
			v.set(i, false);
		}
	}
	
	public void testGet() throws Exception {
		//72 long
		BitVector v = new BitVector("100101111011011100001101011001100000101110001011100001110011101101100010");
		assertEquals((byte)new BigInteger("10010111", 2).intValue(), v.getByte(64));
	}

}
