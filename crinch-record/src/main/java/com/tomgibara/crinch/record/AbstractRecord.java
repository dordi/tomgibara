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

public abstract class AbstractRecord implements Record {

	final long ordinal;
	final long position;
	
	public AbstractRecord() {
		ordinal = -1L;
		position = -1L;
	}

	public AbstractRecord(long ordinal, long position) {
		this.ordinal = ordinal;
		this.position = position;
	}
	
	public AbstractRecord(AbstractRecord that) {
		this.ordinal = that.ordinal;
		this.position = that.position;
	}
	
	public AbstractRecord(Record that) {
		this.ordinal = that.getOrdinal();
		this.position = that.getPosition();
	}

	@Override
	public long getOrdinal() {
		return ordinal;
	}
	
	@Override
	public long getPosition() {
		return position;
	}
	
}
