package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import com.tomgibara.crinch.perm.permutable.PermutableList;

public class PermissionInfoTest extends TestCase {

	private static List<Integer> list(Integer... ints) {
		return Arrays.asList(ints);
	}
	
	private static List<Integer> copy(List<Integer> list) {
		return new ArrayList<Integer>(list);
	}
	
	private static PermutableList<Integer> permutable(List<Integer> list) {
		return new PermutableList<Integer>(list);
	}
	
	private static Set<Permutation> set(Permutation... perms) {
		HashSet<Permutation> set = new HashSet<Permutation>();
		set.addAll(Arrays.asList(perms));
		return set;
	}
	
	public void testOdd() {
		assertFalse( Permutation.identity(5).getInfo().isOdd() );
		assertTrue( Permutation.identity(5).generator().swap(0, 1).permutation().getInfo().isOdd() );
		assertFalse( Permutation.identity(5).generator().swap(0, 1).swap(1, 2).permutation().getInfo().isOdd() );
	}
	
	public void testIdentity() {
		List<Integer> a = list(1,2,3,4,5);
		List<Integer> b = copy(a);
		Permutation p = Permutation.identity(5);
		assertTrue(p.getInfo().isIdentity());
		p.permute(permutable(b));
		assertEquals(b, a);
		p.unpermute(permutable(b));
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
	
	public void testCycles() {
		{
			Permutation p = Permutation.identity(5);
			assertTrue(p.getInfo().getCycles().isEmpty());
		}
		{
			Permutation p = new Permutation(1,2,3,4,0);
			assertEquals(set(p), p.getInfo().getCycles());
		}
		{
			Permutation p = new Permutation(1,0,2,4,3);
			assertEquals(set(new Permutation(1,0,2,3,4), new Permutation(0,1,2,4,3)), p.getInfo().getCycles());
		}
		{
			Permutation p = new Permutation(1,2,0,4,3);
			assertEquals(set(new Permutation(1,2,0,3,4), new Permutation(0,1,2,4,3)), p.getInfo().getCycles());
		}

		Random random = new Random(0);
		for (int i = 0; i < 1000; i++) {
			final Permutation permutation = Permutation.identity(5).generator().shuffle(random).permutation();
			Permutation.Info info = permutation.getInfo();
			Set<Permutation> cycles = info.getCycles();
			assertEquals(info.getNumberOfCycles(), cycles.size());
			PermutationGenerator generator = Permutation.identity(5).generator();
			System.out.println();
			System.out.println(permutation.toString());
			System.out.println(cycles);
			System.out.println("**************");
			for (Permutation p : cycles) {
				System.out.println(p +" applied to " + generator.permutation());
				p.permute(generator);
				System.out.println("gave " + generator.permutation());
			}
			assertEquals(permutation, generator.permutation());
		}
	}
	
}
