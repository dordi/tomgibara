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

import java.io.File;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.process.ProcessContext;

class PositionStats {

	final RecordDef definition;
	final ExtendedCoding coding;
	final File file;
	
	long bottomPosition;
	long topPosition;
	int fixedBitSize;
	long bitsWritten;

	public PositionStats(ProcessContext context) {
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		definition = def.asBasis();
		coding = context.getCoding();
		file = context.file("positions", true, definition);
		if (context.isClean()) file.delete();
	}
	
	void write() {
		CodedStreams.writeToFile(new CodedStreams.WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				writer.writePositiveLong(bottomPosition + 1L);
				writer.writePositiveLong(topPosition + 1L);
				writer.writePositiveInt(fixedBitSize + 1);
				writer.writePositiveLong(bitsWritten + 1L);
			}
		}, coding, file);
	}
	
	void read() {
		CodedStreams.readFromFile(new CodedStreams.ReadTask() {
			@Override
			public void readFrom(CodedReader reader) {
				bottomPosition = reader.readPositiveLong() - 1L;
				topPosition = reader.readPositiveLong() - 1L;
				fixedBitSize = reader.readPositiveInt() - 1;
				bitsWritten = reader.readPositiveLong() - 1L;
			}
		}, coding, file);
	}
	
}
