package com.tomgibara.cluster.gvm;

/**
 * A pair of clusters, each element of which is distinct.
 * 
 * @author Tom Gibara
 *
 * @param <K> the key type
 */

class GvmClusterPair<K> {

	/**
	 * The first cluster in this collection.
	 */
	
	final GvmCluster<K> c1;
	
	/**
	 * The second cluster in this collection.
	 */
	
	final GvmCluster<K> c2;
	
	/**
	 * The index of this pair within a heap of pairs.
	 */
	
	int index;
	
	/**
	 * The amount the global variance would increase if this pair was merged.
	 */
	
	double value;
	
	/**
	 * Constructs a new pair and computes its value.
	 * 
	 * @param c1 a cluster, not equal to c2
	 * @param c2 a cluster, not equal to c1
	 */
	
	GvmClusterPair(GvmCluster<K> c1, GvmCluster<K> c2) {
		if (c1 == c2) throw new IllegalArgumentException();
		this.c1 = c1;
		this.c2 = c2;
		update();
	}

	// object methods
	
	// package methods
	
	/**
	 * Updates the value of the pair.
	 */
	
	void update() {
		value = c1.test(c2) - c1.var - c2.var;
	}
	
}
