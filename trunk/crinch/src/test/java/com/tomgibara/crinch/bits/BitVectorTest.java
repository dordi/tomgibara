package com.tomgibara.crinch.bits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Pattern;

import com.tomgibara.crinch.bits.BitVector;

import junit.framework.TestCase;

public class BitVectorTest extends TestCase {

	private static final Pattern ONE = Pattern.compile("1");
	private static final Pattern ZERO = Pattern.compile("0");
	
	private static final Random random = new Random(0);
	
	private static BitVector[] randomVectorFamily(int length, int size) {
		BitVector v = randomVector(length);
		BitVector[] vs = new BitVector[size + 1];
		vs[0] = v;
		for (int i = 0; i < size; i++) {
			int a = random.nextInt(v.size()+1);
			int b = a + random.nextInt(v.size()+1-a);
			vs[i+1] = v.rangeView(a, b);
		}
		return vs;
	}
	
	private static BitVector[] randomVectorFamily(int size) {
		return randomVectorFamily(random.nextInt(1000), size);
	}
	
	private static BitVector randomVector(int length) {
		//TODO optimize when factory methods are available
		BitVector vector = new BitVector(length);
		for (int i = 0; i < length; i++) {
			vector.setBit(i, random.nextBoolean());
		}
		return vector;
	}
	
	private static BitVector randomVector() {
		return randomVector(random.nextInt(1000));
	}
	
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testToString(vs[j]);
			}
		}
	}
	
	private void testToString(BitVector v) {
		String str = v.toString();
		assertEquals(str.length(), v.size());
		assertEquals(v, new BitVector(str));
	}

	public void testSetBit() throws Exception {
		BitVector v = new BitVector(100);
		for (int i = 0; i < 100; i++) {
			v.setBit(i, true);
			for (int j = 0; j < 100; j++) {
				assertEquals("Mismatch at " + j + " during " + i, j == i, v.getBit(j));
			}
			v.setBit(i, false);
		}
	}
	
	public void testGet() throws Exception {
		//72 long
		BitVector v = new BitVector("100101111011011100001101011001100000101110001011100001110011101101100010");
		assertEquals((byte)new BigInteger("10010111", 2).intValue(), v.getByte(64));
	}

	public void testNumberMethods() {
		//check short vector
		BitVector v = new BitVector(1);
		testNumberMethods(v, 0);
		v.set(true);
		testNumberMethods(v, 1);
		
		//check long vector
		v = new BitVector(128);
		testNumberMethods(v, 0);
		v.set(true);
		testNumberMethods(v, -1);
		
		//check view vector
		v = v.rangeView(64, 128);
		testNumberMethods(v, -1);
		
		//check empty vector
		v = new BitVector(0);
		testNumberMethods(v, 0);
		
	}
	
	private void testNumberMethods(BitVector v, long value) {
		assertEquals((byte) value, v.byteValue());
		assertEquals((short) value, v.shortValue());
		assertEquals((int) value, v.intValue());
		assertEquals(value, v.longValue());
		assertEquals(BigInteger.valueOf(value), v.bigIntValue());
		assertEquals((float) value, v.floatValue());
		assertEquals((double) value, v.doubleValue());
	}
	
	public void testBitCounts() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testBitCounts(vs[j]);
			}
		}
	}
	
	private void testBitCounts(BitVector v) {
		String str = v.toString();
		int totalOneCount = str.replace("0", "").length();
		int totalZeroCount = str.replace("1", "").length();
		assertEquals(v.size(), v.countOnes() + v.countZeros());
		assertEquals(totalOneCount, v.countOnes());
		assertEquals(totalOneCount, v.countOnes(0, v.size()));
		assertEquals(totalZeroCount, v.countZeros());
		assertEquals(totalZeroCount, v.countZeros(0, v.size()));
		int reps = v.size();
		for (int i = 0; i < reps; i++) {
			int a = random.nextInt(v.size()+1);
			int b = a + random.nextInt(v.size()+1-a);
			String s = str.substring(str.length()-b, str.length()-a);
			int oneCount = s.replace("0", "").length();
			int zeroCount = s.replace("1", "").length();
			assertEquals(oneCount, v.countOnes(a, b));
			assertEquals(zeroCount, v.countZeros(a, b));
		}
	}
	
	public void testSetGetBit() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testGetSetBit(vs[j]);
			}
		}
	}

	private void testGetSetBit(BitVector v) {
		BitVector c = v.copy();
		int i = random.nextInt(v.size());
		v.setBit(i, !v.getBit(i));
		c.xorVector(v);
		assertTrue(c.getBit(i));
		assertEquals(1, c.countOnes());
	}
	
	public void testOverlapping() {
		BitVector v = new BitVector("1010101010101010");
		BitVector w = v.rangeView(0, 15);
		v.xorVector(1, w);
		assertEquals(new BitVector("1111111111111110"), v);
		
		v = new BitVector("1010101010101010");
		w = v.rangeView(1, 16);
		v.xorVector(0, w);
		assertEquals(new BitVector("1111111111111111"), v);
	}
	
	public void testCloneViewAndCopy() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				BitVector v = vs[j];
				
				BitVector cl = v.clone();
				assertEquals(v, cl);
				assertNotSame(v, cl);
				
				BitVector cp = v.copy();
				assertEquals(v, cp);
				assertNotSame(v, cp);
				
				BitVector vw = v.view();
				assertEquals(v, vw);
				assertNotSame(v, vw);
				
				//check clone and view are backed by same data
				cl.xor(true);
				cp.xorVector(vw);
				assertEquals(cp.size(), cp.countOnes());
				
				assertTrue(v.isMutable());
				BitVector mu = v.mutable();
				assertSame(v, mu);
				BitVector im = v.immutable();
				assertNotSame(v, im);
				assertFalse(im.isMutable());
				mu = im.mutable();
				assertNotSame(im, mu);
				assertTrue(mu.isMutable());
			}
		}
	}

	public void testMutability() {
		BitVector v = new BitVector(1).immutable();
		try {
			v.modify(BitVector.Operation.SET, true);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.modifyRange(BitVector.Operation.SET, 0, 1, true);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.modifyBit(BitVector.Operation.SET, 0, true);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.modifyBits(BitVector.Operation.SET, 0, 1L, 1);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.modifyVector(BitVector.Operation.SET, v);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.modifyVector(BitVector.Operation.SET, 0, v);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			v.duplicate(false, true);
			fail();
		} catch (IllegalStateException e) {
			//expected
		} 
	}

	public void testSerialization() throws Exception {
		BitVector v1 = randomVector(1000);
		BitVector w1 = v1.view();
		BitVector x1 = v1.copy();
		BitVector y1 = x1.immutableView();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(v1);
		oout.writeObject(w1);
		oout.writeObject(x1);
		oout.writeObject(y1);
		oout.close();
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(in);
		BitVector v2 = (BitVector) oin.readObject();
		BitVector w2 = (BitVector) oin.readObject();
		BitVector x2 = (BitVector) oin.readObject();
		BitVector y2 = (BitVector) oin.readObject();
		oin.close();
		
		assertNotSame(v1, v2);
		assertNotSame(w1, w2);
		assertNotSame(x1, x2);
		assertNotSame(y1, y2);
		
		assertEquals(v1, v2);
		assertEquals(w1, w2);
		assertEquals(x1, x2);
		assertEquals(y1, y2);

		assertTrue(v2.isMutable());
		assertTrue(w2.isMutable());
		assertTrue(x2.isMutable());
		assertFalse(y2.isMutable());

		assertTrue(x2.equals(v2));
		w2.set(true);
		assertEquals(1000, v2.countOnes());
		assertFalse(x2.equals(v2));
		x2.set(true);
		assertEquals(1000, y2.countOnes());
		
	}
	
}
