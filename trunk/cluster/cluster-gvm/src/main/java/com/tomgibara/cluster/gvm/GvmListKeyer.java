package com.tomgibara.cluster.gvm;

import java.util.List;

/**
 * Allows multiple keys to be associated with clusters in the form of a lists
 * which may be concatenated when clusters merge.
 * 
 * @author Tom Gibara
 * 
 * @param <K>
 *            type of key
 */

//TODO if hierachial clustering is supported, lists will need to be treated as unmodifiable
public class GvmListKeyer<K> extends GvmSimpleKeyer<List<K>> {

	protected List<K> combineKeys(List<K> list1, List<K> list2) {
		list1.addAll(list2);
		return list1;
	}
	
}
