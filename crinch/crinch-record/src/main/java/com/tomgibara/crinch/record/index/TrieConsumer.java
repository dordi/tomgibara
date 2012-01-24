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
package com.tomgibara.crinch.record.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.NullBitWriter;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CharFrequencyRecorder;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedStreams.WriteTask;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.compact.RecordCompactor;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.LinkedRecord;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class TrieConsumer implements RecordConsumer<LinearRecord> {

	private final SubRecordDef subRecDef;
	
	private ProcessContext context;
	private RecordDef definition;
	private DynamicRecordFactory factory;

	private RecordCompactor compactor;
	private Node root;
	private long nodeCount = 0;
	private long[] frequencies;
	private HuffmanCoding huffmanCoding;
	private boolean uniqueKeys;
	private ClassConfig config;
	private NullBitWriter recordSizingWriter;
	private CodedWriter recordSizingCoded;

	
	public TrieConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		definition = def.asBasis();
		if (subRecDef != null) definition = definition.asSubRecord(subRecDef);
		factory = DynamicRecordFactory.getInstance(definition);

		if (definition.getTypes().isEmpty()) throw new IllegalArgumentException("no columns");
		if (definition.getTypes().get(0) != ColumnType.STRING_OBJECT) throw new IllegalStateException("column not a string");
		compactor = new RecordCompactor(context, definition, 1);
		uniqueKeys = compactor.getColumnStats(0).isUnique();
		config = new ClassConfig(true, !uniqueKeys, false);
		recordSizingWriter = new NullBitWriter();
		recordSizingCoded = new CodedWriter(recordSizingWriter, context.getCoding());
		if (context.isClean()) {
			statsFile().delete();
			file().delete();
		}
	}

	@Override
	public int getRequiredPasses() {
		return file().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		context.setPassName("Building trie");
		root = new Node('\0');
	}

	@Override
	public void consume(LinearRecord record) {
		LinearRecord subRec = factory.newRecord(config, record, subRecDef != null);
		final CharSequence key = subRec.nextString();
		subRec.mark();
		final int length = key.length();
		Node node = root;
		outer: for (int i = 0; i < length; i++) {
			char c = key.charAt(i);
			Node child = node.child;
			if (child == null) {
				child = new Node(c);
				node.child = child;
				nodeCount++;
				node = child;
				continue outer;
			}
			while (true) {
				if (child.c == c) {
					node = child;
					continue outer;
				}
				if (child.sibling == null) {
					node = new Node(c);
					child.sibling = node;
					nodeCount++;
					continue outer;
				}
				child = child.sibling;
			}
		}
		if (node.record != null) {
			if (uniqueKeys) throw new IllegalStateException("unexpected duplicate key: " + key);
			((LinkedRecord) subRec).insertRecordBefore((LinkedRecord) node.record);
		} else {
			node.record = subRec;
		}
	}

	@Override
	public void endPass() {
		context.getLogger().log("Node count: " + nodeCount);
		CharFreqRec rec = new CharFreqRec();
		rec.record(root);
		frequencies = rec.getFrequencies();
		huffmanCoding = new HuffmanCoding(new HuffmanCoding.UnorderedFrequencyValues(frequencies));
		new Offsetter().offset(root);
		writeStats();
		Writer writer = new Writer();
		writer.write(root);
		writer.close();
	}

	@Override
	public void complete() {
		cleanup();
	}

	@Override
	public void quit() {
		cleanup();
	}

	// wipe references to free memory
	private void cleanup() {
		compactor = null;
		root = null;
		frequencies = null;
		huffmanCoding = null;
		config = null;
	}
	
	private void writeStats() {
		CodedStreams.writeToFile(new WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				CodedStreams.writePrimitiveArray(writer, frequencies);
			}
		}, context.getCoding(), statsFile());
	}

	private File file() {
		return context.file("trie", false, definition);
	}

	private File statsFile() {
		return context.file("trie", true, definition);
	}
	
	private void writeNode(CodedWriter coded, Node node) {
		if (node != root) huffmanCoding.encodePositiveInt(coded.getWriter(), node.c + 1);
		LinearRecord record = node.record;
		if (uniqueKeys) {
			coded.getWriter().writeBoolean(record != null);
			if (record != null) writeRecord(coded, record);
		} else {
			if (record == null) {
				coded.writePositiveLong(1L);
			} else {
				// first record the size
				{
					LinkedRecord next = (LinkedRecord) record;
					recordSizingWriter.setPosition(0L);
					do {
						writeRecord(recordSizingCoded, next);
						next = next.getNextRecord();
					} while (next != record);
					coded.writePositiveLong(recordSizingWriter.getPosition() + 1L);
				}
				// then actually write the records
				{
					LinkedRecord next = (LinkedRecord) record;
					do {
						writeRecord(coded, next);
						next = next.getNextRecord();
					} while (next != record);
				}
			}
		}
	}
	
	private void writeRecord(CodedWriter coded, LinearRecord record) {
		if (definition.isOrdinal()) coded.writePositiveLong(record.getOrdinal() + 1L);
		if (definition.isPositional()) coded.writePositiveLong(record.getPosition() + 1L);
		record.reset();
		compactor.compact(coded, record);
	}
	
	private class CharFreqRec extends CharFrequencyRecorder {
		
		void record(Node node) {
			if (node != root) record(node.c);
			if (node.sibling != null) record(node.sibling);
			if (node.child != null) record(node.child);
		}
		
	}
	
	private class Offsetter {
		
		private final NullBitWriter writer;
		private final CodedWriter coded;
		
		private long lastOffset = 0L;
		
		public Offsetter() {
			ExtendedCoding coding = context.getCoding();
			writer = new NullBitWriter();
			coded = new CodedWriter(writer, coding);
		}
		
		void offset(Node node) {
			// this is a complicated dance, we do it because we want to conserve memory when building the trie
			// (that means doing as much as possible with as little state as possible)
			
			//first gather some basic state
			final boolean hasChild = node.child != null;
			final boolean hasSibling = node.sibling != null;
			
			// now, find out how far child is from end of file (this is expressed as a negative number)
			if (hasChild) offset(node.child);
			// do the same thing for one's sibling
			if (hasSibling) offset(node.sibling);
			// at this point last offset will measure the distance of the end of the next node from the eof
			// but that's now us! So the difference is +ve offset of the related nodes from us
			// ie. how many bits you need to skip from the end of this node to reach the start of our child/sibling node
			// and since we're the only node pointing to our child/sibling (it's a tree)
			// we can store the +ve number back onto the related nodes
			// if we don't do that now, when we come to write the node for real, we won't be able to recalculate the +ve offset
			// because we won't have the value of lastOffset available to us.
			if (hasChild) node.child.offset = node.child.offset - lastOffset;
			if (hasSibling) node.sibling.offset = node.sibling.offset - lastOffset;
			
			// now measure the bits needed to write this node
			writer.setPosition(0L);
			writeNode(coded, node);
			coded.writePositiveLong(hasChild ? node.child.offset + 2L : 1L);
			coded.writePositiveLong(hasSibling ? node.sibling.offset + 2L : 1L);
			long offset = lastOffset - writer.getPosition();
			
			// record the negative offset on this node and in the lastOffset
			// ready for a node that references us
			lastOffset = node.offset = offset;
		}

	}

	private class Writer {

		private final HuffmanCoding huffmanCoding;
		private final OutputStream out;
		private final BitWriter writer;
		private final CodedWriter coded;
		
		public Writer() {
			this.huffmanCoding = TrieConsumer.this.huffmanCoding;
			try {
				out = new BufferedOutputStream(new FileOutputStream(file()), 1024);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			writer = new OutputStreamBitWriter(out);
			coded = new CodedWriter(writer, context.getCoding());
		}
		
		void write(Node node) {
			final boolean hasChild = node.child != null;
			final boolean hasSibling = node.sibling != null;

			writeNode(coded, node);
			coded.writePositiveLong(hasChild ? node.child.offset + 2L : 1L);
			coded.writePositiveLong(hasSibling ? node.sibling.offset + 2L : 1L);

			if (hasSibling) write(node.sibling);
			if (hasChild) write(node.child);
		}

		void close() {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.getLogger().log(Level.ERROR, "Failed to flush writer", e);
			}
			try {
				out.close();
			} catch (IOException e) {
				context.getLogger().log(Level.WARN, "Failed to close file", e);
			}
		}
		
	}
	
	private static class Node {
		
		final char c;
		
		//TODO may need to support multiple records!
		LinearRecord record;
		
		Node sibling;
	
		Node child;
		
		long offset;
		
		Node(char c) {
			this.c = c;
		}
		
	}
	
	
}
