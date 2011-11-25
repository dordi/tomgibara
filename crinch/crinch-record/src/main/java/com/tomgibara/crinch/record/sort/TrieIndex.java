package com.tomgibara.crinch.record.sort;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.CombinedRecord;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.compact.RecordDecompactor;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDefinition;
import com.tomgibara.crinch.record.def.SubRecordDefinition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;

//TODO lots of work here - make reading into memory optional, support memory mapping too
public class TrieIndex {

	private final File file;
	private final HuffmanCoding huffmanCoding;
	private final ExtendedCoding coding;
	private final byte[] data;
	private final RecordDecompactor decompactor;
	private final RecordDefinition recordDef;
	private final DynamicRecordFactory factory;
	
	public TrieIndex(ProcessContext context, SubRecordDefinition subRecDef) {
		RecordDefinition def = context.getRecordDef();
		if (def == null) throw new IllegalStateException("context has no record definition");
		def = def.asBasis();
		if (subRecDef != null) def = def.asSubRecord(subRecDef);
		recordDef = def;
		factory = DynamicRecordFactory.getInstance(recordDef);
		
		File statsFile = new File(context.getOutputDir(), context.getDataName() + ".trie-stats." + recordDef.getId());
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
		decompactor = new RecordDecompactor(stats.adaptFor(def), 1);

		file = new File(context.getOutputDir(), context.getDataName() + ".trie." + recordDef.getId());
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
	
	public Accessor newAccessor() {
		return new Accessor();
	}
	
	public class Accessor {
	
		private final ByteArrayBitReader reader;
		private final CodedReader coded;

		public Accessor() {
			reader = new ByteArrayBitReader(data);
			coded = new CodedReader(reader, coding);
		}
		
		public LinearRecord getRecord(String key) {
			if (key == null) throw new IllegalArgumentException("null key");
			reader.setPosition(0L);
			boolean root = true;
			int index = 0;
			while (true) {
				char c = root ? '\0' : (char) (huffmanCoding.decodePositiveInt(reader) - 1);
				LinearRecord record;
				if (coded.getReader().readBoolean()) {
					long ordinal = recordDef.isOrdinal() ? coded.readPositiveLong() - 1L : -1L;
					long position = recordDef.isPositional() ? coded.readPositiveLong() - 1L : -1L;
					record = decompactor.decompact(coded, ordinal, position);
				} else {
					record = null;
				}
				long childOffset = coded.readPositiveLong() - 2L;
				long siblingOffset = coded.readPositiveLong() - 2L;
				boolean hasChild = childOffset >= 0;
				boolean hasSibling = siblingOffset >= 0;
				if (root) {
					if (key.isEmpty()) return unite(key, record);
					if (!hasChild) return null;
					reader.skipBits(childOffset);
					root = false;
				} else if (key.charAt(index) == c) {
					if (++index == key.length()) return unite(key, record);
					if (!hasChild) return null;
					reader.skipBits(childOffset);
				} else {
					if (!hasSibling) return null;
					reader.skipBits(siblingOffset);
				}
			}
		}
	
		public boolean contains(String key) {
			return getRecord(key) != null;
		}
		
		public void dump() {
			reader.setPosition(0L);
			dump(null);
		}
		
		private LinearRecord unite(String key, LinearRecord record) {
			if (record == null) return null;
			return factory.newRecord(new CombinedRecord(new SingletonRecord(record.getRecordOrdinal(), record.getRecordPosition(), key), record));
		}
		
		private void dump(StringBuilder sb) {
			boolean root = sb == null;
			while (true) {
				char c = root ? '\0' : (char) (huffmanCoding.decodePositiveInt(reader) - 1);
				LinearRecord record;
				if (coded.getReader().readBoolean()) {
					long ordinal = recordDef.isOrdinal() ? coded.readPositiveLong() - 1L : -1L;
					long position = recordDef.isPositional() ? coded.readPositiveLong() - 1L : -1L;
					record = decompactor.decompact(coded, ordinal, position);
				} else {
					record = null;
				}
				long childOffset = coded.readPositiveLong() - 2L;
				long siblingOffset = coded.readPositiveLong() - 2L;
				boolean hasChild = childOffset >= 0;
				boolean hasSibling = siblingOffset >= 0;
				
				if (root) {
					sb = new StringBuilder();
					if (record != null) System.out.println(sb);
					if (!hasChild) return;
					reader.skipBits(childOffset);
					dump(sb);
					return;
				}
	
				sb.append(c);
				if (record != null) System.out.println(sb);
				if (hasChild) {
					long position = reader.getPosition();
					reader.skipBits(childOffset);
					dump(sb);
					reader.setPosition(position);
				}
				sb.setLength(sb.length() - 1);
				if (hasSibling) {
					reader.skipBits(siblingOffset);
				} else {
					return;
				}
			}
		}
	}
	
}
