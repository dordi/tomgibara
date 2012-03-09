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

import java.util.ArrayList;
import java.util.List;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.def.RecordDef;

public class RecordStats {

	public static int write(CodedWriter writer, RecordStats stats) {
		int c = writer.writePositiveLong(stats.recordCount + 1L);
		c += writer.writePositiveInt(stats.columnStats.size() + 1);
		for (ColumnStats cs : stats.columnStats) {
			c += ColumnStats.write(writer, cs);
		}
		return c;
	}

	public static RecordStats read(CodedReader reader) {
		RecordStats stats = new RecordStats();
		stats.recordCount = reader.readPositiveLong() - 1L;
		int size = reader.readPositiveInt() - 1;
		for (; size > 0; size--) {
			stats.columnStats.add(ColumnStats.read(reader));
		}
		return stats;
	}
	
	private final List<ColumnStats> columnStats = new ArrayList<ColumnStats>();
	private long recordCount;
	
	public RecordStats() {
	}
	
	public RecordStats(long recordCount) {
		setRecordCount(recordCount);
	}
	
	private RecordStats(RecordStats that) {
		this.columnStats.addAll(that.columnStats);
		this.recordCount = that.recordCount;
	}
	
	public List<ColumnStats> getColumnStats() {
		return columnStats;
	}
	
	public void setRecordCount(long recordCount) {
		if (recordCount < 0) throw new IllegalArgumentException("negative record count");
		this.recordCount = recordCount;
	}
	
	public long getRecordCount() {
		return recordCount;
	}
	
	public RecordStats copy() {
		return new RecordStats(this);
	}
	
	public RecordStats adaptFor(RecordDef recordDef) {
		if (recordDef == null) throw new IllegalArgumentException("null recordDef");
		if (recordDef.getBasis() == null) return this;
		RecordStats copy = copy();
		recordDef.adaptBasicList(copy.getColumnStats());
		return copy;
	}
	
}
