package com.tomgibara.geo;

import junit.framework.TestCase;

public class OSIGridTest extends TestCase {

	public void testBasic() {
		GridRef ref = GridRefSystem.OSI65.createGridRef("J 02598 74444");
		System.out.println(ref);
		System.out.println(ref.toLatLon());
		
	}
	
}
