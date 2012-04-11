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
package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.RecordStats;

// not safe for concurrent use

public class RecordDecompactor extends CompactCharStore {

	private final ColumnCompactor[] compactors;
	//TODO could chain spare records - analyze impact
	private CompactRecord spare = null;

	private RecordDecompactor(RecordDecompactor that) {
		this.compactors = that.compactors;
	}

	public RecordDecompactor(RecordStats stats, int startIndex) {
		List<ColumnStats> list = stats.getColumnStats();
		int length = list.size();
		if (startIndex < 0) throw new IllegalArgumentException("negative startIndex");
		if (startIndex > length) throw new IllegalArgumentException("invalid startIndex");
		ColumnCompactor[] compactors = new ColumnCompactor[length - startIndex];
		for (int i = startIndex, j = 0; i < length; i++, j++) {
			compactors[j] = new ColumnCompactor(list.get(i), this, i);
		}
		this.compactors = compactors;
		setCharColumns(compactors.length + startIndex);
	}

	public CompactRecord decompact(CodedReader reader, long ordinal) {
		return record().populate(reader, ordinal);
	}

	public CompactRecord decompact(CodedReader reader, long ordinal, long position) {
		return record().populate(reader, ordinal, position);
	}

	public RecordDecompactor copy() {
		return new RecordDecompactor(this);
	}
	
	ColumnCompactor[] getCompactors() {
		return compactors.clone();
	}
	
	void spare(CompactRecord spare) {
		if (this.spare == null) this.spare = spare;
	}
	
	private CompactRecord record() {
		if (spare == null) return new CompactRecord(this);
		try {
			return spare;
		} finally {
			spare = null;
		}
	}
	
}
