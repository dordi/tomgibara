package com.tomgibara.crinch.hashing;

import java.util.Arrays;

import junit.framework.TestCase;

import com.tomgibara.crinch.util.WriteStream;

public class PRNGMultiHashTest extends TestCase {

	public void testBasic() throws Exception {
		
		HashSource<Integer> s = new HashSource<Integer>() {
			
			@Override
			public void sourceData(Integer value, WriteStream out) {
				out.writeInt(value);
			}
		};
		
		PRNGMultiHash<Integer> h = new PRNGMultiHash<Integer>("SHA1PRNG", s, 50);
		int[] tmp = new int[10];
		for (int i = 0; i < 100; i++) {
			HashList hashes = h.hashAsList(i, tmp.length);
			for (int j = 0; j < tmp.length; j++) {
				tmp[j] = hashes.getAsInt(j);
			}
			System.out.println(Arrays.toString(tmp));
		}
		
	}
	
}
