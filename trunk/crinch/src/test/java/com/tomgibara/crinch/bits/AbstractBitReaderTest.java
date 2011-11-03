package com.tomgibara.crinch.bits;

import java.util.Random;

import junit.framework.TestCase;

public abstract class AbstractBitReaderTest extends TestCase {

	abstract BitReader readerFor(BitVector vector);

	public void testReadBoolean() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = r.nextInt(100) * 8;
			BitVector source = new BitVector(r, size);
			BitReader reader = readerFor(source);

			for (int j = 0; j < size; j++) {
				assertEquals("at bit " + j, source.getBit(j), reader.readBoolean());
			}
		}
	}
	
	public void testSkip() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			
			int size = r.nextInt(10000) * 8;
			BitVector source = new BitVector(r, size);
			BitReader reader = readerFor(source);
			
			long skipped = 0L;
			long read = 0L;
			while (true) {
				long oldpos = reader.getPosition();
				long toskip = r.nextInt(50);
				long actual = reader.skipBits(toskip);
				assertTrue(actual <= toskip);
				skipped += actual;
				long newpos = reader.getPosition();
				assertEquals(newpos, oldpos + actual);
				if (newpos == size) break;
				assertTrue(actual == toskip);
				int position = (int) reader.getPosition();
				assertEquals("at bit " + position,  source.getBit(position), reader.readBoolean());
				read++;
			}
			assertEquals(size, skipped + read);
		}
	}
	
	public void testRead() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = r.nextInt(100) * 8;
			BitVector source = new BitVector(r, size);
			BitVector reverse = source.mutableCopy();
			reverse.reverse();
			BitReader reader = readerFor(source);

			while (true) {
				int oldpos = (int) reader.getPosition();
				int count = Math.min(size - oldpos, r.nextInt(33));
				int bits = reader.read(count);
				int newpos = (int) reader.getPosition();
				assertEquals(oldpos + count, newpos);
				int actual = (int) reverse.getBits(size - newpos, count);
				assertEquals(actual, bits);
				if (newpos == size) break;
			}
		}
	}
	
}
