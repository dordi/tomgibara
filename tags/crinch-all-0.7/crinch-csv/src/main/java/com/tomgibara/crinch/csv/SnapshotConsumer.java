package com.tomgibara.crinch.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import au.com.bytecode.opencsv.CSVWriter;

import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.StringRecord;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class SnapshotConsumer implements RecordConsumer<StringRecord> {

	private ProcessContext context;
	private File file;
	private CSVWriter writer;
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		file = context.file("snapshot", false, null);
		if (context.isClean()) file.delete();
	}

	@Override
	public int getRequiredPasses() {
		return file.isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		context.setPassName("Creating snapshot");
		try {
			writer = new CSVWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
		} catch (IOException e) {
			throw new RuntimeException("failed to open snapshot file", e);
		}
	}

	@Override
	public void consume(StringRecord record) {
		writer.writeNext(record.getAll());
	}

	@Override
	public void endPass() {
		close();
	}

	@Override
	public void complete() {
	}

	@Override
	public void quit() {
		close();
	}

	private void close() {
		if (writer != null) try {
			writer.close();
		} catch (IOException e) {
			context.getLogger().log(Level.WARN, "failed close snapshot file", e);
		} finally {
			writer = null;
		}
	}
	
}
