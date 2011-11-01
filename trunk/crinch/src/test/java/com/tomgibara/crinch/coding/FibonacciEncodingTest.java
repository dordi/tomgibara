package com.tomgibara.crinch.coding;

import java.util.Random;

import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

public class FibonacciEncodingTest extends ExtendedCodingTest {
	
	@Override
	ExtendedCoding getCoding() {
		return FibonacciEncoding.extended;
	}
	
    public void testGeneral() {
        int[] memory = new int[3];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 96, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 96, 0);
        for (int i = 1; i <= 12; i++) {
            coding.encodePositiveInt(writer, i);
            writer.setPosition(0);
            //System.out.println(String.format("%3d = %s", i, writer));
            reader.setPosition(0);
            int j = coding.decodePositiveInt(reader);
            assertEquals(i, j);
        }

        coding.encodePositiveInt(writer, 2057509736);
        writer.setPosition(0);
        reader.setPosition(0);
        coding.decodePositiveInt(reader);
        writer.setPosition(0);
        writer.writeZeros(96);
        writer.setPosition(0);
        coding.encodePositiveInt(writer, 3005096);
        writer.setPosition(0);
        reader.setPosition(0);
        coding.decodePositiveInt(reader);
        
        Random r = new Random(0L);

        for (int i = 0; i < 100000; i++) {
            int j = -1;
            while (j < 1) j = r.nextInt();
            coding.encodePositiveInt(writer, j);
            writer.setPosition(0);
            reader.setPosition(0);
            int k = coding.decodePositiveInt(reader);
            assertEquals(j, k);
        }
        
        for (int i = 0; i < 100000; i++) {
            long l = -1;
            while (l < 1) l = r.nextLong();
            coding.encodePositiveLong(writer, l);
            writer.setPosition(0);
            reader.setPosition(0);
            long m = coding.decodePositiveLong(reader);
            assertEquals(l, m);
        }
        
    }

}