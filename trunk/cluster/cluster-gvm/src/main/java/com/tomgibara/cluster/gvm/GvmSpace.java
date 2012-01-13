package com.tomgibara.cluster.gvm;


public abstract class GvmSpace<P extends GvmPoint> {

	//TODO use within cluster
	public static double nonNegative(double v) {
		return v < 0.0 ? 0.0 : v;
	}
	
	public abstract P newOrigin();
	
	public abstract P newCopy(P pt);
	
	public abstract double magnitudeSqr(P pt);

	public abstract double sum(P pt);
	
	//not used in algorithm, but useful - override for good performance
	public double distance(P pt1, P pt2) {
		P p = newCopy(pt1);
		p.subtract(pt2);
		return Math.sqrt(magnitudeSqr(p));
	}
	
	//naive implementation that must be overridden for good performance
	// m - total mass (not zero)
	// pt - aggregate point (prescaled by mass) 
	// ptSqr - aggregate point squared (prescaled by mass)
	public double variance(double m, P pt, P ptSqr) {
		P x = newCopy(ptSqr);
//		// Var is E(X^2) - E(X)^2, but squaring pt1 introduces an extra factor which is the total mass, so:
		double scale = 1 / m;
		x.subtractScaledSqr(scale, pt);
		return sum(x) * scale;
	}

	//naive implementation that must be overridden for good performance
	// m1 - established total mass
	// pt1 - aggregate point (prescaled by mass) 
	// ptSqr1 - aggregate point squared (prescaled by mass)
	// m2 - mass of candidate point
	// pt2 - candidate point (not prescaled by mass)
	// Note: (m1 + m2) never zero
	public double variance(double m1, P pt1, P ptSqr1, double m2, P pt2) {
		// compute the total mass
		double m0 = m1 + m2;
		// compute the new sum
		P pt0 = newCopy(pt1);
		pt0.addScaled(m2, pt2);
		// compute the new sum of squares
		P ptSqr0 = newCopy(ptSqr1);
		ptSqr0.addScaledSqr(m2, pt2);
		// compute the variance
		return variance(m0, pt0, ptSqr0);
	}

	//naive implementation that must be overridden for good performance
	// m1 - established total mass
	// pt1 - aggregate point (prescaled by mass) 
	// ptSqr1 - aggregate point squared (prescaled by mass)
	// m2 - mass of candidate cluster
	// pt2 - candidate cluster point (prescaled by mass)
	// ptSqr2 - candidate cluster point squared (prescaled by mass)
	// Note: (m1 + m2) never zero
	public double variance(double m1, P pt1, P ptSqr1, double m2, P pt2, P ptSqr2) {
		// compute the total mass
		double m0 = m1 + m2;
		// compute the new sum
		P pt0 = newCopy(pt1);
		pt0.add(pt2);
		// compute the new sum of squares
		P ptSqr0 = newCopy(ptSqr1);
		ptSqr0.add(ptSqr2);
		// compute the variance
		return variance(m0, pt0, ptSqr0);
	}

}
