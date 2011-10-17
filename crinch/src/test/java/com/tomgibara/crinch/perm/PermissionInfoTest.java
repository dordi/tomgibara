package com.tomgibara.crinch.perm;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class PermissionInfoTest extends PermutationTestCase {

	public void testOdd() {
		assertFalse( Permutation.identity(5).getInfo().isOdd() );
		assertTrue( Permutation.identity(5).generator().transpose(0, 1).permutation().getInfo().isOdd() );
		assertFalse( Permutation.identity(5).generator().transpose(0, 1).transpose(1, 2).permutation().getInfo().isOdd() );
	}
	
	public void testIdentity() {
		List<Integer> a = list(1,2,3,4,5);
		List<Integer> b = copy(a);
		Permutation p = Permutation.identity(5);
		assertTrue(p.getInfo().isIdentity());
		p.permute(permutable(b));
		assertEquals(b, a);
	}

	public void testCyclic() {
		assertEquals(1, new Permutation(1,2,3,4,0).getInfo().getNumberOfCycles());
		assertEquals(0, new Permutation(0,1,2,3,4).getInfo().getNumberOfCycles());
		assertEquals(1, new Permutation(1,0,2,3,4).getInfo().getNumberOfCycles());
		assertEquals(2, new Permutation(1,0,2,4,3).getInfo().getNumberOfCycles());
		assertEquals(0, new Permutation(0).getInfo().getNumberOfCycles());
		assertEquals(0, new Permutation().getInfo().getNumberOfCycles());
	}
	
	public void testDisjointCycles() {
		{
			Permutation p = Permutation.identity(5);
			assertTrue(p.getInfo().getDisjointCycles().isEmpty());
		}
		{
			Permutation p = new Permutation(1,2,3,4,0);
			assertEquals(set(p), p.getInfo().getDisjointCycles());
		}
		{
			Permutation p = new Permutation(1,0,2,4,3);
			assertEquals(set(new Permutation(1,0,2,3,4), new Permutation(0,1,2,4,3)), p.getInfo().getDisjointCycles());
		}
		{
			Permutation p = new Permutation(1,2,0,4,3);
			assertEquals(set(new Permutation(1,2,0,3,4), new Permutation(0,1,2,4,3)), p.getInfo().getDisjointCycles());
		}

		Random random = new Random(0);
		for (int i = 0; i < 1000; i++) {
			int size = 1 + random.nextInt(20);
			final Permutation permutation = Permutation.identity(size).generator().shuffle(random).permutation();
			Permutation.Info info = permutation.getInfo();
			Set<Permutation> cycles = info.getDisjointCycles();
			assertEquals(info.getNumberOfCycles(), cycles.size());
			PermutationGenerator generator = Permutation.identity(size).generator();
			for (Permutation p : cycles) {
				p.permute(generator);
			}
			assertEquals(permutation, generator.permutation());
		}
	}
	
}
