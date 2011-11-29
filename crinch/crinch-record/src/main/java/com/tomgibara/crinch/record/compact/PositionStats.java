package com.tomgibara.crinch.record.compact;

import java.io.File;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.def.RecordDefinition;

class PositionStats {

	final RecordDefinition definition;
	final ExtendedCoding coding;
	final File file;
	
	long bottomPosition;
	long topPosition;
	int fixedBitSize;
	long bitsWritten;

	public PositionStats(ProcessContext context) {
		RecordDefinition def = context.getRecordDef();
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
