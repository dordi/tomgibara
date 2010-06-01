package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.WriteStream;

/**
 * A generic {@link HashSource} implementation that simply uses the {@link #hashCode()} value to generate byte data.
 * 
 * @author tomgibara
 *
 */

public class ObjectHashSource implements HashSource<Object> {

	@Override
	public void sourceData(Object value, WriteStream out) {
		out.writeInt(value.hashCode());
	}
	
}
