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


}
