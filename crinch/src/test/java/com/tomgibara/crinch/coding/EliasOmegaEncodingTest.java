package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

import junit.framework.TestCase;

public class EliasOmegaEncodingTest extends TestCase {

    public void testCorrectness() {
        int[] memory = new int[1];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 32, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 32, 0);
        for (int i = 1; i <= 10; i++) {
            writer.setPosition(0);
            EliasOmegaEncoding.encode(i, writer);
            writer.flush();
            reader.setPosition(0);
            int j = EliasOmegaEncoding.decode(reader);
            assertEquals(i, j);
        }
    }

    public void testSigned() {
        int[] memory = new int[4];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 128, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 128, 0);
    	for (int i = -10000; i < 10000; i++) {
    		checkInt(writer, reader, i);
    	}
    	
    	checkInt(writer, reader, 1-(1 << 30));
    	checkInt(writer, reader, -(1 << 30));
    	checkInt(writer, reader, 1 - (1 << 30));
    	checkInt(writer, reader, (1 << 30));
    	checkInt(writer, reader, 1 + (1 << 30));
    }

    private void checkInt(MemoryBitWriter writer, MemoryBitReader reader, int i) {
        writer.setPosition(0);
        EliasOmegaEncoding.encodeSigned(i, writer);
        writer.flush();
        reader.setPosition(0);
        int j = EliasOmegaEncoding.decodeSigned(reader);
        assertEquals(i, j);
    }
    
}
