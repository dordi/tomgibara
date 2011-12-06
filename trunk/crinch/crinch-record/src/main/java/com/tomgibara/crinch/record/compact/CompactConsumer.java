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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class CompactConsumer implements RecordConsumer<LinearRecord> {

	private static final int PASS_TYPES = 0;
	private static final int PASS_STATS = 1;
	private static final int PASS_COMPACT = 2;
	private static final int PASS_DONE = 3;
	
	private ProcessContext context = null;
	private int pass;
	private RecordTyper typer;
	private RecordAnalyzer analyzer;
	private RecordCompactor compactor;
	private OutputStream out;
	private OutputStreamBitWriter writer;
	private CodedWriter coded;
	private long bitsWritten;
	
	public CompactConsumer() {
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		if (context.getColumnTypes() == null) {
			pass = PASS_TYPES;
		} else if (context.getRecordStats() == null) {
			pass = PASS_STATS;
		} else {
			if (context.isClean()) file().delete();
			if (!file().isFile()) {
				pass = PASS_COMPACT;
			} else {
				pass = PASS_DONE;
			}
		}
	}

	@Override
	public int getRequiredPasses() {
		return PASS_DONE - pass;
	}

	@Override
	public void beginPass() {
		switch (pass) {
		case PASS_TYPES:
			context.setPassName("Identifying types");
			typer = new RecordTyper(context);
			break;
		case PASS_STATS:
			if (analyzer == null) {
				analyzer = new RecordAnalyzer(context);
				context.setPassName("Gathering statistics");
			} else {
				context.setPassName("Identifying unique values");
			}
			break;
		case PASS_COMPACT:
			context.setPassName("Compacting data");
			compactor = new RecordCompactor(context, null, 0);
			open();
			break;
		}
	}

	@Override
	public void consume(LinearRecord record) {
		switch (pass) {
		case PASS_TYPES:
			typer.type(record);
			break;
		case PASS_STATS:
			analyzer.analyze(record);
			break;
		case PASS_COMPACT:
			int c = compactor.compact(coded, record);
			bitsWritten += c;
			break;
		}
	}

	@Override
	public void endPass() {
		switch(pass) {
		case PASS_TYPES:
			context.setRecordCount(typer.getRecordCount());
			context.setColumnTypes(typer.getColumnTypes());
			pass = PASS_STATS;
			break;
		case PASS_STATS:
			if (!analyzer.needsReanalysis()) {
				context.setRecordStats(analyzer.getStats());
				pass = PASS_COMPACT;
			}
			break;
		case PASS_COMPACT:
			pass = PASS_DONE;
			break;
		default : throw new IllegalStateException("too many passes");
		}
	}

	@Override
	public void complete() {
		close();
	}

	@Override
	public void quit() {
		close();
	}

	private File file() {
		//TODO should change to include ordering when that has been added to stats
		return context.file("compact", false, context.getRecordDef().getBasisOrSelf());
	}
	
	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(file()), 1024);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer = new OutputStreamBitWriter(out);
		coded = new CodedWriter(writer, context.getCoding());
		bitsWritten = 0L;
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.getLogger().log(Level.ERROR, "Failed to flush writer", e);
			} finally {
				writer = null;
				coded = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				context.getLogger().log(Level.WARN, "Failed to close file", e);
			} finally {
				out = null;
			}
		}
		if (context != null) {
			context = null;
		}
	}
	
}
