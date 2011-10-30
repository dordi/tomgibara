package com.tomgibara.crinch.record.compact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.ValueParser;

public class CompactConsumer implements RecordConsumer<LinearRecord> {

	private static final int PASS_TYPE = 0;
	private static final int PASS_STATS = 1;
	private static final int PASS_WRITE = 2;
	private static final int PASS_DONE = 3;
	
	private final ValueParser parser;
	private final File outFile;
	private int pass;
	private RecordTyper typer;
	private RecordAnalyzer analyzer;
	private RecordCompactor compactor;
	private OutputStream out;
	private OutputStreamBitWriter writer;
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
	}

	@Override
	public int getRequiredPasses() {
		return PASS_DONE - pass;
	}

	@Override
	public void beginPass() {
		System.out.println("===== BEGINNING PASS " + pass + " =====");
		switch (pass) {
		case PASS_TYPE:
			typer = new RecordTyper(parser);
			break;
		case PASS_STATS:
			analyzer = typer.analyzer();
			break;
		case PASS_WRITE:
			compactor = analyzer.compactor();
			open();
			System.out.println("Record count: " + analyzer.getRecordCount());
			EliasOmegaEncoding.encodePositiveLong(analyzer.getRecordCount() + 1, writer);
			RecordDecompactor.write(compactor.decompactor(), writer);
			break;
		}
	}

	int count = 0;
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
			int c = compactor.compact(writer, record);
			//System.out.println(c);
			bitsWritten += c;
			break;
		}
	}

	@Override
	public void endPass() {
		switch(pass) {
		case PASS_TYPE:
			System.out.println("Types identified as " + typer.getColumnTypes());
			break;
		case PASS_STATS:
			System.out.println(analyzer.toString());
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
		bitsWritten = 0L;
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToByteBoundary();
				writer.flush();
			} catch (RuntimeException e) {
				e.printStackTrace();
			} finally {
				writer = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out = null;
			}
		}
	}
	
}
