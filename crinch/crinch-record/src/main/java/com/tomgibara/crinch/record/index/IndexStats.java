package com.tomgibara.crinch.record.index;

import java.io.File;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.process.ProcessContext;

//TODO consider sharing across packages (difference in definition!)
abstract class IndexStats implements CodedStreams.WriteTask, CodedStreams.ReadTask {

	private static RecordDef definition(ProcessContext context, SubRecordDef subRecDef) {
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		def = def.asBasis();
		if (subRecDef != null) def = def.asSubRecord(subRecDef);
		return def;
	}

	final String type;
	final RecordDef definition;
	final ExtendedCoding coding;
	final File file;
	
	IndexStats(String type, ProcessContext context) {
		this(type, context, (SubRecordDef) null);
	}

	IndexStats(String type, ProcessContext context, SubRecordDef subRecDef) {
		this(type, context, definition(context, subRecDef));
	}
	
	IndexStats(String type, ProcessContext context, RecordDef definition) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (context == null) throw new IllegalArgumentException("null context");
		this.type = type;
		this.definition = definition;
		coding = context.getCoding();
		file = context.file(type, true, definition);
		if (context.isClean()) file.delete();
	}

	void write() {
		CodedStreams.writeToFile(this, coding, file);
	}
	
	void read() {
		CodedStreams.readFromFile(this, coding, file);
	}

}
