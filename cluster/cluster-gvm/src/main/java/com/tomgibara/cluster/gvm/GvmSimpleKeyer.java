package com.tomgibara.cluster.gvm;

/**
 * A convenience class that reduces the task of choosing a key for a cluster to
 * that of choosing-between/combining two non-null keys.
 * 
 * @author Tom Gibara
 * 
 * @param <K>
 *            the key type
 */

public abstract class GvmSimpleKeyer<K> implements GvmKeyer<K> {

	@Override
	public K mergeKeys(GvmCluster<K> c1, GvmCluster<K> c2) {
		K k1 = c1.getKey();
		K k2 = c2.getKey();
		if (k1 == null) return k2;
		if (k2 == null) return k1;
		return combineKeys(k1, k2);
	}
	
	@Override
	public K addKey(GvmCluster<K> cluster, K k2) {
		K k1 = cluster.getKey();
		if (k1 == null) return k2;
		if (k2 == null) return k1;
		return combineKeys(k1, k2);
	}
	
	/**
	 * Combines two keys. Combining two keys may totally discard information
	 * from one, both or none of the supplied keys.
	 * 
	 * @param k1
	 *            a key, not null
	 * @param k2
	 *            a key, not null
	 * 
	 * @return a combined key
	 */
	protected abstract K combineKeys(K k1, K k2);
	
}
