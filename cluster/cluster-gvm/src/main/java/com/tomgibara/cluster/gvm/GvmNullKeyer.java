package com.tomgibara.cluster.gvm;

/**
 * Simply discards all keys.
 * 
 * @author Tom Gibara
 *
 * @param <K> the key type
 */

public class GvmNullKeyer<K> implements GvmKeyer<K> {

	@Override
	public K addKey(GvmCluster<K> cluster, K key) {
		return null;
	}
	
	@Override
	public K mergeKeys(GvmCluster<K> c1, GvmCluster<K> c2) {
		return null;
	}
	
}
