package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

public class EliasDeltaCodingTest extends ExtendedCodingTest {

	@Override
	ExtendedCoding getCoding() {
		return EliasDeltaCoding.extended;
	}

    public void testCorrectness() {
        int[] memory = new int[1];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 32, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 32, 0);
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
        MemoryBitWriter writer = new MemoryBitWriter(memory, size * 32, 0);
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
        
        MemoryBitReader reader = new MemoryBitReader(memory, writer.getSize(), 0);
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
