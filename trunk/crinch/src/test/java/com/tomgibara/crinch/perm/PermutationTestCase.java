package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tomgibara.crinch.perm.permutable.PermutableList;

import junit.framework.TestCase;

abstract class PermutationTestCase extends TestCase {

	static List<Integer> list(Integer... ints) {
		return Arrays.asList(ints);
	}
	
	static List<Integer> copy(List<Integer> list) {
		return new ArrayList<Integer>(list);
	}
	
	static PermutableList<Integer> permutable(List<Integer> list) {
		return new PermutableList<Integer>(list);
	}
	
	static Set<Permutation> set(Permutation... perms) {
		HashSet<Permutation> set = new HashSet<Permutation>();
		set.addAll(Arrays.asList(perms));
		return set;
	}
	
}
