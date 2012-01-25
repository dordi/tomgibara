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

public class SingletonRecord extends AbstractLinearRecord {

	private Object value;

	public SingletonRecord(Object value) {
		super(-1L, -1L);
		this.value = value;
	}

	public SingletonRecord(long ordinal, long position, Object value) {
		super(ordinal, position);
		this.value = value;
	}

	@Override
	protected Object getValue(int index, boolean free) {
		if (free) {
			Object tmp = value;
			value = null;
			return tmp;
		} else {
			return value;
		}
	}

	@Override
	protected int getLength() {
		return 1;
	}

	@Override
	public String toString() {
		return "ordinal: " + ordinal + ",  position: " + position + ", value: " + value;
	}
	
}
