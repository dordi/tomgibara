package com.tomgibara.crinch.record.compact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.ValueParser;

public class CompactConsumer implements RecordConsumer<LinearRecord> {

	private static final int PASS_TYPE = 0;
	private static final int PASS_STATS = 1;
	private static final int PASS_WRITE = 2;
	private static final int PASS_DONE = 3;
	
	private final ValueParser parser;
	private final File outFile;
	private ProcessContext context = null;
	private int pass;
	private RecordTyper typer;
	private RecordAnalyzer analyzer;
	private RecordCompactor compactor;
	private OutputStream out;
	private OutputStreamBitWriter writer;
	private CodedWriter coded;
	private long bitsWritten;
	
	public CompactConsumer(ValueParser parser, File outFile) {
		if (parser == null) throw new IllegalArgumentException("null parser");
		if (outFile == null) throw new IllegalArgumentException("null outFile");
		this.parser = parser;
		this.outFile = outFile;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		pass = PASS_TYPE;
		this.context = context;
	}

	@Override
	public int getRequiredPasses() {
		return PASS_DONE - pass;
	}

	@Override
	public void beginPass() {
		switch (pass) {
		case PASS_TYPE:
			context.setPassName("Identifying types");
			typer = new RecordTyper(parser);
			break;
		case PASS_STATS:
			context.setPassName("Gathering statistics");
			analyzer = typer.analyzer();
			break;
		case PASS_WRITE:
			context.setPassName("Compacting data");
			compactor = analyzer.compactor();
			open();
			RecordStats.write(coded, context.getRecordStats());
			break;
		}
	}

	@Override
	public void consume(LinearRecord record) {
		switch (pass) {
		case PASS_TYPE:
			typer.type(record);
			break;
		case PASS_STATS:
			analyzer.analyze(record);
			break;
		case PASS_WRITE:
			int c = compactor.compact(coded, record);
			bitsWritten += c;
			break;
		}
	}

	@Override
	public void endPass() {
		switch(pass) {
		case PASS_TYPE:
			context.setColumnTypes(typer.getColumnTypes());
			break;
		case PASS_STATS:
			context.setRecordStats(analyzer.stats());
			break;
		}
		pass++;
	}

	@Override
	public void complete() {
		close();
	}

	@Override
	public void quit() {
		close();
	}

	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(outFile), 1024);
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
				writer.padToByteBoundary();
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
