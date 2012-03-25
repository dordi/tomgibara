package com.tomgibara.crinch.record.index;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.process.ProcessContext;

class HashStats extends IndexStats {

	boolean positional;
	boolean ordinal;
	int tableSize;
	int[] hashSeeds;
	int positionBits;
	int ordinalBits;
	
	HashStats(ProcessContext context, SubRecordDef subRecDef) {
		super("hash", context, subRecDef);
	}

	@Override
	public void writeTo(CodedWriter writer) {
		writer.getWriter().writeBoolean(positional);
		writer.getWriter().writeBoolean(ordinal);
		writer.writePositiveInt(tableSize);
		CodedStreams.writePrimitiveArray(writer, hashSeeds);
		writer.writePositiveInt(positionBits);
		writer.writePositiveInt(ordinalBits);
	}

	@Override
	public void readFrom(CodedReader reader) {
		positional = reader.getReader().readBoolean();
		ordinal = reader.getReader().readBoolean();
		tableSize = reader.readPositiveInt();
		hashSeeds = CodedStreams.readIntArray(reader);
		positionBits = reader.readPositiveInt();
		ordinalBits = reader.readPositiveInt();
	}
	

	
}
