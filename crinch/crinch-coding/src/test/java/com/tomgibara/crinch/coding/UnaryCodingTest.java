package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.bits.BitWriter;

import junit.framework.TestCase;

public class UnaryCodingTest extends TestCase {

	public void testFirstThousandNumbers() {
		UnaryCoding c = UnaryCoding.zeroTerminated;

		int min = 1;
		int max = 1000;
		
		int size = ((max + 1) * (max + 2) - min * (min + 1)) / 2;
		
		BitVector v = new BitVector(size);
		
		BitWriter w = v.openWriter();
		for (int i = min; i <= max; i++) {
			assertEquals(i+1, c.encodePositiveInt(w, i));
		}
		
		assertEquals(size, w.getPosition());
		
		BitReader r = v.openReader();
		for (int i = min; i <= max; i++) {
			assertEquals(i, c.decodePositiveInt(r));
		}
	}
	
}