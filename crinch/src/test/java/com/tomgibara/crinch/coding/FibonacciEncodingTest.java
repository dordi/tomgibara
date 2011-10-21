package com.tomgibara.crinch.coding;

import java.util.Random;

import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

import junit.framework.TestCase;

public class FibonacciEncodingTest extends TestCase {
	
	//TODO split
    public void testGeneral() {
        int[] memory = new int[3];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 96, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 96, 0);
        for (int i = 1; i <= 12; i++) {
            FibonacciEncoding.encode(i, writer);
            writer.setPosition(0);
            //System.out.println(String.format("%3d = %s", i, writer));
            reader.setPosition(0);
            int j = FibonacciEncoding.decode(reader);
            assertEquals(i, j);
        }

        FibonacciEncoding.encode(2057509736, writer);
        writer.setPosition(0);
        reader.setPosition(0);
        FibonacciEncoding.decode(reader);
        writer.setPosition(0);
        writer.writeZeros(96);
        writer.setPosition(0);
        FibonacciEncoding.encode(3005096, writer);
        writer.setPosition(0);
        reader.setPosition(0);
        FibonacciEncoding.decode(reader);
        
        Random r = new Random();

        for (int i = 0; i < 100000; i++) {
            int j = -1;
            while (j < 1) j = r.nextInt();
            FibonacciEncoding.encode(j, writer);
            writer.setPosition(0);
            reader.setPosition(0);
            int k = FibonacciEncoding.decode(reader);
            assertEquals(j, k);
        }
        
        for (int i = 0; i < 100000; i++) {
            long l = -1;
            while (l < 1) l = r.nextLong();
            FibonacciEncoding.encode(l, writer);
            writer.setPosition(0);
            reader.setPosition(0);
            long m = FibonacciEncoding.decodeLong(reader);
            assertEquals(l, m);
        }
        
    }

}