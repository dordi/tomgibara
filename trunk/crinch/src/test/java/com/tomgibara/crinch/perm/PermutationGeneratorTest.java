package com.tomgibara.crinch.perm;

import java.util.Random;

public class PermutationGeneratorTest extends PermutationTestCase {

	public void testInvert() {
		{
			Permutation p = new Permutation(4,3,2,1,0);
			Permutation i = p.generator().invert().permutation();
			assertEquals(p, i);
		}
		
		{
			Permutation p = new Permutation(1,2,3,4,0);
			Permutation i = p.generator().invert().permutation();
			assertTrue(p.generator().apply(i).permutation().getInfo().isIdentity());
		}
		
		Random random = new Random(0L);
		for (int n = 0; n < 10000; n++) {
			int size = random.nextInt(20);
			Permutation p = Permutation.identity(size).generator().shuffle(random).permutation();
			Permutation q = p.generator().invert().permutation();
			assertTrue(p.generator().apply(q).permutation().getInfo().isIdentity());
			assertTrue(q.generator().apply(p).permutation().getInfo().isIdentity());
		}
	}
	
	public void testApply() {
		Permutation p = Permutation.identity(5);
		Permutation p1 = Permutation.identity(5).generator().transpose(0, 1).permutation();
		Permutation p2 = Permutation.identity(5).generator().transpose(1, 2).permutation();
		assertEquals(new Permutation(1,2,0,3,4), p.generator().apply(p1).apply(p2).permutation());
	}

	public void testIdentity() {
		assertTrue(Permutation.reverse(10).generator().identity().permutation().getInfo().isIdentity());
	}

	public void testRotate() {
		assertTrue(Permutation.rotate(10, 2).generator().rotate(-2).permutation().getInfo().isIdentity());
	}
	
	public void testPower() {
		for (int i = 0; i < 100; i++) {
			Permutation p = Permutation.rotate(10, 1);
			assertEquals(Permutation.rotate(10, i), p.generator().power(i).permutation());
		}
	}
	

}
