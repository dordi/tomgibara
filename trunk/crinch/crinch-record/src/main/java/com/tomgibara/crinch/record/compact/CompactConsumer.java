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
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;

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
				context.log("Failed to flush writer", e);
			} finally {
				writer = null;
				coded = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				context.log("Failed to close file", e);
			} finally {
				out = null;
			}
		}
		if (context != null) {
			context = null;
		}
	}
	
}
