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
package com.tomgibara.crinch.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.record.ReaderSource;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.StringRecord;
import com.tomgibara.crinch.record.process.ProcessContext;


import au.com.bytecode.opencsv.CSVReader;

public class CsvProducer implements RecordProducer<StringRecord> {

	private Iterable<? extends ReaderSource> readerSources;
	private char separator;

	CsvProducer() {
	}

	void init(Iterable<? extends ReaderSource> readerSources, char separator) {
		if (readerSources == null) throw new IllegalArgumentException("null readerSources");
		this.readerSources = readerSources;
		this.separator = separator;
	}
	
	public CsvProducer(Iterable<? extends ReaderSource> readerSources) {
		this(readerSources, ',');
	}

	public CsvProducer(Iterable<? extends ReaderSource> readerSources, char separator) {
		init(readerSources, separator);
	}
	
	@Override
	public void prepare(ProcessContext context) {
	}
	
	@Override
	public RecordSequence<StringRecord> open() {
		
		return new RecordSequence<StringRecord>() {

			private Iterator<? extends ReaderSource> iterator = readerSources.iterator();
			private Reader reader = null;
			CSVReader csvReader = null;
			private StringRecord next;
			private long recordCount = 0L;
			
			{ while (advance()); }
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public StringRecord next() {
				if (next == null) throw new NoSuchElementException();
				try {
					return next;
				} finally {
					while (advance());
				}
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
			
			@Override
			public void close() {
				if (iterator != null) {
					try {
						reader.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						reader = null;
						csvReader = null;
						iterator = null;
					}					
				}
			}
			
			private boolean advance() {
				try {
					if (iterator == null) throw new NoSuchElementException();
					next = null;
					if (reader == null) {
						if (!iterator.hasNext()) {
							iterator = null;
							return false;
						}
						ReaderSource source = iterator.next();
						reader = source.open();
						csvReader = new CSVReader(reader, separator);
						return true;
					}
					String[] strs = csvReader.readNext();
					if (strs == null) {
						reader.close();
						reader = null;
						csvReader = null;
						return true;
					}
					next = new StringRecord(recordCount++, -1L, strs);
					return false;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Override
	public void complete() {
	}

}
