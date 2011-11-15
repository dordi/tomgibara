package com.tomgibara.crinch.record.sort;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.NullBitWriter;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedStreams.WriteTask;
import com.tomgibara.crinch.coding.CharFrequencyRecorder;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.Coding;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnOrder;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;

public class TrieConsumer extends OrderedConsumer {

	private final int columnIndex;
	private Node root;
	private boolean ordinalValue;
	private long nodeCount = 0;
	private long[] frequencies;
	private HuffmanCoding huffmanCoding;
	
	public TrieConsumer(int columnIndex, boolean ordinal, ColumnOrder... orders) {
		super(ordinal, !ordinal, orders);
		if (columnIndex < 0) throw new IllegalArgumentException("negative column index");
		this.columnIndex = columnIndex;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		super.prepare(context);
		if (!sortedFile().exists()) throw new IllegalStateException("no sorted file: " + sortedFile());
		if (columnIndex >= definition.getTypes().size()) throw new IllegalStateException("not enough columns");
		if (definition.getTypes().get(columnIndex) != ColumnType.STRING_OBJECT) throw new IllegalStateException("column not a string");
		ordinalValue = definition.isOrdinal();
	}

	@Override
	public int getRequiredPasses() {
		return file().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		root = new Node('\0');
	}

	@Override
	public void consume(LinearRecord record) {
		for (int i = 0; i < columnIndex; i++) record.skipNext();
		final String key = record.nextString();
		final long value = ordinalValue ? record.getRecordOrdinal() : record.getRecordPosition();
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
		node.value = value;
		record.exhaust();
	}

	@Override
	public void endPass() {
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
	}

	@Override
	public void quit() {
	}

	private void writeStats() {
		File file = new File(context.getOutputDir(), context.getDataName() + ".col-" + columnIndex + ".trie-stats." + definition.getId());
		CodedStreams.writeToFile(new WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				writer.getWriter().writeBoolean(definition.isOrdinal());
				CodedStreams.writePrimitiveArray(writer, frequencies);
			}
		}, context.getCoding(), file);
	}

	private File file() {
		return new File(context.getOutputDir(), context.getDataName() + ".col-" + columnIndex + ".trie." + definition.getId());
	}
	
	private static class CharFreqRec extends CharFrequencyRecorder {
		
		void record(Node node) {
			record(node.c);
			if (node.sibling != null) record(node.sibling);
			if (node.child != null) record(node.child);
		}
		
	}
	
	private class Offsetter {
		
		private final HuffmanCoding huffmanCoding;
		private final NullBitWriter writer;
		private final CodedWriter coded;
		
		private long lastOffset = 0L;
		
		public Offsetter() {
			this.huffmanCoding = TrieConsumer.this.huffmanCoding;
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
			if (node != root) huffmanCoding.encodePositiveInt(writer, node.c + 1);
			// add 2 to the value because -1 indicates absent value
			coded.writePositiveLong(node.value + 2L);
			//TODO find a better encoding
			coded.getWriter().writeBoolean(hasChild);
			if (hasChild) coded.writePositiveLong(node.child.offset + 1L);
			coded.getWriter().writeBoolean(hasSibling);
			if (hasSibling) coded.writePositiveLong(node.sibling.offset + 1L);
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

			if (node != root) huffmanCoding.encodePositiveInt(writer, node.c + 1);
			coded.writePositiveLong(node.value + 2L);
			coded.getWriter().writeBoolean(hasChild);
			if (hasChild) coded.writePositiveLong(node.child.offset + 1L);
			coded.getWriter().writeBoolean(hasSibling);
			if (hasSibling) coded.writePositiveLong(node.sibling.offset + 1L);

			if (hasSibling) write(node.sibling);
			if (hasChild) write(node.child);
		}

		void close() {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.log("Failed to flush writer", e);
			}
			try {
				out.close();
			} catch (IOException e) {
				context.log("Failed to close file", e);
			}
		}
		
	}
	
	private static class Node {
		
		final char c;
		
		//TODO may need to support multiple refs!
		long value;
		
		Node sibling;
	
		Node child;
		
		long offset;
		
		Node(char c) {
			this.c = c;
			value = -1L;
		}
		
	}
	
	
}
