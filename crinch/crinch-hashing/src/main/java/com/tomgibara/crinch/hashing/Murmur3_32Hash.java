package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.AbstractWriteStream;

// See http://smhasher.googlecode.com/svn/trunk/MurmurHash3.cpp
// based on http://guava-libraries.googlecode.com/git/guava/src/com/google/common/hash/Murmur3_32HashFunction.java

public class Murmur3_32Hash<T> extends AbstractHash<T> {

	static final int c1 = 0xcc9e2d51;
	static final int c2 = 0x1b873593;

	private final HashSource<T> source;
	private final int seed;
	
	public Murmur3_32Hash(HashSource<T> source) {
		this(source, 0);
	}
	
	public Murmur3_32Hash(HashSource<T> source, int seed) {
		if (source == null) throw new IllegalArgumentException("null source");
		this.source = source;
		this.seed = seed;
	}
	
	@Override
	public HashRange getRange() {
		return HashRange.FULL_INT_RANGE;
	}
	
	@Override
	public int hashAsInt(T value) {
		MurmurStream stream = new MurmurStream();
		stream.reset(seed);
		source.sourceData(value, stream);
		return stream.hash();
	}
	
	@Override
	public long hashAsLong(T value) {
		return hashAsInt(value) & 0xffffffffL;
	}
	
	// inner classes 
	
	private static class MurmurStream extends AbstractWriteStream {
		
		private int k1;
		private int h1;
		private int len;
		
		void reset(int seed) {
			h1 = seed;
			len = 0;
		}

		@Override
		public void writeByte(byte v) {
			k1 = (v << 24) | (k1 >>> 8);
			
			// process body
			
			if (((++len) & 3) == 0) {
				k1 *= c1; 
				k1 = Integer.rotateLeft(k1, 15); 
				k1 *= c2;
				
				h1 ^= k1;
				h1 = Integer.rotateLeft(h1, 13);
				h1 = h1 * 5 + 0xe6546b64;
			}
		}
		
		public int hash() {
			
			// process tail
			
			int rem = len & 3;
			if (rem != 0) {
				k1 >>>= (4 - rem) << 3;
				
				k1 *= c1;
				k1 = Integer.rotateLeft(k1, 15);
				k1 *= c2;
				h1 ^= k1;
			}
			
			// finalize
			
			h1 ^= len;
			
			h1 ^= h1 >>> 16;
			h1 *= 0x85ebca6b;
			h1 ^= h1 >>> 13;
			h1 *= 0xc2b2ae35;
			h1 ^= h1 >>> 16;
			
			// return
			
			return h1;
		}
		
	}

}
