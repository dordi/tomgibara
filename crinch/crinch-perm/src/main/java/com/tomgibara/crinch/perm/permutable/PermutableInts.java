/*
 * Copyright 2011 Tom Gibara
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
package com.tomgibara.crinch.perm.permutable;

import java.util.Arrays;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableInts implements Permutable {

	private final int[] values;
	
	public PermutableInts(int... values) {
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}
	
	public int[] getValues() {
		return values;
	}
	
	@Override
	public int getPermutableSize() {
		return values.length;
	}
	
	@Override
	public PermutableInts transpose(int i, int j) {
		int v = values[i];
		values[i] = values[j];
		values[j] = v;
		return this;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableInts)) return false;
		PermutableInts that = (PermutableInts) obj;
		return Arrays.equals(this.values, that.values);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}
