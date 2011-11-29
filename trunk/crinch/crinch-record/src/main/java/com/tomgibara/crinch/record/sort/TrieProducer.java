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
package com.tomgibara.crinch.record.sort;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.CombinedRecord;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.compact.RecordDecompactor;
import com.tomgibara.crinch.record.def.RecordDefinition;
import com.tomgibara.crinch.record.def.SubRecordDefinition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;

//TODO lots of work here - make reading into memory optional, support memory mapping too
public class TrieProducer implements RecordProducer<LinearRecord> {

	private static ClassConfig sConfig = new ClassConfig(false, false);

	private final SubRecordDefinition subRecDef;
	
	private File file;
	private HuffmanCoding huffmanCoding;
	private ExtendedCoding coding;
	private byte[] data;
	private int maxLength;
	private RecordDecompactor decompactor;
	private RecordDefinition recordDef;
	private DynamicRecordFactory factory;
	private boolean uniqueKeys;
	
	public TrieProducer(SubRecordDefinition subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		RecordDefinition def = context.getRecordDef();
		if (def == null) throw new IllegalStateException("context has no record definition");
		def = def.asBasis();
		if (subRecDef != null) def = def.asSubRecord(subRecDef);
		recordDef = def;
		factory = DynamicRecordFactory.getInstance(recordDef);
		
		File statsFile = context.file("trie", true, recordDef);
		final long[][] arr = new long[1][];
		CodedStreams.readFromFile(new CodedStreams.ReadTask() {
			@Override
			public void readFrom(CodedReader reader) {
				//TODO should switch to non-neg method when it becomes available
				arr[0] = CodedStreams.readLongArray(reader);
			}
		}, context.getCoding(), statsFile);
		long[] frequencies = arr[0];

		RecordStats stats = context.getRecordStats();
		if (stats == null) throw new IllegalStateException("context has no record stats");
		stats = stats.adaptFor(def);
		decompactor = new RecordDecompactor(stats, 1);
		ColumnStats keyStats = stats.getColumnStats().get(0);
		maxLength = keyStats.getMaximum().intValue();
		uniqueKeys = keyStats.isUnique();

		file = context.file("trie", false, recordDef);
		huffmanCoding = new HuffmanCoding(new HuffmanCoding.UnorderedFrequencyValues(frequencies));
		coding = context.getCoding();
		
		//TODO need to support non-memory operation
		int size = (int) file.length();
		byte[] data = new byte[size];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			new DataInputStream(in).readFully(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				context.log("Failed to close file", e);
			}
		}
		this.data = data;
	}
	
	@Override
	public Accessor open() {
		return new Accessor();
	}
	
	@Override
	public void complete() {
		file = null;
		huffmanCoding = null;
		coding = null;
		data = null;
		decompactor = null;
		recordDef = null;
		factory = null;
	}
	
	public class Accessor implements RecordSequence<LinearRecord> {
	
		private final ByteArrayBitReader reader;
		private final CodedReader coded;

		private StringBuilder key;
		long nextRecordPosition;
		long finalRecordPosition;
		private long[] childPositions;
		private long[] siblingPositions;
		private LinearRecord next = null;
		
		public Accessor() {
			reader = new ByteArrayBitReader(data);
			coded = new CodedReader(reader, coding);
		}

		//TODO unfortunate to create so much garbage when walking trie
		private LinearRecord readRecord(CharSequence key) {
			long ordinal = recordDef.isOrdinal() ? coded.readPositiveLong() - 1L : -1L;
			long position = recordDef.isPositional() ? coded.readPositiveLong() - 1L : -1L;
			LinearRecord record = decompactor.decompact(coded, ordinal, position);
			return factory.newRecord(sConfig, new CombinedRecord(new SingletonRecord(record.getRecordOrdinal(), record.getRecordPosition(), key.toString()), record));
		}
		
		@Override
		public boolean hasNext() {
			if (unused()) next = advance();
			return next != null;
		}
		
		@Override
		public LinearRecord next() {
			if (unused()) next = advance();
			if (next == null) throw new NoSuchElementException();
			LinearRecord tmp = next;
			next = advance();
			return tmp;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			/* no-op atm */
		}
		
		//TODO produce API that can return multiple records for single key
		public LinearRecord get(String key) {
			if (key == null) throw new IllegalArgumentException("null key");
			reader.setPosition(0L);
			boolean root = true;
			int index = 0;
			LinearRecord record = null;
			long recordPosition = -1L;
			while (true) {
				char c = root ? '\0' : (char) (huffmanCoding.decodePositiveInt(reader) - 1);
				if (uniqueKeys) {
					record = reader.readBoolean() ? readRecord(key) : null;
				} else {
					long reclen = coded.readPositiveLong() - 1L;
					recordPosition = reclen == 0L ? -1L : reader.getPosition();
					reader.skipBits(reclen);
				}
				long childOffset = coded.readPositiveLong() - 2L;
				long siblingOffset = coded.readPositiveLong() - 2L;
				boolean hasChild = childOffset >= 0;
				boolean hasSibling = siblingOffset >= 0;
				if (root) {
					if (key.isEmpty()) {
						if (uniqueKeys) return record;
						if (recordPosition == -1L) return null;
						reader.setPosition(recordPosition);
						return readRecord(key);
					}
					if (!hasChild) return null;
					reader.skipBits(childOffset);
					root = false;
				} else if (key.charAt(index) == c) {
					if (++index == key.length()) {
						if (uniqueKeys) return record;
						if (recordPosition == -1L) return null;
						reader.setPosition(recordPosition);
						return readRecord(key);
					}
					if (!hasChild) return null;
					reader.skipBits(childOffset);
				} else {
					if (!hasSibling) return null;
					reader.skipBits(siblingOffset);
				}
			}
		}
	
		public boolean contains(String key) {
			return get(key) != null;
		}

		private boolean unused() {
			return key == null;
		}
		
		private LinearRecord advance() {
			boolean initial = unused();
			if (initial) {
				key = new StringBuilder(maxLength);
				siblingPositions = new long[maxLength + 1];
				childPositions = new long[maxLength + 1];
				reader.setPosition(0L);
			}

			if (nextRecordPosition < finalRecordPosition) {
				reader.setPosition(nextRecordPosition);
				LinearRecord record = readRecord(key);
				nextRecordPosition = reader.getPosition();
				return record;
			}
			
			while (true) {
				if (!initial) {
					int depth = key.length();
					while (true) {
						long childPosition = childPositions[depth]; 
						if (childPosition >= 0) {
							reader.setPosition(childPosition);
							childPositions[depth] = -1L;
							break;
						}
						
						long siblingPosition = siblingPositions[depth];
						if (siblingPosition >= 0) {
							reader.setPosition(siblingPosition);
							key.setLength(--depth);
							initial = false;
							break;
						}

						if (depth == 0) return null;
						key.setLength(--depth);
					}
					key.append( (char) (huffmanCoding.decodePositiveInt(reader) - 1) );
				}

				initial = false;
				LinearRecord record;
				if (uniqueKeys) {
					record = reader.readBoolean() ? readRecord(key) : null;
				} else {
					long reclen = coded.readPositiveLong() - 1L;
					if (reclen == 0L) {
						record = null;
					} else {
						finalRecordPosition = reader.getPosition() + reclen;
						record = readRecord(key);
						nextRecordPosition = reader.getPosition();
						reader.setPosition(finalRecordPosition);
					}
				}
				long childOffset = coded.readPositiveLong() - 2L;
				long siblingOffset = coded.readPositiveLong() - 2L;
				long position = reader.getPosition();
				int depth = key.length();
				childPositions[depth] = childOffset < 0 ? -1L : childOffset + position;
				siblingPositions[depth] = siblingOffset < 0 ? -1L : siblingOffset + position;

				if (record != null) return record;
			}

		}
		
	}
	
}
