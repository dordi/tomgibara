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

/**
 * A latitude, longitude pair combined with a height.
 * 
 * @author Tom Gibara
 */

public final class LatLonHeight {

	private final LatLon latLon;
	private final double height;
	
	LatLonHeight(LatLon latLon, double height) {
		if (latLon == null) throw new IllegalArgumentException("null latLon");
		if (!GeoUtil.isCoordinate(height)) throw new IllegalArgumentException("invalid height");
		this.latLon = latLon;
		this.height = height;
	}
	
	public LatLon getLatLon() {
		return latLon;
	}
	
	public double getHeight() {
		return height;
	}
	
	public Cartesian toCartesian() {
		return latLon.getDatum().latLonHeightToCartesian(this);
	}

	@Override
	public int hashCode() {
		return latLon.hashCode() ^ GeoUtil.hashCode(height);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof LatLonHeight)) return false;
		LatLonHeight that = (LatLonHeight) obj;
		if (this.height != that.height) return false;
		if (!this.latLon.equals(that.latLon)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s at %.6fm", latLon, height);
	}
	
}
