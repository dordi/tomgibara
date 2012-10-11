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


public class OSGridTest extends GeoTest {

	private static GridRefSystem system = GridRefSystem.OSGB36;
	private static OSGrid grid = (OSGrid) system.getGrid();

	private GridRef ref(int e, int n) {
		return system.createGridRef(e, n);
	}
	
	private GridRef ref(String str) {
		return grid.refFromString(system, str);
	}
	
	public void testBasic() {
		
		assertEquals(ref(216650, 771250), ref("NN166712"));
		assertEquals(ref(216650, 771250), ref("NN 166712"));
		assertEquals(ref(216650, 771250), ref("NN166 712"));
		assertEquals(ref(216650, 771250), ref("NN 166 712"));
		assertEquals(ref(216650, 771250), ref("NN   166   712"));
		
		ref("NN");
		ref("NN12");
		ref("NN1234");
		ref("NN123456");
		ref("NN12345678");
		ref("NN1234567890");
		
		assertBad("NN 1660 712");
		assertBad("NN 1234567 1234567");
		assertBad("AI 123 456");
		assertBad("IA 123 456");
		
		checkFormat("NN");
		checkFormat("NN17");
		checkFormat("NN1671");
		checkFormat("NN 166 712");
	}
	
	private void checkFormat(String str) {
		assertEquals(str, grid.refToString(ref(str)));
	}

	private void assertBad(String str) {
		try {
			fail("Gave: " + ref(str));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
}
