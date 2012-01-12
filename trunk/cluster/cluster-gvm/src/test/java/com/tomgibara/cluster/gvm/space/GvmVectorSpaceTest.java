package com.tomgibara.cluster.gvm.space;

import com.tomgibara.cluster.gvm.space.GvmVectorSpace.Vector;

import junit.framework.TestCase;

public class GvmVectorSpaceTest extends TestCase {

	private final GvmVectorSpace twoSpace = new GvmVectorSpace(2);
	
	public void testVariance() {
		Vector pt = twoSpace.newVector(new double[] {1.0 + 2.0, 2.0 + 2.0});
		Vector ptSqr = twoSpace.newVector(new double[] {1.0 + 4.0, 4.0 + 4.0});
		double var = twoSpace.variance(2.0, pt, ptSqr);
		assertEquals(0.25, var);
	}

	public void testVarianceWithPoint() {
		double mA = 1.0;
		Vector ptA = twoSpace.newVector(new double[] {1.0, 2.0});
		Vector ptSqrA = twoSpace.newVector(new double[] {1.0, 4.0});
		
		double mB = 1.0;
		Vector ptB = twoSpace.newVector(new double[] {2.0, 2.0});
		double var = twoSpace.variance(mA, ptA, ptSqrA, mB, ptB);
		
		assertEquals(0.25, var);
	}
	
}
