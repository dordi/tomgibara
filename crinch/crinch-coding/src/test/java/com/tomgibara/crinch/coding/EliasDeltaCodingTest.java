package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitWriter;

public class EliasDeltaCodingTest extends ExtendedCodingTest {

	@Override
	ExtendedCoding getCoding() {
		return EliasDeltaCoding.extended;
	}

    public void testCorrectness() {
        int[] memory = new int[1];
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, 32);
        IntArrayBitReader reader = new IntArrayBitReader(memory, 32);
        for (int i = 1; i <= 10; i++) {
            writer.setPosition(0);
            coding.encodePositiveInt(writer, i);
            writer.flush();
            reader.setPosition(0);
            int j = coding.decodePositiveInt(reader);
            assertEquals(i, j);
        }
    }

    public void testSpeed() {
        testSpeed(10000000, 100);
    }
    
    private void testSpeed(int size, int bound) {
        int[] memory = new int[size];
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, size * 32);
        int count = size;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
        	coding.encodePositiveInt(writer, (i % bound) + 1);
            //encode(i + 1, writer);
            //memory[i] = (i % bound) + 1;
            //memory[i] = i;
        }
        writer.flush();
        long finish = System.currentTimeMillis();
        System.out.println(finish-start + " ms to write first " + count + " integers");
        
        IntArrayBitReader reader = new IntArrayBitReader(memory, writer.getSize());
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int v = coding.decodePositiveInt(reader);
            //int v = memory[i];
            if (v != (i % bound) + 1) throw new RuntimeException("on read " + i);
            //if (v != i) throw new RuntimeException("on read " + i);
            //if (v != i+1) throw new RuntimeException("on read " + i);
        }
        finish = System.currentTimeMillis();
        System.out.println(finish-start + " ms to read first " + count + " integers");
    }
    

}
