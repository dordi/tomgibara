/*
 * Copyright 2012 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.tomgibara.geo;

import junit.framework.TestCase;

public abstract class GeoTest extends TestCase {

	void assertAlmostEquals(double a, double b) {
		assertTrue(Math.abs(a-b) < 0.01);
	}
	
	void assertClose(GridRef ref1, GridRef ref2) {
		assertSame(ref1.getSystem(), ref2.getSystem());
		assertTrue(Math.abs(ref1.getEasting() - ref2.getEasting()) < 2); 
		assertTrue(Math.abs(ref1.getNorthing() - ref2.getNorthing()) < 2); 
	}

	void assertClose(LatLon ll1, LatLon ll2) {
		double x = ll1.getLatitude() - ll2.getLatitude();
		double y = ll1.getLongitude() - ll2.getLongitude();
		assertTrue(x*x + y*y < 0.00001);
	}
	
	void assertClose(LatLonHeight llh1, LatLonHeight llh2) {
		LatLon ll1 = llh1.getLatLon();
		LatLon ll2 = llh2.getLatLon();
		assertClose(ll1, ll2);
	}
	
}
