package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.NullBitWriter;

import junit.framework.TestCase;

public abstract class CodingTest<C extends Coding> extends TestCase {

	abstract Iterable<C> getCodings();
	
	abstract int getMaxEncodableValue(C coding);
	
	boolean isEncodableValueLimited(C coding) {
		return getMaxEncodableValue(coding) >= 0;
	}
	
	public void testSmallInts() {
		for (C coding : getCodings()) {
			int min = 0;
			int max = 1000;
			if (isEncodableValueLimited(coding)) max = Math.min(max, getMaxEncodableValue(coding));

			int size;
			{
				NullBitWriter w = new NullBitWriter();
				for (int i = min; i <= max; i++) {
					coding.encodePositiveInt(w, i);
				}
				size = (int) w.getPosition();
			}

			BitVector v = new BitVector(size);
			
			BitWriter w = v.openWriter();
			for (int i = min; i <= max; i++) {
				coding.encodePositiveInt(w, i);
			}
			
			BitReader r = v.openReader();
			for (int i = min; i <= max; i++) {
				assertEquals(i, coding.decodePositiveInt(r));
			}
		}
	}
}
