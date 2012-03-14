package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.NullBitWriter;

import junit.framework.TestCase;

public class TruncatedBinaryCodingTest extends TestCase {

	public void testBasicInt() {
		for (int size = 1; size < 100; size++) {
			TruncatedBinaryCoding coding = new TruncatedBinaryCoding(size);
			
			NullBitWriter writer = new NullBitWriter();
			for (int i = 0; i < size; i++) {
				coding.encodePositiveInt(writer, i);
			}
			int length = (int) writer.getPosition();
			
			BitVector v = new BitVector(length);
			BitWriter w = v.openWriter();
			for (int i = 0; i < size; i++) {
				coding.encodePositiveInt(w, i);
			}
			
			BitReader r = v.openReader();
			for (int i = 0; i < size; i++) {
				assertEquals(i, coding.decodePositiveInt(r));
			}
		}
	}
	
}
