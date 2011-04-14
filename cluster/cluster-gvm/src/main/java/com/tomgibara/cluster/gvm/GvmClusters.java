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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.cluster.NUMBER;

/**
 * Maintains a collection of clusters which are adjusted as new elements are
 * added. The keyer assigned to this object should not be modified while there
 * are clusters.
 * 
 * @author Tom Gibara
 * 
 * @param <K>
 *            the key type
 */

public class GvmClusters<K> {

	// statics
	
	/**
	 * Helper method to avoid propagation of negative variances.
	 * 
	 * @param var
	 *            a variance
	 * @return the variance clamped at zero
	 */
	
	static double correct(double var) {
		return var >= 0.0 ? var : 0.0;
	}
	
	// fields
	
	/**
	 * The greatest number of clusters that will be recorded
	 */
	
	final int capacity;
	
	/**
	 * The number of dimensions over which clustering is being performed.
	 */
	
	final int dimension;

	/**
	 * The keyer used to apply keys to clusters.
	 */
	
	private GvmKeyer<K> keyer = new GvmDefaultKeyer<K>();

	/**
	 * The clusters.
	 */

	private final GvmCluster<K>[] clusters;
	
	/**
	 * All possible cluster pairs.
	 */
	
	private final GvmClusterPairs<K> pairs;
	
	/**
	 * The number of points that have been added.
	 */
	
	private int additions = 0;

	/**
	 * The current number of clusters.
	 */

	private int count = 0;

	/**
	 * The number of clusters prior to reduction
	 */
	
	private int bound = 0;
	
	public GvmClusters(int dimension, int capacity) {
		if (dimension < 0) throw new IllegalArgumentException("negative dimension");
		if (capacity < 0) throw new IllegalArgumentException("negative capacity");
		this.dimension = dimension;
		this.capacity = capacity;
		this.clusters = new GvmCluster[capacity];
		pairs = new GvmClusterPairs<K>(capacity * (capacity-1) / 2);
	}
	
	// accessors

	/**
	 * The keyer used to assign keys to clusters.
	 */
	
	public GvmKeyer<K> getKeyer() {
		return keyer;
	}
	
	/**
	 * The keyer to be used.
	 * 
	 * @param keyer a keyer, not null
	 */
	
	public void setKeyer(GvmKeyer<K> keyer) {
		if (keyer == null) throw new IllegalArgumentException();
		this.keyer = keyer;
	}

	/**
	 * The greatest number of clusters that may be recorded.
	 * 
	 * @return the capacity
	 */
	
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * The number of dimensions over which clustering is being performed.
	 * 
	 * @return the number of dimensions
	 */
	
	public int getDimension() {
		return dimension;
	}
	
	// public methods
	
	/**
	 * Removes all clusters and clustered points but retains the keyer.
	 */
	
	public void clear() {
		Arrays.fill(clusters, 0, bound, null);
		pairs.clear();
		additions = 0;
		count = 0;
		bound = 0;
	}

	/**
	 * Adds a point to be clustered.
	 * 
	 * @param m
	 *            the mass at the point
	 * @param xs
	 *            the coordinates of the point
	 * @param key
	 *            a key assigned to the point, may be null
	 */

	public void add(NUMBER m, NUMBER[] xs, K key) {
		if (NUMBER.equal(m, NUMBER.zero())) return; //nothing to do
		if (count < capacity) { //shortcut
			//TODO should prefer add if var comes to zero
			GvmCluster<K> cluster = new GvmCluster<K>(this);
			clusters[additions] = cluster;
			cluster.set(m, xs);
			addPairs();
			cluster.key = keyer.addKey(cluster, key);
			count++;
			bound = count;
		} else {
			//identify cheapest merge
			GvmClusterPair<K> mergePair = pairs.peek();
			double mergeT = mergePair == null ? Double.MAX_VALUE : mergePair.value;
			//find cheapest addition
			GvmCluster<K> additionC = null;
			double additionT = Double.MAX_VALUE;
			for (int i = 0; i < clusters.length; i++) {
				GvmCluster<K> cluster = clusters[i];
				double t = cluster.test(m, xs);
				if (t < additionT) {
					additionC = cluster;
					additionT = t;
				}
			}
			if (additionT <= mergeT) {
				//chose addition
				additionC.add(m, xs);
				updatePairs(additionC);
				additionC.key = keyer.addKey(additionC, key);
			} else {
				//choose merge
				GvmCluster<K> c1 = mergePair.c1;
				GvmCluster<K> c2 = mergePair.c2;
				if (NUMBER.lessThan(c1.m0, c2.m0)) {
					c1 = c2;
					c2 = mergePair.c1;
				}
				c1.key = keyer.mergeKeys(c1, c2);
				c1.add(c2);
				updatePairs(c1);
				c2.set(m, xs);
				updatePairs(c2);
				//TODO should this pass through a method on keyer?
				c2.key = null;
				c2.key = keyer.addKey(c2, key);
			}
		}
		additions++;
	}

	/**
	 * Collapses the number of clusters subject to constraints on the maximum
	 * permitted variance, and the least number of clusters. This method may be
	 * called at any time, including between calls to add(). 
	 * 
	 * @param maxVar
	 *            an upper bound on the global variance that may not be exceeded
	 *            by merging clusters
	 * @param minClusters
	 *            a lower bound on the the number of clusters that may not be
	 *            exceeded by merging clusters
	 */
	
	public void reduce(double maxVar, int minClusters) {
		if (minClusters < 0) throw new IllegalArgumentException("negative minClusters");
		if (count <= minClusters) return; //nothing to do
		
		double totalVar = 0.0;
		double totalMass = 0.0;
		for (int i = 0; i < count; i++) {
			GvmCluster<K> cluster = clusters[i];
			totalVar += cluster.var;
			totalMass += NUMBER.doubleValue(cluster.m0);
		}
		
		while (count > minClusters) {
			if (count == 1) {
				//remove the last cluster
				for (int i = 0; i < bound; i++) {
					GvmCluster<K> c = clusters[i];
					if (!c.removed) {
						c.removed = true;
						break;
					}
				}
			} else {
				GvmClusterPair<K> mergePair = pairs.peek();
				GvmCluster<K> c1 = mergePair.c1;
				GvmCluster<K> c2 = mergePair.c2;
				if (NUMBER.lessThan(c1.m0, c2.m0)) {
					c1 = c2;
					c2 = mergePair.c1;
				}
				if (maxVar >= 0.0) {
					double diff = c1.test(c2) - c1.var - c2.var;
					totalVar += diff;
					if (totalVar/totalMass > maxVar) break; //stop here, we are going to exceed maximum
				}
				c1.key = keyer.mergeKeys(c1, c2);
				c1.add(c2);
				updatePairs(c1);
				removePairs(c2);
				c2.removed = true;
			}
			count--;
		}
		//iterate over clusters and remove dead clusters
		{
			int j = 0;
			for (int i = 0; i < bound;) {
				boolean lose = clusters[i].removed;
				if (lose) {
					i++;
				} else {
					if (i != j) clusters[j] = clusters[i];
					i++;
					j++;
				}
			}
			for (; j < bound; j++) {
				clusters[j] = null;
			}
		}
		//iterate over cluster pairs and remove dead pairs
		for (int i = 0; i < count; i++) {
			GvmCluster<K> cluster = clusters[i];
			GvmClusterPair<K>[] pairs = cluster.pairs;
			int k = 0;
			for (int j = 0; j < bound-1;) {
				GvmClusterPair<K> pair = pairs[j];
				boolean lose = pair.c1.removed || pair.c2.removed;
				if (lose) {
					j++;
				} else {
					if (j != k) pairs[k] = pairs[j];
					k++;
					j++;
				}
			}
			for (; k < bound; k++) {
				pairs[k] = null;
			}
		}
		bound = count;
	}
	
	/**
	 * Obtains the clusters for the points added. This method may be called
	 * at any time, including between calls to add().
	 * 
	 * @return the result of clustering the points thus far added
	 */
	
	public List<GvmResult<K>> results() {
		ArrayList<GvmResult<K>> list = new ArrayList<GvmResult<K>>(count);
		for (int i = 0; i < count; i++) {
			GvmCluster<K> cluster = clusters[i];
			//TODO exclude massless clusters?
			list.add(new GvmResult<K>(cluster));
		}
		return list;
	}
	
	// private utility methods
	
	//assumes that count not yet incremented
	//assumes last cluster is the one to add pairs for
	//assumes pairs are contiguous
	private void addPairs() {
		GvmCluster<K> cj = clusters[count];
		int c = count - 1; //index at which new pairs registered for existing clusters
		for (int i = 0; i < count; i++) {
			GvmCluster<K> ci = clusters[i];
			GvmClusterPair<K> pair = new GvmClusterPair<K>(ci, cj);
			ci.pairs[c] = pair;
			cj.pairs[i] = pair;
			pairs.add(pair);
		}
		
	}
	
	//does not assume pairs are contiguous
	private void updatePairs(GvmCluster<K> cluster) {
		GvmClusterPair<K>[] pairs = cluster.pairs;
		//accelerated path
		if (count == bound) {
			int limit = count - 1;
			for (int i = 0; i < limit; i++) {
				this.pairs.reprioritize(pairs[i]);
			}
		} else {
			int limit = bound - 1;
			for (int i = 0; i < limit; i++) {
				GvmClusterPair<K> pair = pairs[i];
				if (pair.c1.removed || pair.c2.removed) continue;
				this.pairs.reprioritize(pair);
			}
		}
	}

	//does not assume pairs are contiguous
	//leaves pairs in cluster pair lists
	//these are tidied when everything is made contiguous again
	private void removePairs(GvmCluster<K> cluster) {
		GvmClusterPair<K>[] pairs = cluster.pairs;
		for (int i = 0; i < bound-1; i++) {
			GvmClusterPair<K> pair = pairs[i];
			if (pair.c1.removed || pair.c2.removed) continue;
			this.pairs.remove(pair);
		}
		
	}
	
}
