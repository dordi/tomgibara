package com.tomgibara.crinch.record.util;

import junit.framework.TestCase;

public class UniquenessCheckerTest extends TestCase {

	public void testSize() {
		
		System.out.println("1000000 x 4");
		new UniquenessChecker<Integer>(1000000, 4);
		System.out.println("10000000 x 4");
		new UniquenessChecker<Integer>(10000000, 4);
		System.out.println("1000000 x 40");
		new UniquenessChecker<Integer>(1000000, 40);
		System.out.println("1000000 x 40");
		new UniquenessChecker<Integer>(1000000, 400);
		
	}
	
}
