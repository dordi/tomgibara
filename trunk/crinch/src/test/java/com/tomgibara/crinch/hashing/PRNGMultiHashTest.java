package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

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
		
		PRNGMultiHash<Integer> mh = new PRNGMultiHash<Integer>("SHA1PRNG", s, 50);
		final HashRange r = mh.getRange();
		final int m = 10;
		final int[] tmp = new int[m];
		for (int i = 0; i < 100; i++) {
			HashList list = mh.hashAsList(i, m);
			assertTrue(list.size() >= m);
			for (int j = 0; j < tmp.length; j++) {
				final int h = list.getAsInt(j);
				assertTrue(BigInteger.valueOf(h).compareTo(r.getMaximum()) <= 0);
				assertTrue(r.getMinimum().compareTo(BigInteger.valueOf(h)) <= 0);
				tmp[j] = h;
			}
			//TODO need to execute a statistical test for uniform distribution here
		}
		
	}
	
}
