package com.tomgibara.cluster.gvm;

public abstract class GvmPoint {

	public abstract void setToOrigin();
	
	public abstract void setTo(GvmPoint pt);
	
	public abstract void setToScaled(double m, GvmPoint pt);
	
	public abstract void setToScaledSqr(double m, GvmPoint pt);

	public abstract void add(GvmPoint pt);
	
	public abstract void addScaled(double m, GvmPoint pt);
	
	public abstract void addScaledSqr(double m, GvmPoint pt);

	public abstract void subtract(GvmPoint pt);
	
	public abstract void subtractScaled(double m, GvmPoint pt);
	
	public abstract void subtractScaledSqr(double m, GvmPoint pt);
	
	public abstract void scale(double m);
	
	public abstract void sqr();

}
