package com.tomgibara.cluster.gvm.space;

import java.util.Arrays;
import java.util.List;

import com.tomgibara.cluster.ClusterPainter;
import com.tomgibara.cluster.gvm.GvmResult;
import com.tomgibara.cluster.gvm.GvmSpace;

public class GvmVectorSpace extends GvmSpace {

	private final int dimensions;
	
	private static double[] coords(Object obj) {
		return (double[]) obj;
	}
	
	public GvmVectorSpace(int dimensions) {
		if (dimensions < 1) throw new IllegalArgumentException("non-positive dimensions");
		this.dimensions = dimensions;
	}
	
	public int getDimensions() {
		return dimensions;
	}
	
	@Override
	public double[] newOrigin() {
		return new double[dimensions];
	}

	@Override
	public double[] newCopy(Object pt) {
		return coords(pt).clone();
	}

	@Override
	public double magnitudeSqr(Object pt) {
		double sum = 0.0;
		double[] coords = coords(pt);
		for (int i = 0; i < dimensions; i++) {
			double c = coords[i];
			sum += c * c;
		}
		return sum;
	}
	
	@Override
	public double sum(Object pt) {
		double sum = 0.0;
		for (double coord : coords(pt)) {
			sum += coord;
		}
		return sum;
	}

	@Override
	public void setToOrigin(Object pt) {
		Arrays.fill(coords(pt), 0.0);
	}

	@Override
	public void setTo(Object dstPt, Object srcPt) {
		System.arraycopy(coords(srcPt), 0, coords(dstPt), 0, dimensions);
	}

	@Override
	public void setToScaled(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			dstCoords[i] = m * srcCoords[i];
		}
	}

	@Override
	public void setToScaledSqr(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			double c = srcCoords[i];
			dstCoords[i] = m * c * c;
		}
	}

	@Override
	public void add(Object dstPt, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			dstCoords[i] += srcCoords[i];
		}
	}

	@Override
	public void addScaled(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			dstCoords[i] += m * srcCoords[i];
		}
	}

	@Override
	public void addScaledSqr(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			double c = srcCoords[i];
			dstCoords[i] += m * c * c;
		}
	}

	@Override
	public void subtract(Object dstPt, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			dstCoords[i] -= srcCoords[i];
		}
	}

	@Override
	public void subtractScaled(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			dstCoords[i] -= m * srcCoords[i];
		}
	}

	@Override
	public void subtractScaledSqr(Object dstPt, double m, Object srcPt) {
		double[] dstCoords = coords(dstPt);
		double[] srcCoords = coords(srcPt);
		for (int i = 0; i < dimensions; i++) {
			double c = srcCoords[i];
			dstCoords[i] -= m * c * c;
		}
	}

	@Override
	public void scale(Object pt, double m) {
		double[] coords = coords(pt);
		for (int i = 0; i < dimensions; i++) {
			coords[i] *= m;
		}
	}

	@Override
	public void square(Object pt) {
		double[] coords = coords(pt);
		for (int i = 0; i < dimensions; i++) {
			coords[i] *= coords[i];
		}
	}
	
	public <R extends GvmResult<?>, P> ClusterPainter<R, P> painter(List<P> paints) {
		return new ClusterPainter<R, P>(new Sizer<R>(), paints);
	}

	private class Sizer<R extends GvmResult<?>> implements ClusterPainter.Sizer<R> {

		@Override
		public double distance(R r1, R r2) {
			return GvmVectorSpace.this.distance(r1.getPoint(), r2.getPoint());
		}

		@Override
		public double radius(R r) {
			return Math.sqrt(r.getVariance() * 2); // 2 std deviations
		}

		@Override
		public long points(R r) {
			return r.getCount();
		}
		
	}
	
}
