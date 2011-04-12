package com.tomgibara.cluster.gvm;

/**
 * Merges keys by choosing the non-null key of the more massive cluster when
 * available. Where a key is being added to a cluster, any pre-existing key is
 * preserved.
 * 
 * @author Tom Gibara
 * 
 * @param <K>
 *            the key type
 */

public class GvmDefaultKeyer<K> implements GvmKeyer<K> {

	@Override
	public K mergeKeys(GvmCluster<K> c1, GvmCluster<K> c2) {
		K key = c1.getKey();
		return key == null ? c2.getKey() : key;
	}
	
	@Override
	public K addKey(GvmCluster<K> cluster, K key) {
		K k = cluster.getKey();
		return k == null ? key : k;
	}
	
	
	
}
