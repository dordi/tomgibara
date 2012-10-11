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

/**
 * Parses and formats map references using the Ordinance Survey grid.
 * 
 * @author Tom Gibara
 */

public final class OSGrid implements Grid {

	public static OSGrid instance = new OSGrid();
	
	private final GridHelper helper = new GridHelper(false);
	
	private OSGrid() { }
	
	@Override
	public GridRef refFromString(GridRefSystem system, String str) {
		return helper.refFromString(system, str);
	}
	
	@Override
	public String refToString(GridRef ref) {
		return helper.refToString(ref);
	}

}
