package com.tomgibara.cluster.gvm.space;

import java.util.Arrays;

import com.tomgibara.cluster.gvm.GvmPoint;
import com.tomgibara.cluster.gvm.GvmSpace;

public class GvmVectorSpace extends GvmSpace<GvmVectorSpace.Vector> {

	private final int dimensions;
	
	public GvmVectorSpace(int dimensions) {
		if (dimensions < 1) throw new IllegalArgumentException("non-positive dimensions");
		this.dimensions = dimensions;
	}
	
	public int getDimensions() {
		return dimensions;
	}
	
	public Vector newVector(double[] coords) {
		if (coords == null) throw new IllegalArgumentException("null coords");
		if (coords.length != dimensions) throw new IllegalArgumentException("coords has wrong dimensions");
		return new Vector(coords);
	}
	
	@Override
	public Vector newOrigin() {
		return new Vector(dimensions);
	}

	@Override
	public Vector newCopy(Vector pt) {
		return new Vector((Vector) pt);
	}

	@Override
	public double magnitudeSqr(Vector pt) {
		double sum = 0.0;
		double[] coords = pt.coords;
		for (int i = 0; i < coords.length; i++) {
			double c = coords[i];
			sum += c * c;
		}
		return sum;
	}
	
	@Override
	public double sum(Vector pt) {
		double sum = 0.0;
		for (double coord : pt.coords) {
			sum += coord;
		}
		return sum;
	}

	public static class Vector extends GvmPoint {

		private final double[] coords;
		
		private static double[] coords(GvmPoint pt) {
			return ((Vector) pt).coords;
		}
		
		Vector(int dimensions) {
			coords = new double[dimensions];
		}
		
		Vector(double[] coords) {
			this.coords = coords;
		}
		
		public Vector(Vector that) {
			this.coords = that.coords.clone();
		}

		public void setCoord(int index, double coord) {
			coords[index] = coord;
		}
		
		public double getCoord(int index) {
			return coords[index];
		}
		
		
		@Override
		public void setToOrigin() {
			Arrays.fill(coords, 0.0);
		}

		@Override
		public void setTo(GvmPoint pt) {
			System.arraycopy(coords(pt), 0, coords, 0, coords.length);
		}

		@Override
		public void setToScaled(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				this.coords[i] = m * coords[i];
			}
		}

		@Override
		public void setToScaledSqr(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				double c = coords[i];
				this.coords[i] = m * c * c;
			}
		}

		@Override
		public void add(GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				this.coords[i] += coords[i];
			}
		}

		@Override
		public void addScaled(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				this.coords[i] += m * coords[i];
			}
		}

		@Override
		public void addScaledSqr(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				double c = coords[i];
				this.coords[i] += m * c * c;
			}
		}

		@Override
		public void subtract(GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				this.coords[i] -= coords[i];
			}
		}

		@Override
		public void subtractScaled(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				this.coords[i] -= m * coords[i];
			}
		}

		@Override
		public void subtractScaledSqr(double m, GvmPoint pt) {
			double[] coords = coords(pt);
			for (int i = 0; i < coords.length; i++) {
				double c = coords[i];
				this.coords[i] -= m * c * c;
			}
		}

		@Override
		public void scale(double m) {
			for (int i = 0; i < coords.length; i++) {
				coords[i] *= m;
			}
		}

		@Override
		public void sqr() {
			for (int i = 0; i < coords.length; i++) {
				coords[i] *= coords[i];
			}
		}

		@Override
		public String toString() {
			return Arrays.toString(coords);
		}
		
	}
	
}
