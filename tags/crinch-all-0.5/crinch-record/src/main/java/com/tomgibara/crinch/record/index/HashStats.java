package com.tomgibara.crinch.record.index;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.process.ProcessContext;

class HashStats extends IndexStats {

	boolean positional;
	boolean ordinal;
	int tableSize;
	int[] hashSeeds;
	int positionBits;
	int ordinalBits;
	
	HashStats(ProcessContext context) {
		super("hash", context);
	}

	@Override
	public void writeTo(CodedWriter writer) {
		writer.getWriter().writeBoolean(positional);
		writer.getWriter().writeBoolean(ordinal);
		writer.writePositiveInt(tableSize + 1);
		CodedStreams.writePrimitiveArray(writer, hashSeeds);
		writer.writePositiveInt(positionBits);
		writer.writePositiveInt(ordinalBits);
	}

	@Override
	public void readFrom(CodedReader reader) {
		positional = reader.getReader().readBoolean();
		ordinal = reader.getReader().readBoolean();
		tableSize = reader.readPositiveInt() - 1;
		hashSeeds = CodedStreams.readIntArray(reader);
		positionBits = reader.readPositiveInt();
		ordinalBits = reader.readPositiveInt();
	}
	

	
}
