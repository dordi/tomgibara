package com.tomgibara.crinch.bits;

import java.util.Random;

public abstract class AbstractByteBasedBitReaderTest extends AbstractBitReaderTest {

	abstract ByteBasedBitReader readerFor(BitVector vector);
	
	public void testSetPosition() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = 8 + 8 * r.nextInt(100);
			BitVector source = new BitVector(r, size);
			ByteBasedBitReader reader = readerFor(source);

			for (int j = 0; j < 100; j++) {
				int position = r.nextInt(size);
				try {
					reader.setPosition(position);
				} catch (IllegalArgumentException e) {
					// backward seek not supported
					reader = readerFor(source);
					reader.setPosition(position);
				}
				assertEquals("at bit " + position, source.getBit(position), reader.readBoolean());
			}
		}
	}

}
