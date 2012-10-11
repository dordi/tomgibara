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


public class GridRefTest extends GeoTest {

	public void testBasic() {
		LatLon ll = GridRefSystem.OSGB36.createGridRef("NN166712").toLatLon();
		assertAlmostEquals(56.796556, ll.getLatitude());
		assertAlmostEquals(-5.00393, ll.getLongitude());
	}
	
	public void testRoundTrip() {
		testRoundTrip(GridRefSystem.OSGB36.createGridRef("NN166712"));
		testRoundTrip(GridRefSystem.OSI65.createGridRef("H 79972 62472"));
	}
	
	private void testRoundTrip(GridRef ref) {
		GridRef ref2 = ref.toLatLon().toGridRef(ref.getSystem().getGrid());
		assertClose(ref, ref2);
	}
	
}
