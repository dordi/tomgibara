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
