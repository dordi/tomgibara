/*
 * Copyright 2007 Tom Gibara
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
package com.tomgibara.cluster.gvm.demo.city;

import com.tomgibara.cluster.gvm.GvmSimpleKeyer;
import com.tomgibara.cluster.gvm.space.GvmVectorSpace;

public class SingleCityKeyer extends GvmSimpleKeyer<GvmVectorSpace.Vector,City> {

	@Override
	protected City combineKeys(City city1, City city2) {
		return city1.pop < city2.pop ? city2 : city1;
	}
	
}
