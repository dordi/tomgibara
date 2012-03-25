package com.tomgibara.crinch.csv;

import java.io.File;
import java.util.Collections;

import com.tomgibara.crinch.record.GZipReaderSource;
import com.tomgibara.crinch.record.process.ProcessContext;

public class SnapshotProducer extends CsvProducer {

	public SnapshotProducer() {
	}

	@Override
	public void prepare(ProcessContext context) {
		File file = context.file("snapshot", false, null);
		if (!file.isFile()) throw new IllegalArgumentException("context has no snapshot");
		init(Collections.singleton(new GZipReaderSource(file, "UTF-8")), ',');
		super.prepare(context);
	}
	
}
