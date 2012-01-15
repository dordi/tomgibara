package com.tomgibara.cluster.gvm.space;

import junit.framework.TestCase;

public class GvmVectorSpaceTest extends TestCase {

	private final GvmVectorSpace twoSpace = new GvmVectorSpace(2);
	
	public void testVariance() {
		double[] pt = {1.0 + 2.0, 2.0 + 2.0};
		double[] ptSqr = {1.0 + 4.0, 4.0 + 4.0};
		double var = twoSpace.variance(2.0, pt, ptSqr);
		assertEquals(0.25 * 2, var);
	}

	public void testVarianceWithPoint() {
		double mA = 1.0;
		double[] ptA = {1.0, 2.0};
		double[] ptSqrA = {1.0, 4.0};
		
		double mB = 1.0;
		double[] ptB = {2.0, 2.0};
		double var = twoSpace.variance(mA, ptA, ptSqrA, mB, ptB);
		
		assertEquals(0.25 * 2, var);
	}
	
}
