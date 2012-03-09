package com.tomgibara.crinch.record.compact;

import java.io.File;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.process.ProcessContext;

//TODO unify with index stats
class CompactStats implements CodedStreams.WriteTask, CodedStreams.ReadTask {

	private static RecordDef definition(ProcessContext context, SubRecordDef subRecDef) {
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		if (subRecDef != null) def = def.asSubRecord(subRecDef);
		return def.asBasis();
	}

	final String type;
	final RecordDef definition;
	final ExtendedCoding coding;
	final File file;
	
	long bitsWritten;

	CompactStats(String type, ProcessContext context) {
		this(type, context, (SubRecordDef) null);
	}

	CompactStats(String type, ProcessContext context, SubRecordDef subRecDef) {
		this(type, context, definition(context, subRecDef));
	}
	
	CompactStats(String type, ProcessContext context, RecordDef definition) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (context == null) throw new IllegalArgumentException("null context");
		if (definition == null) throw new IllegalArgumentException("null definition");
		this.type = type;
		this.definition = definition;
		coding = context.getCoding();
		file = context.file("compact", true, definition);
		if (context.isClean()) file.delete();
	}
	
	void write() {
		CodedStreams.writeToFile(this, coding, file);
	}
	
	void read() {
		CodedStreams.readFromFile(this, coding, file);
	}

	@Override
	public void writeTo(CodedWriter writer) {
		writer.writePositiveLong(bitsWritten + 1L);
	}
	
	@Override
	public void readFrom(CodedReader reader) {
		bitsWritten = reader.readPositiveLong() - 1L;
	}
	
}
