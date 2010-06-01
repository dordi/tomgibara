package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.WriteStream;

/**
 * Converts an object into byte data that can be used to compute a hash value.
 * 
 * @author tomgibara
 *
 * @param <T> the type of object that will be be the source of hash data
 */

//TODO rename this class and its methods, but to what?
public interface HashSource<T> {

	void sourceData(T value, WriteStream out);
	
}
