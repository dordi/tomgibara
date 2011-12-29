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
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.bits.ByteBasedBitReader;
import com.tomgibara.crinch.bits.FileBitReaderFactory;
import com.tomgibara.crinch.bits.FileBitReaderFactory.Mode;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.CombinedRecord;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.compact.RecordDecompactor;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.dynamic.LinkedRecord;
import com.tomgibara.crinch.record.dynamic.LinkedRecordList;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

//TODO lots of work here - make reading into memory optional, support memory mapping too
public class TrieProducer implements RecordProducer<LinearRecord> {

	public static enum Case {
		MIXED, UPPER, LOWER
	}
	
	private static ClassConfig sConfig = new ClassConfig(false, true, false);

	private final SubRecordDef subRecDef;
	
	private File file;
	private HuffmanCoding huffmanCoding;
	private ExtendedCoding coding;
	private FileBitReaderFactory fbrf;
	private int maxLength;
	private RecordDecompactor decompactor;
	private RecordDef recordDef;
	private DynamicRecordFactory factory;
	private boolean uniqueKeys;
	
	public TrieProducer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		RecordDef def = context.getRecordDef();
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
		
		fbrf = new FileBitReaderFactory(file, Mode.CHANNEL);
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
		fbrf = null;
		decompactor = null;
		recordDef = null;
		factory = null;
	}
	
	public class Accessor implements RecordSequence<LinearRecord> {
	
		private final ByteBasedBitReader reader;
		private final RecordDecompactor decompactor = TrieProducer.this.decompactor.copy();
		private final CodedReader coded;

		private boolean autoClose = false;
		private Case casing = Case.MIXED;
		private String prefixKey = "";
		private long prefixPosition = 0L;
		private boolean initial = true;
		private boolean exact = false;

		private boolean closed = false;
		private StringBuilder key;
		long nextRecordPosition;
		long finalRecordPosition;
		private long[] childPositions;
		private long[] siblingPositions;
		private LinearRecord next = null;
		
		public Accessor() {
			reader = fbrf.openReader();
			coded = new CodedReader(reader, coding);
		}

		public Accessor autoClose(boolean autoClose) {
			checkNotClosed();
			this.autoClose = autoClose;
			return this;
		}
		
		public Accessor prefix(String prefix) {
			if (prefix == null) throw new IllegalArgumentException("null prefix");
			configure(prefix);
			exact = false;
			return this;
		}
		
		public Accessor key(String key) {
			if (key == null) throw new IllegalArgumentException("null key");
			configure(key);
			exact = true;
			return this;
		}

		public Accessor casing(Case casing) {
			if (casing == null) throw new IllegalArgumentException("null casing");
			this.casing = casing;
			configure(prefixKey);
			return this;
		}
		
		@Override
		public boolean hasNext() {
			if (initial) next = advance();
			return next != null;
		}
		
		@Override
		public LinearRecord next() {
			if (initial) next = advance();
			if (next == null) throw new NoSuchElementException();
			LinearRecord tmp = next;
			next = advance();
			return tmp;
		}

		public List<LinearRecord> allNext() {
			LinkedRecordList<LinkedRecord> list = new LinkedRecordList<LinkedRecord>();
			while (hasNext()) {
				list.add((LinkedRecord) next());
			}
			return (List) Collections.unmodifiableList(list);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			closed = true;
			fbrf.closeReader(reader);
		}
		
		private void configure(String str) {
			checkNotClosed();
			if (str != null) locate(str);
			initial = true;
			nextRecordPosition = 0L;
			finalRecordPosition = 0L;
		}

		// finishes with reader positioned to read record(s)
		private void locate(String key) {
			if (key.length() > maxLength) {
				prefixKey = null;
				prefixPosition = -1L;
				return;
			}
			reader.setPosition(0L);
			boolean root = true;
			int index = 0;
			StringBuilder sb = new StringBuilder();
			while (true) {
				boolean followChild;
				if (root) {
					if (key.isEmpty()) {
						prefixKey = "";
						prefixPosition = 0L;
						return;
					}
					followChild = true;
					root = false;
				} else {
					char c = (char) (huffmanCoding.decodePositiveInt(reader) - 1);
					if (charsMatch(key.charAt(index), c)) {
						sb.append(c);
						if (++index == key.length()) {
							prefixKey = sb.toString();
							prefixPosition = reader.getPosition();
							return;
						}
						followChild = true;
					} else {
						followChild = false;
					}
				}
				if (uniqueKeys) {
					if (reader.readBoolean()) readRecord(key);
				} else {
					reader.skipBits(coded.readPositiveLong() - 1L);
				}
				long childOffset = coded.readPositiveLong() - 2L;
				long siblingOffset = coded.readPositiveLong() - 2L;
				if (followChild) {
					if (childOffset < 0) {
						prefixKey = null;
						prefixPosition = -1L;
						return;
					}
					reader.skipBits(childOffset);
				} else {
					if (siblingOffset < 0) {
						prefixKey = null;
						prefixPosition = -1L;
						return;
					}
					reader.skipBits(siblingOffset);
				}
			}
		}
		
		private boolean charsMatch(char c, char d) {
			switch (casing) {
			case MIXED : return c == d;
			case UPPER : return Character.toUpperCase(c) == Character.toUpperCase(d);
			case LOWER : return Character.toLowerCase(c) == Character.toLowerCase(d);
			default : throw new IllegalStateException("Unexpected case: " + casing);
			}
		}
		
		//TODO unfortunate to create so much garbage when walking trie
		private LinearRecord readRecord(CharSequence key) {
			long ordinal = recordDef.isOrdinal() ? coded.readPositiveLong() - 1L : -1L;
			long position = recordDef.isPositional() ? coded.readPositiveLong() - 1L : -1L;
			LinearRecord record = decompactor.decompact(coded, ordinal, position);
			return factory.newRecord(sConfig, new CombinedRecord(new SingletonRecord(record.getOrdinal(), record.getPosition(), key.toString()), record));
		}
		
		private void prepare() {
			if (key == null) {
				key = new StringBuilder(maxLength);
				siblingPositions = new long[maxLength + 1];
				childPositions = new long[maxLength + 1];
			}
		}
		
		private LinearRecord advance() {
			checkNotClosed();
			if (prefixPosition < 0L) return null;
			
			if (initial) {
				prepare();
				key.setLength(0);
				key.append(prefixKey);
				reader.setPosition(prefixPosition);
			}

			if (nextRecordPosition < finalRecordPosition) {
				//TODO recording & setting the next record position may not now be necessary
				// since locate and advance are now the only methods that change reader position
				reader.setPosition(nextRecordPosition);
				LinearRecord record = readRecord(key);
				nextRecordPosition = reader.getPosition();
				return record;
			}
			
			int depth = key.length();
			while (true) {
				if (!initial) {
					if (exact) return terminus();
					
					while (true) {
						long childPosition = childPositions[depth]; 
						if (childPosition >= 0) {
							reader.setPosition(childPosition);
							childPositions[depth] = -1L;
							break;
						}

						if (depth == prefixKey.length()) return terminus();

						long siblingPosition = siblingPositions[depth];
						if (siblingPosition >= 0) {
							reader.setPosition(siblingPosition);
							key.setLength(--depth);
							initial = false;
							break;
						}

						key.setLength(--depth);
					}
					key.append( (char) (huffmanCoding.decodePositiveInt(reader) - 1) );
					depth++;
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
				childPositions[depth] = childOffset < 0 ? -1L : childOffset + position;
				siblingPositions[depth] = siblingOffset < 0 ? -1L : siblingOffset + position;

				if (record != null) return record;
			}

		}
		
		private LinearRecord terminus() {
			if (autoClose) closed = true;
			return null;
		}
		
		private void checkNotClosed() {
			if (closed) throw new IllegalStateException("closed");
		}
		
	}
	
}
