package com.tomgibara.geo;

public interface CartesianTransform {

	Cartesian transform(Cartesian source);
	
	CartesianTransform getInverse();
	
}
