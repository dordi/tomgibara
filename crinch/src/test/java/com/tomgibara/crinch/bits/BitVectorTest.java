package com.tomgibara.crinch.bits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
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
	
	public void testEqualityAndHash() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testEqualityAndHash(vs[j]);
			}
		}
	}
	
	private void testEqualityAndHash(BitVector v) {
		assertEquals(v, v);
		int size = v.size();
		BitVector w = new BitVector(size+1);
		w.setVector(0, v);
		assertFalse(w.equals(v));
		assertFalse(v.equals(w));
		BitVector x = new BitVector(size);
		for (int i = 0; i < size; i++) x.setBit(i, v.getBit(i));
		assertEquals(v, x);
		assertEquals(x, v);
		assertEquals(v.hashCode(), x.hashCode());
		
		for (int i = 0; i < size; i++) {
			x.flipBit(i);
			assertFalse(v.equals(x));
			assertFalse(x.equals(v));
			x.flipBit(i);
		}
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

	public void testToByteArray() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testToByteArray(vs[j]);
			}
		}
	}
	
	private void testToByteArray(BitVector v) {
		String s = v.toString();
		int d = s.length() % 8;
		if (d != 0) {
			StringBuilder sb = new StringBuilder(s.length() + 8 - d);
			for (; d < 8; d++) sb.append('0');
			s = sb.append(s).toString();
		}
		byte[] bytes = new byte[s.length()/8];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(i * 8, (i+1)*8), 2);
		}
		assertTrue(Arrays.equals(bytes, v.toByteArray()));
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
		
		//check view vectors
		BitVector w = v.rangeView(64, 128);
		testNumberMethods(w, -1);
		w = v.rangeView(63, 128);
		testNumberMethods(w, -1);
		
		//check empty vector
		v = new BitVector(0);
		testNumberMethods(v, 0);
		
	}

	private void testNumberMethods(BitVector v, long value) {
		assertEquals((byte) value, v.byteValue());
		assertEquals((short) value, v.shortValue());
		assertEquals((int) value, v.intValue());
		assertEquals(value, v.longValue());
	}

	public void testBigIntValue() {
		BitVector v = new BitVector(1024);
		v.set(true);
		int f = 512;
		int t = 512;
		BigInteger i = BigInteger.ONE;
		while (f > 0 && t < 1024) {
			//evens
			BitVector e = v.rangeView(f, t);
			assertEquals(i.subtract(BigInteger.ONE), e.bigIntValue());
			i = i.shiftLeft(1);
			f--;
			//odds
			BitVector o = v.rangeView(f, t);
			assertEquals(i.subtract(BigInteger.ONE), o.bigIntValue());
			i = i.shiftLeft(1);
			t++;
		}
		
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
		if (v.size() == 0) return;
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
				testCloneViewAndCopy(vs[j]);
			}
		}
	}

	private void testCloneViewAndCopy(BitVector v) {
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

	
	public void testResizedCopy() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testResizedCopy(vs[j]);
			}
		}
	}
	
	private void testResizedCopy(BitVector v) {
		int size = v.size();
		
		int a = size == 0 ? 0 : random.nextInt(size);
		BitVector w = v.resizedCopy(a);
		assertEquals(v.rangeView(0, w.size()), w);
		
		w = v.resizedCopy(size);
		assertEquals(v, w);
		
		a = size == 0 ? 1 : size + random.nextInt(size);
		w = v.resizedCopy(a);
		assertEquals(v, w.rangeView(0, size));
		w.isRangeAllZeros(size, w.size());
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

	public void testIsAll() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testIsAll(vs[j]);
			}
		}
	}
	
	private void testIsAll(BitVector v) {
		v.set(false);
		assertTrue(v.isAllZeros());
		v.set(true);
		assertTrue(v.isAllOnes());
		int reps = v.size();
		for (int i = 0; i < reps; i++) {
			int a = random.nextInt(v.size()+1);
			int b = a + random.nextInt(v.size()+1-a);
			v.setRange(a, b, false);
			assertTrue(v.isRangeAllZeros(a, b));
			v.setRange(a, b, true);
			assertTrue(v.isRangeAllOnes(a, b));
		}
	}
	
	public void testCompare() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testCompare(vs[j]);
			}
		}
	}

	private void testCompare(BitVector v) {
		int size = v.size();
		assertTrue(v.testEquals(v));
		assertTrue(v.testContains(v));
		if (!v.isAllZeros()) assertTrue(v.testIntersects(v));
		
		BitVector w = v.alignedCopy(true);
		assertTrue(v.testEquals(w));
		assertTrue(w.testEquals(v));
		assertTrue(v.testContains(w));
		assertTrue(w.testContains(v));
		if (!v.isAllZeros()) {
			assertTrue(v.testIntersects(w));
			assertTrue(w.testIntersects(v));
		}
		
		w = v.alignedCopy(true);
		for (int i = 0; i < size; i++) {
			w.setBit(i, true);
			assertTrue( w.testContains(v) );
			assertTrue( v.testEquals(w) || !v.testContains(w) );
		}
		
		w = v.alignedCopy(true);
		for (int i = 0; i < size; i++) {
			w.setBit(i, false);
			assertTrue( v.testContains(w) );
			assertTrue( w.testEquals(v) || !w.testContains(v) );
		}
		
	}

	public void testReadAndWrite() throws Exception {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testReadAndWrite(vs[j]);
			}
		}
	}

	private void testReadAndWrite(BitVector v) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		v.write(out);
		byte[] bytes = out.toByteArray();
		assertTrue(Arrays.equals(v.toByteArray(), bytes));
		
		BitVector w = new BitVector(v.size());
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		w.read(in);
		assertEquals(v, w);
	}
	
	public void testRotation() {
		BitVector v = new BitVector(32);
		v.setBit(0, true);
		for (int i = 0; i < 32; i++) {
			assertEquals(1 << i, v.intValue());
			v.rotate(1);
		}
		
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testRotation(vs[j]);
			}
		}
	}

	private void testRotation(BitVector v) {
		BitVector w = v.copy();
		int d = random.nextInt();
		for (int i = 0; i < v.size(); i++) v.rotate(d);
		assertEquals(w, v);
	}

	public void testShift() {
		BitVector v = new BitVector(32);
		v.setBit(0, true);
		for (int i = 0; i < 32; i++) {
			assertEquals(1 << i, v.intValue());
			v.shift(1, false);
		}
		
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testShift(vs[j]);
			}
		}
	}

	private void testShift(BitVector v) {
		int size = v.size();
		int scope = size == 0 ? 4 : size * 3;
		int d = random.nextInt(scope) - scope/2;
		BitVector w = v.copy();
		v.shift(d, true);
		if (d > 0) {
			if (d >= size) {
				assertTrue(v.isAllOnes());
			} else {
				assertTrue( v.isRangeAllOnes(0, d) );
				assertTrue( v.rangeView(d, size).testEquals(w.rangeView(0, size - d)) );
			}
		} else {
			if (d <= -size) {
				assertTrue(v.isAllOnes());
			} else {
				assertTrue( v.isRangeAllOnes(size + d, size));
				assertTrue( v.rangeView(0, size + d).testEquals(w.rangeView(-d, size)));
			}
		}
	}

	public void testListIterator() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testListIterator(vs[j]);
			}
		}
	}

	private void testListIterator(BitVector v) {
		int size = v.size();
		
		final BitVector w = new BitVector(size);
		ListIterator<Boolean> i = v.listIterator();
		while (i.hasNext()) {
			w.setBit(i.nextIndex(), i.next());
		}
		assertEquals(v, w);
		
		final BitVector x = new BitVector(size);
		i = v.listIterator(size);
		while (i.hasPrevious()) {
			x.setBit(i.previousIndex(), i.previous());
		}
		assertEquals(v, x);
		
		final int a = random.nextInt(size + 1);
		i = v.listIterator(a);
		if (a == size) {
			assertEquals(-1, i.nextIndex());
		} else {
			assertEquals(a, i.nextIndex());
			assertEquals(v.getBit(a), i.next().booleanValue());
		}
		
		i = v.listIterator(a);
		if (a == 0) {
			assertEquals(-1, i.previousIndex());
		} else {
			assertEquals(a - 1, i.previousIndex());
			assertEquals(v.getBit(a - 1), i.previous().booleanValue());
		}
	}

	public void testReverse() {
		for (int i = 0; i < 10; i++) {
			BitVector[] vs = randomVectorFamily(10);
			for (int j = 0; j < vs.length; j++) {
				testReverse(vs[j]);
			}
		}
	}

	private void testReverse(BitVector v) {
		BitVector w = v.copy();
		w.reverse();
		ListIterator<Boolean> i = v.listIterator();
		ListIterator<Boolean> j = w.listIterator(v.size());
		while (i.hasNext()) {
			assertEquals(i.next(), j.previous());
		}
		w.reverse();
		assertEquals(v, w);
	}

	
	
}
