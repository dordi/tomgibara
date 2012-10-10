package com.tomgibara.geo;

public interface LatLonHeightTransform {

	LatLonHeight transform(LatLonHeight latLonHeight) throws TransformUnavailableException;
	
}
