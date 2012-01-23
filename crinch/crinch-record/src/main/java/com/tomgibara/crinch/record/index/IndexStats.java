package com.tomgibara.crinch.record.index;

import java.io.File;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.process.ProcessContext;

abstract class IndexStats implements CodedStreams.WriteTask, CodedStreams.ReadTask {

	final String indexType;
	final RecordDef definition;
	final ExtendedCoding coding;
	final File file;
	
	IndexStats(String indexType, ProcessContext context) {
		if (indexType == null) throw new IllegalArgumentException("null indexType");
		if (context == null) throw new IllegalArgumentException("null context");
		this.indexType = indexType;
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		definition = def.asBasis();
		coding = context.getCoding();
		file = context.file("hash", true, definition);
		if (context.isClean()) file.delete();
	}

	void write() {
		CodedStreams.writeToFile(this, coding, file);
	}
	
	void read() {
		CodedStreams.readFromFile(this, coding, file);
	}

}
