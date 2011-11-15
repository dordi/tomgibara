package com.tomgibara.crinch.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileReaderSource implements ReaderSource {

	private final File file;
	private final String encoding;
	
	public FileReaderSource(File file, String encoding) {
		this.file = file;
		this.encoding = encoding;
	}
	
	@Override
	public String getName() {
		return file.getName();
	}
	
	@Override
	public Reader open() throws IOException {
		return new InputStreamReader(new FileInputStream(file), encoding);
	}
	
}
