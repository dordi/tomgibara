/*
 * Copyright 2007 Tom Gibara
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
package com.tomgibara.cluster.gvm;

import java.io.Serializable;
import java.util.Arrays;

import com.tomgibara.cluster.NUMBER;

/**
 * A snapshot of a cluster that has been produced as the result of clustering a
 * number of coordinates.
 * 
 * @author Tom Gibara
 * 
 * @param <K>
 *            the key type
 */

public class GvmResult<K> implements Serializable {

	/**
	 * The number of points in the cluster.
	 */
	
	private int count;
	
	/**
	 * The aggregate mass of the cluster.
	 */
	
	private NUMBER mass;
	
	/**
	 * The coordinates of the cluster's centroid.
	 */
	
	private final NUMBER[] coords;
	
	/**
	 * The variance of the cluster.
	 */
	
	private double variance;
	
	/**
	 * The key associated with the cluster.
	 */
	
	private K key;
	//TODO consider adding key mass

	/**
	 * Creates a new result of the given dimension.
	 * 
	 * @param dimension the number of dimensions in the result
	 */
	
	public GvmResult(int dimension) {
		coords = new NUMBER[dimension];
	}
	
	GvmResult(GvmCluster<K> cluster) {
		count = cluster.count;
		mass = cluster.m0;
		variance = cluster.var;
		key = cluster.key;
		
		NUMBER[] m1 = cluster.m1;
		coords = new NUMBER[m1.length];
		for (int i = 0; i < coords.length; i++) {
			coords[i] = NUMBER.quotient(m1[i], mass);
		}
	}
	
	/**
	 * The number of points in the cluster.
	 */
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	/**
	 * The aggregate mass of the cluster.
	 */
	
	public NUMBER getMass() {
		return mass;
	}
	
	/**
	 * Sets the aggregate mass of the cluster.
	 */
	
	public void setMass(NUMBER mass) {
		this.mass = mass;
	}
	
	/**
	 * The coordinates of the cluster's centroid. The returned array should not
	 * be modified.
	 */

	public NUMBER[] getCoords() {
		return coords;
	}
	
	/**
	 * Sets the coordinates of the cluster's centroid. The values of the
	 * supplied array are copied.
	 */

	public void setCoords(NUMBER[] coords) {
		System.arraycopy(coords, 0, this.coords, 0, Math.min(this.coords.length, coords.length));
	}

	/**
	 * The variance of the cluster.
	 */
	
	public double getVariance() {
		return variance;
	}
	
	/**
	 * Sets the variance of the cluster.
	 */
	
	public void setVariance(double variance) {
		this.variance = variance;
	}
	
	/**
	 * The key associated with the cluster.
	 */
	
	public K getKey() {
		return key;
	}
	
	/**
	 * Sets the key associated with the cluster.
	 */
	
	public void setKey(K key) {
		this.key = key;
	}
	
	// object methods
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Arrays.toString(coords));
		sb.append(String.format("  count: %d  variance: %3.3f  mass: %3.3f  key: %s", count, variance, mass, key));
		return sb.toString();
	}
}
