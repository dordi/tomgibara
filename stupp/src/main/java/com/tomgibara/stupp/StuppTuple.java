/*
 * Copyright 2009 Tom Gibara
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
package com.tomgibara.stupp;

import java.util.Arrays;

//TODO should tuple maintain reference to properties that created it?
//TODO change to an interface with optimized implementations
public class StuppTuple {
	
	final Object[] values;
	
	public StuppTuple(Object... values) {
		this.values = values;
	}
	
	public boolean containsNull() {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StuppTuple)) return false;
		StuppTuple that = (StuppTuple) obj;
		return Arrays.equals(this.values, that.values);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}