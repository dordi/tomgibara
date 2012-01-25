/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record;

import java.util.Arrays;

public class ArrayRecord extends AbstractLinearRecord {

	private final Object[] values;
	
	public ArrayRecord(long ordinal, long position, Object[] values) {
		super(ordinal, position);
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}

	@Override
	protected Object getValue(int index, boolean free) {
		if (free) {
			Object tmp = values[index];
			values[index] = null;
			return tmp;
		} else {
			return values[index];
		}
	}

	@Override
	protected int getLength() {
		return values.length;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}
