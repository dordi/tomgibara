package com.tomgibara.crinch.math;

import java.util.Random;

import junit.framework.TestCase;

public class CrinchMathTest extends TestCase {

	private static final float EPSILON = 0.000001f;
	
	private static void assertClose(float x, float y) {
		assertTrue(x + " " + y + " within " + EPSILON, x == y || Math.abs(x-y) < EPSILON * Math.max(x, y));
	}
	
	public void testPowCorrect() {
		
		Random r = new Random(0L);

		for (int i = 0; i < 100; i++) {
			float f = r.nextFloat() * 40;
			int n = r.nextInt(30);
			assertClose((float) Math.pow(f, n), CrinchMath.pow(f, n));
		}
		
	}
	
	public void testPowFast() {
		int tests = 1000000;
		float[] fs = new float[tests];
		int[] ns = new int[tests];
		Random r = new Random(0L);
		for (int i = 0; i < tests; i++) {
			ns[i] = - 40 + r.nextInt(30) * 80;
			fs[i] = r.nextFloat();
		}

		long totalTimeGeom = 0L;
		int repeats = 10;
		for (int j = 0; j < repeats; j++) {
			long start = System.currentTimeMillis();
			float sum = 0f;
			for (int i = 0; i < tests; i++) {
				sum += CrinchMath.pow(fs[i], ns[i]);
			}
			long finish = System.currentTimeMillis();
			totalTimeGeom += finish - start;
			System.out.println(sum); // don't let the JVM optimize away the calculation
		}

		long totalTimeMath = 0L;
		for (int j = 0; j < repeats; j++) {
			long start = System.currentTimeMillis();
			float sum = 0f;
			for (int i = 0; i < tests; i++) {
				sum += Math.pow(fs[i], ns[i]);
			}
			long finish = System.currentTimeMillis();
			totalTimeMath += finish - start;
			System.out.println(sum); // don't let the JVM optimize away the calculation
		}
		
		assertTrue("geom faster", totalTimeGeom < totalTimeMath);
		System.out.println("GEOM: " + totalTimeGeom);
		System.out.println("MATH: " + totalTimeMath);
	}

}
