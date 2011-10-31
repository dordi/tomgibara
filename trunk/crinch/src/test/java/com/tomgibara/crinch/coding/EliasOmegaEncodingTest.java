package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

public class EliasOmegaEncodingTest extends TestCase {

	private final ExtendedCoding coding = EliasOmegaEncoding.extended;
	
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

    public void testSignedInt() {
        int[] memory = new int[4];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 128, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 128, 0);
    	for (int i = -10000; i < 10000; i++) {
    		checkInt(writer, reader, i);
    	}
    	
    	// tests that fit into int manipulation
    	checkInt(writer, reader, 1-(1 << 30));
    	checkInt(writer, reader, -(1 << 30));
    	checkInt(writer, reader, 1 - (1 << 30));
    	checkInt(writer, reader, (1 << 30));
    	checkInt(writer, reader, 1 + (1 << 30));
    	
    	//tests that exceed int manipulation
    	checkInt(writer, reader, -(1 << 30) - 1);
    	
    	Random r = new Random(0L);
    	for (int i = -0; i < 1000000; i++) {
    		checkInt(writer, reader, r.nextInt());
    	}

    }

    private void checkInt(MemoryBitWriter writer, MemoryBitReader reader, int i) {
        writer.setPosition(0);
        coding.encodeSignedInt(writer, i);
        writer.flush();
        reader.setPosition(0);
        int j = coding.decodeSignedInt(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    public void testSignedLong() {
        int[] memory = new int[4];
        MemoryBitWriter writer = new MemoryBitWriter(memory, 128, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, 128, 0);
    	for (long i = -10000; i < 10000; i++) {
    		checkLong(writer, reader, i);
    	}
    	
    	// tests that fit into int manipulation
    	checkLong(writer, reader, 1L-(1L << 62));
    	checkLong(writer, reader, -(1L << 62));
    	checkLong(writer, reader, 1L - (1L << 62));
    	checkLong(writer, reader, (1L << 62));
    	checkLong(writer, reader, 1L + (1L << 62));
    	
    	//tests that exceed int manipulation
    	checkLong(writer, reader, -(1L << 62) - 1L);
    	
    	Random r = new Random(0L);
    	for (int i = 0; i < 1000000; i++) {
    		checkLong(writer, reader, r.nextLong());
    	}

    }

    private void checkLong(MemoryBitWriter writer, MemoryBitReader reader, long i) {
        writer.setPosition(0);
        coding.encodeSignedLong(writer, i);
        writer.flush();
        reader.setPosition(0);
        long j = coding.decodeSignedLong(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    public void testSignedBigInt() {
    	int bits = 10240;
        int[] memory = new int[bits / 8];
        MemoryBitWriter writer = new MemoryBitWriter(memory, bits, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, bits, 0);

    	for (long i = 1; i < 10000000000L; i+=1000000L) {
   			checkPositiveBigInt(writer, reader, BigInteger.valueOf(i));
    	}

        Random r = new Random(0L);
    	for (int i = 0; i < 10000; i++) {
    		checkBigInt(writer, reader, new BigInteger(r.nextInt(bits/4), r));
    	}

    }

    private void checkPositiveBigInt(MemoryBitWriter writer, MemoryBitReader reader, BigInteger i) {
        writer.setPosition(0);
        coding.encodePositiveBigInt(writer, i);
        writer.flush();
        reader.setPosition(0);
        BigInteger j = coding.decodePositiveBigInt(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    private void checkBigInt(MemoryBitWriter writer, MemoryBitReader reader, BigInteger i) {
        writer.setPosition(0);
        coding.encodeSignedBigInt(writer, i);
        writer.flush();
        reader.setPosition(0);
        BigInteger j = coding.decodeSignedBigInt(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    public void testDouble() {
    	int bytes = 16;
        int[] memory = new int[bytes];
        MemoryBitWriter writer = new MemoryBitWriter(memory, bytes * 8, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, bytes * 8, 0);
        checkDouble(writer, reader, 0.0);
        checkDouble(writer, reader, -0.0);
        checkDouble(writer, reader, 1.0);
        checkDouble(writer, reader, 2.0);
        checkDouble(writer, reader, 3.0);
        checkDouble(writer, reader, 4.0);

        {
	        int count = 0;
	        long sum = 0;
	        for (double d = -100.0; d < 100.0; d += 0.1) {
	            sum += checkDouble(writer, reader, d);
	            count++;
			}
	        //System.out.println(sum +" / " + count + " = " + (sum / (double) count));
        }
        
        {
	        int count = 0;
	        long sum = 0;
		    Random r = new Random(0L);
			for (int i = 0; i < 10000; i++) {
				double d = Double.longBitsToDouble(r.nextLong());
				if (Double.isNaN(d) || Double.isInfinite(d)) continue;
				sum += checkDouble(writer, reader, d);
	            count++;
			}
		    //System.out.println(sum +" / " + count + " = " + (sum / (double) count));
        }

    }

    private long checkDouble(MemoryBitWriter writer, MemoryBitReader reader, double d) {
        writer.setPosition(0);
        coding.encodeDouble(writer, d);
        writer.flush();
        reader.setPosition(0);
        double e = coding.decodeDouble(reader);
        assertEquals(d, e);
        return reader.getPosition();
    }
    
    public void testDecimal() {
    	int bits = 10240;
        int[] memory = new int[bits / 8];
        MemoryBitWriter writer = new MemoryBitWriter(memory, bits, 0);
        MemoryBitReader reader = new MemoryBitReader(memory, bits, 0);

        Random r = new Random(0L);
    	for (int i = 0; i < 10000; i++) {
    		checkDecimal(writer, reader, new BigDecimal(new BigInteger(r.nextInt(bits/4), r), r.nextInt(100) - 50));
    	}

    }

    private void checkDecimal(MemoryBitWriter writer, MemoryBitReader reader, BigDecimal d) {
        writer.setPosition(0);
        coding.encodeDecimal(writer, d);
        writer.flush();
        reader.setPosition(0);
        BigDecimal e = coding.decodeDecimal(reader);
        assertEquals(d, e);
        reader.setPosition(0);
    }
    

}
