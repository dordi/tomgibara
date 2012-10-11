package com.tomgibara.geo;

import static com.tomgibara.geo.GeoUtil.arcSecsToRads;

//TODO could compute more accurately?
public class HelmertTransform implements CartesianTransform {

	public static HelmertTransform withMPpmArcSecs(double cx, double cy, double cz, double s, double rx, double ry, double rz) {
		return new HelmertTransform(cx, cy, cz, s/1000000, arcSecsToRads(rx), arcSecsToRads(ry), arcSecsToRads(rz));
	}

	public final double cx; // centre x (m)
	public final double cy; // centre y (m)
	public final double cz; // centre z (m)
	public final double s;  // scale factor (pre-multiplied)
	public final double rx; // rotation about x (rad)
	public final double ry; // rotation about y (rad)
	public final double rz; // rotation about z (rad)
	
	private final double s1; // scale factor plus one
	
	private HelmertTransform(double cx, double cy, double cz, double s, double rx, double ry, double rz) {
		this.cx = cx;
		this.cy = cy;
		this.cz = cz;
		this.s = s;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
		s1 = 1 + s; 
	}

	public Cartesian transform(Cartesian source) {
		double x = source.getX(), y = source.getY(), z = source.getZ();
		double tx = cx + x * s1 - y * rz + z * ry;
		double ty = cy + x * rz + y * s1 - z * rx;
		double tz = cz - x * ry + y * rx + z * s1;
		return new Cartesian(tx, ty, tz);
	}
	
	@Override
	public HelmertTransform getInverse() {
		return new HelmertTransform(-cx, -cy, -cz, -s, -rx, -ry, -rz);
	}
	
	@Override
	public int hashCode() {
		return GeoUtil.hashCode(s)
				+ GeoUtil.hashCode(cx)
				+ GeoUtil.hashCode(rx)
				+ 31 * (
						GeoUtil.hashCode(cy)
						+ GeoUtil.hashCode(ry)
						+ 31 * (
								GeoUtil.hashCode(cz)
								+ GeoUtil.hashCode(rz)));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof HelmertTransform)) return false;
		HelmertTransform that = (HelmertTransform) obj;
		if (this.cx != that.cx) return false;
		if (this.cy != that.cy) return false;
		if (this.cz != that.cz) return false;
		if (this.s != that.s) return false;
		if (this.rx != that.rx) return false;
		if (this.ry != that.ry) return false;
		if (this.rz != that.rz) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + cx + "," + cy + "," + cz + ") " + s + "[" + rx + "," + ry + "," + rz + "]";
	}
	
}
