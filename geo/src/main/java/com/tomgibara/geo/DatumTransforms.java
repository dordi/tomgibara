package com.tomgibara.geo;

import java.util.HashMap;
import java.util.Map;

public class DatumTransforms {

	private static final DatumTransforms defaultTransforms;
	
	static {
		DatumTransforms dts = new DatumTransforms();
		dts.addTransform(
				Datum.WSG84, Datum.OSGB36,
				HelmertTransform.mPpmArcSecs(-446.448, 125.157, -542.06, 20.4894, -0.1502, -0.247, -0.8421)
				);
		dts.addTransform(
				Datum.WSG84, Datum.OSI65,
				HelmertTransform.mPpmArcSecs(-482.53, 130.596, -564.557, -8.15, 1.042, 0.214, 0.631)
				);
		defaultTransforms = dts.immutableCopy();
	}
	
	public static DatumTransforms getDefaultTransforms() {
		return defaultTransforms;
	}
	
	private final Map<Mapping, CartesianTransform> transforms;
	private final boolean immutable;
	
	public DatumTransforms() {
		transforms = new HashMap<DatumTransforms.Mapping, CartesianTransform>();
		immutable = false;
	}
	
	private DatumTransforms(DatumTransforms that, boolean immutable) {
		this.transforms = new HashMap<DatumTransforms.Mapping, CartesianTransform>(that.transforms);
		this.immutable = immutable;
	}

	public boolean isImmutable() {
		return immutable;
	}
	
	public boolean addTransform(Datum source, Datum target, CartesianTransform transform) {
		if (source == null) throw new IllegalArgumentException("null source");
		if (target == null) throw new IllegalArgumentException("null target");
		if (transform == null) throw new IllegalArgumentException("null transform");
		CartesianTransform previous = transforms.put(new Mapping(source, target), transform);
		if (transform.equals(previous)) return false;
		transforms.put(new Mapping(target, source), transform.getInverse());
		return true;
	}
	
	public LatLonHeightTransform getTransform(Datum target) {
		if (target == null) throw new IllegalArgumentException("null target datum");
		return new DatumTransform(target);
	}
	
	public DatumTransforms immutableCopy() {
		return immutable ? this : new DatumTransforms(this, true);
	}
	
	public DatumTransforms mutableCopy() {
		return new DatumTransforms(this, false);
	}
	
	private static class Mapping {
		
		private final Datum source;
		private final Datum target;
		
		public Mapping(Datum source, Datum target) {
			this.source = source;
			this.target = target;
		}
		
		@Override
		public int hashCode() {
			return source.hashCode() ^ 31 * target.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Mapping)) return false;
			Mapping that = (Mapping) obj;
			if (!this.source.equals(that.source)) return false;
			if (!this.target.equals(that.target)) return false;
			return true;
		}
		
	}
	
	private class DatumTransform implements LatLonHeightTransform {
		
		private final Datum target;
		
		DatumTransform(Datum target) {
			this.target = target;
		}
		
		@Override
		public LatLonHeight transform(LatLonHeight latLonHeight) {
			Datum source = latLonHeight.getLatLon().getDatum();
			if (source.equals(target)) return latLonHeight;
			CartesianTransform transform = transforms.get(new Mapping(source, target));
			if (transform == null) {
				//TODO should try to find transform pairs
				throw new TransformUnavailableException();
			} else {
				return transform.transform(latLonHeight.toCartesian()).toLatLonHeight(target);
			}
		}
		
	}
	
}
