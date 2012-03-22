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
package com.tomgibara.crinch.record.index;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.process.ProcessContext;

class PositionStats extends IndexStats {

	long bottomPosition;
	long topPosition;
	int fixedBitSize;
	long bitsWritten;

	public PositionStats(ProcessContext context) {
		super("position", context);
	}
	
	@Override
	public void writeTo(CodedWriter writer) {
		writer.writePositiveLong(bottomPosition);
		writer.writePositiveLong(topPosition);
		writer.writePositiveInt(fixedBitSize);
		writer.writePositiveLong(bitsWritten);
	}
	
	@Override
	public void readFrom(CodedReader reader) {
		bottomPosition = reader.readPositiveLong();
		topPosition = reader.readPositiveLong();
		fixedBitSize = reader.readPositiveInt();
		bitsWritten = reader.readPositiveLong();
	}
	
}
