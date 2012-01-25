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

public class StringRecord extends AbstractRecord {

	private final String[] values;
	
	public StringRecord(long ordinal, long position, String... values) {
		super(ordinal, position);
		if (values == null) throw new IllegalArgumentException("null value");
		this.values = values;
	}
	
	@Override
	public void release() {
	}
	
	public String get(int index) {
		checkIndex(index);
		String value = values[index];
		return value == null || value.isEmpty() ? null : value;
	}
	
	public String[] getAll() {
		return values.clone();
	}
	
	public int length() {
		return values.length;
	}

	public StringRecord mappedRecord(int... mapping) {
		String[] mapped = new String[mapping.length];
		for (int i = 0; i < mapped.length; i++) {
			int index = mapping[i];
			checkIndex(index);
			mapped[i] = values[index];
		}
		return new StringRecord(ordinal, -1L, mapped);
	}
	
	private void checkIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= values.length) throw new IllegalArgumentException("index too large");
	}
	
	//TODO implement object methods

	@Override
	public String toString() {
		return "Ordinal: " + getOrdinal() + ", position: " + getPosition() + ", values: " + Arrays.toString(values);
	}
	
	
}
