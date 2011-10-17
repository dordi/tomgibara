package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

	public void testNextAndPreviousByOrder() {
		int count = 1;
		for (int size = 0; size < 5; size++) {
			if (size > 0) count *= size;
			PermutationGenerator pg = Permutation.identity(size).generator();
			Permutation p;
			
			Set<Permutation> set1 = new HashSet<Permutation>();
			List<Permutation> list1 = new ArrayList<Permutation>();
			p = pg.permutation();
			set1.add(p);
			list1.add(p);
			while (pg.hasNext()) {
				p = pg.nextByNumber().permutation();
				set1.add(p);
				list1.add(p);
			} 
			assertEquals(count, set1.size());
			assertEquals(count, list1.size());
			
			Set<Permutation> set2 = new HashSet<Permutation>();
			List<Permutation> list2 = new ArrayList<Permutation>();
			p = pg.permutation();
			set2.add(p);
			list2.add(p);
			while (pg.hasPrevious()) {
				p = pg.previousByNumber().permutation();
				set2.add(p);
				list2.add(p);
			} 
			assertEquals(count, set2.size());
			assertEquals(count, list2.size());
			
			assertEquals(set1, set2);
			Collections.reverse(list2);
			assertEquals(list1, list2);
		}
	}
	
}
