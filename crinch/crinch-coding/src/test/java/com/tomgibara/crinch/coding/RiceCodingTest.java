package com.tomgibara.crinch.coding;

import java.util.Arrays;
import java.util.Random;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.bits.NullBitWriter;
import com.tomgibara.crinch.bits.BitVector.Operation;

public class RiceCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	private static ExtendedCoding coding(int divisor) {
		return new ExtendedCoding(new RiceCoding(divisor));	
	}
	
	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Arrays.asList( coding(6), coding(3), coding(1), coding(0) );
	}
	
	@Override
	int getMaxEncodableValue(ExtendedCoding coding) {
		return 10 << ((RiceCoding)(coding.getUniversalCoding())).getBits();
	}

	public void testEqualsGolomb() {
		Random r = new Random();
		for (int i = 0; i < 10000; i++) {
			int bits = r.nextInt(8);
			int value = r.nextInt(10 << bits);
			RiceCoding rc = new RiceCoding(bits);
			GolombCoding gc = new GolombCoding(1 << bits);
			NullBitWriter rw = new NullBitWriter();
			rc.encodePositiveInt(rw, value);
			NullBitWriter gw = new NullBitWriter();
			gc.encodePositiveInt(gw, value);
			assertEquals(rw.getPosition(), gw.getPosition());
			BitVector v = new BitVector((int) rw.getPosition());
			rc.encodePositiveInt(v.openWriter(), value);
			rc.encodePositiveInt(v.openWriter(Operation.XOR, 0), value);
			assertTrue(v.isAllZeros());
		}
	}
	
}
