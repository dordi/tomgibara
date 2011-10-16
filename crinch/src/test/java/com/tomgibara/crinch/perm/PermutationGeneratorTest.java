package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.perm.permutable.PermutableList;

import junit.framework.TestCase;

public class PermutationGeneratorTest extends TestCase {

	private static List<Integer> list(Integer... ints) {
		return Arrays.asList(ints);
	}
	
	private static List<Integer> copy(List<Integer> list) {
		return new ArrayList<Integer>(list);
	}
	
	private static PermutableList<Integer> permutable(List<Integer> list) {
		return new PermutableList<Integer>(list);
	}

	public void testInvert() {
		{
			Permutation p = new Permutation(4,3,2,1,0);
			Permutation i = p.generator().invert().permutation();
			assertEquals(p, i);
		}
		
		{
			Permutation p = new Permutation(1,2,3,4,0);
			Permutation i = p.generator().invert().permutation();
			assertTrue(p.compose(i).getInfo().isIdentity());
		}
	}
	
}
