package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
		Permutation.identity(5).generator().transpose(0, 1).permutation().permute(permutable(a));
		assertEquals(list(2,1,3,4,5), a);
	}
	
	public void testPermute() {
		verifyPermute(list(), list());
		verifyPermute(list(5,4,3,1,2), list(1,2,3,4,5), 4,3,2,0,1);
		verifyPermute(list(3,4,5,2,1), list(1,2,3,4,5), 2,3,4,1,0);
	}
	
	private void verifyPermute(List<Integer> expected, List<Integer> input, int... corr) {
		assertEquals(expected, new Permutation(corr).permute(permutable(input)).getList());
	}
	
	public void testReverseConstructor() {
		for (int size = 0; size < 100; size++) {
			Permutation r = Permutation.reverse(size);
			Permutation i = Permutation.identity(size);
			if (size > 1) assertFalse(r.equals(i));
			assertEquals(i, r.generator().apply(r).permutation());
			if (size > 0) {
				assertEquals(0, r.getCorrespondence()[size - 1]);
				assertEquals(size - 1, r.getCorrespondence()[0]);
			}
		}
	}
	
	public void testRotateConstructor() {
		for (int size = 0; size < 100; size++) {
			for (int dist = - 2 * size; dist < 2 * size; dist++) {
				Permutation r = Permutation.rotate(size, dist);
				if (size > 1) {
					if ((dist % size) == 0) {
						assertEquals(0, r.getInfo().getDisjointCycles().size());
						assertTrue(r.getInfo().getFixedPoints().isAllOnes());
					} else {
						assertEquals(1, r.getInfo().getDisjointCycles().size());
						assertTrue(r.getInfo().getFixedPoints().isAllZeros());
					}
				} else {
					assertEquals(Permutation.identity(size), r);
				}
			}
		}
	}

	public void testTransposeConstructor() {
		Random r = new Random(0L);
		for (int n = 0; n < 10000; n++) {
			int size = r.nextInt(10) + 2;
			int i = r.nextInt(size);
			int j = r.nextInt(size);
			Permutation p = Permutation.transpose(size, i, j);
			assertEquals(i == j, p.getInfo().isIdentity());
			assertEquals(p, p.generator().invert().permutation());
			assertEquals(i == j ? 0 : 1, p.getInfo().getNumberOfTranspositions());
			assertEquals(i == j ? 0 : 1, p.getInfo().getNumberOfCycles());
		}
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
