package com.tomgibara.crinch.record.compact;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordDefinition;

class PositionStats {

	final RecordDefinition definition;
	final ExtendedCoding coding;
	final File file;
	
	long bottomPosition;
	long topPosition;
	int fixedBitSize;
	long bitsWritten;

	public PositionStats(ProcessContext context) {
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		definition = RecordDefinition.fromTypes(context.getColumnTypes()).build().withOrdering(context.getColumnOrders()).asBasis();
		coding = context.getCoding();
		file = new File(context.getOutputDir(), context.getDataName() + ".positions-stats." + definition.getId());
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
