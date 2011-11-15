package com.tomgibara.crinch.record.sort;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnOrder;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordDefinition;

//TODO lots of work here - decide if ordinal flag should be in constructor, make reading into memory optional, support memory mapping too
public class TrieIndex {

	private final File file;
	private final boolean ordinal;
	private final HuffmanCoding huffmanCoding;
	private final ExtendedCoding coding;
	
	private ByteArrayBitReader reader;
	private CodedReader coded;
	
	public TrieIndex(ProcessContext context, int columnIndex, ColumnOrder... orders) {
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		RecordDefinition def = new RecordDefinition(false, false, types, orders);
		
		File statsFile = new File(context.getOutputDir(), context.getDataName() + ".col-" + columnIndex + ".trie-stats." + def.getId());
		final Object[] arr = new Object[2];
		CodedStreams.readFromFile(new CodedStreams.ReadTask() {
			@Override
			public void readFrom(CodedReader reader) {
				arr[0] = reader.getReader().readBoolean();
				//TODO should switch to non-neg method when it becomes available
				arr[1] = CodedStreams.readLongArray(reader);
			}
		}, context.getCoding(), statsFile);
		long[] frequencies = (long[]) arr[1];
		
		file = new File(context.getOutputDir(), context.getDataName() + ".col-" + columnIndex + ".trie." + def.getId());
		ordinal = (Boolean) arr[0];
		huffmanCoding = new HuffmanCoding(new HuffmanCoding.UnorderedFrequencyValues(frequencies));
		coding = context.getCoding();
		
		//TODO remove testy hackiness
		int size = (int) file.length();
		byte[] memory = new byte[size];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			new DataInputStream(in).readFully(memory);
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		reader = new ByteArrayBitReader(memory);
		coded = new CodedReader(reader, coding);
	}
	
	public long getValue(String key) {
		reader.setPosition(0L);
		boolean root = true;
		int index = 0;
		while (true) {
			char c = root ? '\0' : (char) (huffmanCoding.decodePositiveInt(reader) - 1);
			long value = coded.readPositiveLong() - 2L;
			long childOffset = coded.readPositiveLong() - 2L;
			long siblingOffset = coded.readPositiveLong() - 2L;
			boolean hasChild = childOffset >= 0;
			boolean hasSibling = siblingOffset >= 0;
			if (root) {
				if (key.isEmpty()) return value;
				if (!hasChild) return -1L;
				reader.skipBits(childOffset);
				root = false;
			} else if (key.charAt(index) == c) {
				if (++index == key.length()) return value;
				if (!hasChild) return -1L;
				reader.skipBits(childOffset);
			} else {
				if (!hasSibling) return -1L;
				reader.skipBits(siblingOffset);
			}
		}
	}

	public void dump() {
		reader.setPosition(0L);
		dump(null);
	}
	
	private void dump(StringBuilder sb) {
		boolean root = sb == null;
		while (true) {
			char c = root ? '\0' : (char) (huffmanCoding.decodePositiveInt(reader) - 1);
			long value = coded.readPositiveLong() - 2L;
			long childOffset = coded.readPositiveLong() - 2L;
			long siblingOffset = coded.readPositiveLong() - 2L;
			boolean hasChild = childOffset >= 0;
			boolean hasSibling = siblingOffset >= 0;
			
			if (root) {
				sb = new StringBuilder();
				if (value != -1L) System.out.println(sb);
				if (!hasChild) return;
				reader.skipBits(childOffset);
				dump(sb);
				return;
			}

			sb.append(c);
			if (value != -1L) System.out.println(sb);
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
