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

	final long recordOrdinal;
	final long recordPosition;
	
	public AbstractRecord() {
		recordOrdinal = -1L;
		recordPosition = -1L;
	}

	public AbstractRecord(long recordOrdinal, long recordPosition) {
		this.recordOrdinal = recordOrdinal;
		this.recordPosition = recordPosition;
	}
	
	public AbstractRecord(AbstractRecord that) {
		this.recordOrdinal = that.recordOrdinal;
		this.recordPosition = that.recordPosition;
	}
	
	public AbstractRecord(Record that) {
		this.recordOrdinal = that.getRecordOrdinal();
		this.recordPosition = that.getRecordPosition();
	}

	@Override
	public long getRecordOrdinal() {
		return recordOrdinal;
	}
	
	@Override
	public long getRecordPosition() {
		return recordPosition;
	}
	
}
