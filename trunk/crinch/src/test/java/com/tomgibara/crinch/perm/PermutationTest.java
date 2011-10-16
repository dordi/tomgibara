package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.perm.permutable.PermutableList;

import junit.framework.TestCase;

public class PermutationTest extends TestCase {

	private static List<Integer> list(Integer... ints) {
		return Arrays.asList(ints);
	}
	
	private static List<Integer> copy(List<Integer> list) {
		return new ArrayList<Integer>(list);
	}
	
	private static PermutableList<Integer> permutable(List<Integer> list) {
		return new PermutableList<Integer>(list);
	}
	
	public void testSwapGeneration() {
		List<Integer> a = list(1,2,3,4,5);
		Permutation.identity(5).generator().swap(0, 1).permutation().permute(permutable(a));
		assertEquals(list(2,1,3,4,5), a);
	}
	
	public void testCorrespondenceConstructor() {
		verifyBadConstructor(0,1,2,3,3);
		verifyBadConstructor(1);
		verifyBadConstructor(-1, 0, -1);
		verifyBadConstructor(1, -1, 0);
		verifyBadConstructor(null);

		new Permutation().permute(permutable(list()));

		assertEquals(list(5,4,3,2,1), new Permutation(4,3,2,1,0).permute(permutable(list(1,2,3,4,5))).getList());
		
	}
	
	private void verifyBadConstructor(int... correspondence) {
		try {
			new Permutation(correspondence);
			fail("allowed invalid construction array " + Arrays.toString(correspondence));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testNumberOfCycles() {
		
	}
	
}
