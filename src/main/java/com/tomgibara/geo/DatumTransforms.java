/*
 * Copyright 2012 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.tomgibara.geo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * A collection of transforms that can transform points defined in different datums.
 * 
 * @author Tom Gibara
 */

public class DatumTransforms {

	private static final DatumTransforms defaultTransforms;
	
	static {
		DatumTransforms dts = new DatumTransforms();
		dts.addTransform(
				Datum.WSG84, Datum.OSGB36,
				HelmertTransform.withMPpmArcSecs(-446.448, 125.157, -542.06, 20.4894, -0.1502, -0.247, -0.8421)
				);
		dts.addTransform(
				Datum.WSG84, Datum.OSI65,
				HelmertTransform.withMPpmArcSecs(-482.53, 130.596, -564.557, -8.15, 1.042, 0.214, 0.631)
				);
		defaultTransforms = dts.immutableCopy();
	}
	
	public static DatumTransforms getDefaultTransforms() {
		return defaultTransforms;
	}
	
	private final Map<Mapping, CartesianTransform> transforms;
	private final Map<Datum, Map<Datum, CartesianTransform>> transformsBySource;
	private final Map<Datum, Map<Datum, CartesianTransform>> transformsByTarget;
	private final boolean immutable;
	
	public DatumTransforms() {
		transforms = new HashMap<DatumTransforms.Mapping, CartesianTransform>();
		transformsBySource = new LinkedHashMap<Datum, Map<Datum, CartesianTransform>>();
		transformsByTarget = new LinkedHashMap<Datum, Map<Datum, CartesianTransform>>();
		immutable = false;
	}
	
	private DatumTransforms(DatumTransforms that, boolean immutable) {
		this.transforms = new HashMap<DatumTransforms.Mapping, CartesianTransform>(that.transforms);
		this.transformsBySource = new LinkedHashMap<Datum, Map<Datum, CartesianTransform>>(that.transformsBySource);
		this.transformsByTarget = new LinkedHashMap<Datum, Map<Datum, CartesianTransform>>(that.transformsByTarget);
		this.immutable = immutable;
	}

	public boolean isImmutable() {
		return immutable;
	}
	
	public boolean addTransform(Datum source, Datum target, CartesianTransform transform) {
		if (source == null) throw new IllegalArgumentException("null source");
		if (target == null) throw new IllegalArgumentException("null target");
		if (transform == null) throw new IllegalArgumentException("null transform");
		if (immutable) throw new IllegalStateException();
		if (source.equals(target)) return false;
		Mapping mapping = new Mapping(source, target);
		CartesianTransform current = transforms.get(mapping);
		if (transform.equals(current)) return false;
		addTransform(mapping, transform);
		addTransform(mapping.getInverse(), transform.getInverse());
		return true;
	}
	
	public DatumTransform getTransform(Datum target) {
		if (target == null) throw new IllegalArgumentException("null target datum");
		if (!immutable) throw new IllegalStateException("transforms not immutable");
		return new DatumTransformImpl(target);
	}
	
	public DatumTransforms immutableCopy() {
		return immutable ? this : new DatumTransforms(this, true);
	}
	
	public DatumTransforms mutableCopy() {
		return new DatumTransforms(this, false);
	}
	
	private void addTransform(Mapping mapping, CartesianTransform transform) {
		transforms.put(mapping, transform);
		addTransform(transformsBySource, mapping, transform);
		addTransform(transformsByTarget, mapping.getInverse(), transform);
	}

	private void addTransform(Map<Datum, Map<Datum, CartesianTransform>> maps, Mapping mapping, CartesianTransform transform) {
		Map<Datum, CartesianTransform> map = maps.get(mapping.source);
		if (map == null) {
			map = new LinkedHashMap<Datum, CartesianTransform>();
			maps.put(mapping.source, map);
		}
		map.put(mapping.target, transform);
	}
	
	private static class Mapping {
		
		private final Datum source;
		private final Datum target;
		
		public Mapping(Datum source, Datum target) {
			this.source = source;
			this.target = target;
		}

		Mapping getInverse() {
			return new Mapping(target, source);
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
	
	private class DatumTransformImpl implements DatumTransform {
		
		private final Datum target;
		private final Map<Datum, CartesianTransform> transforms;
		
		DatumTransformImpl(Datum target) {
			this.target = target;
			transforms = transformsByTarget.get(target);
		}
		
		@Override
		public LatLonHeight transform(LatLonHeight latLonHeight) {
			Datum source = latLonHeight.getLatLon().getDatum();
			if (source.equals(target)) return latLonHeight;
			CartesianTransform transform = transforms.get(source);
			Cartesian cartesian;
			if (transform == null) {
				Map<Datum, CartesianTransform> transforms2 = transformsBySource.get(source);
				LinkedHashSet<Datum> targets = new LinkedHashSet<Datum>(transforms2.keySet());
				targets.retainAll(transforms.keySet());
				if (targets.isEmpty()) throw new TransformUnavailableException();
				Datum intermediate = targets.iterator().next();
				cartesian = transforms2.get(intermediate).transform(latLonHeight.toCartesian());
				cartesian = transforms.get(intermediate).transform(cartesian);
			} else {
				cartesian = transform.transform(latLonHeight.toCartesian());
			}
			return cartesian.toLatLonHeight(target);
		}
		
	}
	
}
