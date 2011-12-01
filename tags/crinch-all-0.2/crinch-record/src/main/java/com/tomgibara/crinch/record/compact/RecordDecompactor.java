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

public class RecordDecompactor {

	private final ColumnCompactor[] compactors;

	public RecordDecompactor(RecordStats stats, int startIndex) {
		List<ColumnStats> list = stats.getColumnStats();
		int length = list.size();
		if (startIndex < 0) throw new IllegalArgumentException("negative startIndex");
		if (startIndex > length) throw new IllegalArgumentException("invalid startIndex");
		ColumnCompactor[] compactors = new ColumnCompactor[length - startIndex];
		for (int i = startIndex, j = 0; i < length; i++, j++) {
			compactors[j] = new ColumnCompactor(list.get(i));
		}
		this.compactors = compactors;
	}

	public CompactRecord decompact(CodedReader reader, long recordOrdinal) {
		return new CompactRecord(compactors, reader, recordOrdinal, -1L);
	}

	public CompactRecord decompact(CodedReader reader, long recordOrdinal, long recordPosition) {
		return new CompactRecord(compactors, reader, recordOrdinal, recordPosition);
	}

	
}
