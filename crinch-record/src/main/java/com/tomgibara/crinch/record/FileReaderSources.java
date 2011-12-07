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
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileReaderSources implements Iterable<ReaderSource> {

	private static final File[] NO_FILES = {};
	
	private final File dir;
	private final String suffix;
	private final String encoding;
	
	public FileReaderSources(File dir, String suffix, String encoding) {
		this.dir = dir;
		this.suffix = suffix;
		this.encoding = encoding;
	}
	
	@Override
	public Iterator<ReaderSource> iterator() {
		File[] files = dir.listFiles(new Filter(suffix));
		return new I(files == null ? NO_FILES : files, encoding);
	}
	
	private static class Filter implements FilenameFilter {

		private final String suffix;
		
		Filter(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public boolean accept(File file, String filename) {
			return filename.endsWith(suffix);
		}
		
	}
	
	private static class I implements Iterator<ReaderSource> {

		private final File[] files;
		private final String encoding;
		private int index = 0;
		
		public I(File[] files, String encoding) {
			this.files = files;
			this.encoding = encoding;
		}
		
		@Override
		public boolean hasNext() {
			return index < files.length;
		}

		@Override
		public ReaderSource next() {
			if (index == files.length) throw new NoSuchElementException();
			return new FileReaderSource(files[index++], encoding);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
