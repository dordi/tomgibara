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


public class TransformTest extends GeoTest {

	private final DatumTransforms transforms = DatumTransforms.getDefaultTransforms();
	
	public void testBasic() {
		testCorrection( GridRefSystem.OSGB36.createGridRef("E651410 N313177") );
		testCorrection( GridRefSystem.OSGB36.createGridRef("NN166712") );
		testCorrection( GridRefSystem.OSI65.createGridRef("J 02598 74444") );
		testCorrection( GridRefSystem.OSI65.createGridRef("H 79972 62472") );
	}

	public void testIntermediate() {
		LatLonHeight llh1 = GridRefSystem.OSGB36.createGridRef("E651410 N313177").toLatLon().atHeight(0);
		LatLonHeight llh2 = transforms.getTransform(Datum.OSI65).transform(llh1);
		System.out.println(llh1 + " -> " + llh2);
	}
	
	public void testCorrection(GridRef ref) {
		System.out.println(ref.getEasting() +"," + ref.getNorthing());
		LatLon ll = ref.toLatLon();
		System.out.println("Untransformed: " + ll);
		LatLon ll2 = transforms.getTransform(Datum.WSG84).transform(ll.atHeight(0)).getLatLon();
		System.out.println("Transformed: " + ll2);
	}
	
}
