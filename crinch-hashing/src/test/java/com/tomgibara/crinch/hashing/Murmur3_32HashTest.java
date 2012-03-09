package com.tomgibara.crinch.hashing;

import java.util.Random;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.tomgibara.crinch.util.WriteStream;

import junit.framework.TestCase;

public class Murmur3_32HashTest extends TestCase {

	public void testMatchesGuava() {
		Random r = new Random(0L);
		Murmur3_32Hash<byte[]> hash1 = new Murmur3_32Hash<byte[]>(new ByteSource());
		HashFunction hash2 = Hashing.murmur3_32();
		
		// test empty
		{
			byte[] b = new byte[0];
			assertEquals(hash2.hashBytes(b).asInt(), hash1.hashAsInt(b));
		}
		
		// test single byte
		{
			byte[] b = new byte[1];
			for (byte i = -128; i < 127; i++) {
				b[0] = i;
				assertEquals(hash2.hashBytes(b).asInt(), hash1.hashAsInt(b));
			}
		}
		
		for (int i = 0; i < 10000; i++) {
			byte[] bytes = new byte[r.nextInt(100)];
			r.nextBytes(bytes);
			int h1 = hash1.hashAsInt(bytes);
			int h2 = hash2.hashBytes(bytes).asInt();
			assertEquals(h2, h1);
		}
	}
	
	private static class ByteSource implements HashSource<byte[]> {
		
		@Override
		public void sourceData(byte[] value, WriteStream out) {
			out.writeBytes(value);
		}
		
	}
	
}
