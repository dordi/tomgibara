/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitWriter;

// TODO should allow number of bits to be configured
public abstract class ExtendedCodingTest extends TestCase {

	abstract ExtendedCoding getCoding();
	
	final ExtendedCoding coding = getCoding();
	
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

    public void testSignedInt() {
        int[] memory = new int[16];
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, 512);
        IntArrayBitReader reader = new IntArrayBitReader(memory, 512);
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

    private void checkInt(IntArrayBitWriter writer, IntArrayBitReader reader, int i) {
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
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, 128);
        IntArrayBitReader reader = new IntArrayBitReader(memory, 128);
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

    private void checkLong(IntArrayBitWriter writer, IntArrayBitReader reader, long i) {
        writer.setPosition(0);
        coding.encodeSignedLong(writer, i);
        writer.flush();
        reader.setPosition(0);
        long j = coding.decodeSignedLong(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    public void testSignedBigInt() {
    	int bits = 4096;
        int[] memory = new int[bits / 32];
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, bits);
        IntArrayBitReader reader = new IntArrayBitReader(memory, bits);

    	for (long i = 1; i < 100L; i++) {
    		checkPositiveBigInt(writer, reader, BigInteger.valueOf(i));
    	}

    	for (long i = 1; i < 10000000000L; i+=1000000L) {
   			checkPositiveBigInt(writer, reader, BigInteger.valueOf(i));
    	}

        Random r = new Random(0L);
    	for (int i = 0; i < 10000; i++) {
    		checkBigInt(writer, reader, new BigInteger(r.nextInt(bits/4), r));
    	}

    }

    private void checkPositiveBigInt(IntArrayBitWriter writer, IntArrayBitReader reader, BigInteger i) {
        writer.setPosition(0);
        coding.encodePositiveBigInt(writer, i);
        writer.flush();
        reader.setPosition(0);
        BigInteger j = coding.decodePositiveBigInt(reader);
        assertEquals(i, j);
        reader.setPosition(0);
    }
    
    private void checkBigInt(IntArrayBitWriter writer, IntArrayBitReader reader, BigInteger i) {
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
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, bytes * 8);
        IntArrayBitReader reader = new IntArrayBitReader(memory, bytes * 8);
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

    private long checkDouble(IntArrayBitWriter writer, IntArrayBitReader reader, double d) {
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
        IntArrayBitWriter writer = new IntArrayBitWriter(memory, bits);
        IntArrayBitReader reader = new IntArrayBitReader(memory, bits);

        Random r = new Random(0L);
    	for (int i = 0; i < 10000; i++) {
    		checkDecimal(writer, reader, new BigDecimal(new BigInteger(r.nextInt(bits/4), r), r.nextInt(100) - 50));
    	}

    }

    private void checkDecimal(IntArrayBitWriter writer, IntArrayBitReader reader, BigDecimal d) {
        writer.setPosition(0);
        coding.encodeDecimal(writer, d);
        writer.flush();
        reader.setPosition(0);
        BigDecimal e = coding.decodeDecimal(reader);
        assertEquals(d, e);
        reader.setPosition(0);
    }
    
}
