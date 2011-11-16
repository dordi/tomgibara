package com.tomgibara.crinch.record.sort;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.PriorityQueue;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.compact.RecordCompactor;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;

public class SortConsumer extends OrderedConsumer {

	private PriorityQueue<LinearRecord> queue; 
	private OutputStream out;
	private BitWriter writer;
	private CodedWriter coded;
	
	public SortConsumer() {
		super(false, false);
	}
	
	@Override
	public int getRequiredPasses() {
		return sortedFile().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		//TODO splitting
		//TODO need to size
		queue = new PriorityQueue<LinearRecord>();
		factory = DynamicRecordFactory.getInstance(definition);
	}

	@Override
	public void consume(LinearRecord record) {
		LinearRecord r = factory.newRecord(record);
		queue.add(r);
	}

	@Override
	//TODO merging
	public void endPass() {
		open();
		try {
			RecordCompactor compactor = new RecordCompactor(context);
			while (!queue.isEmpty()) compactor.compact(coded, queue.poll());
		} finally {
			queue = null;
			factory = null;
			close();
		}
	}

	@Override
	public void complete() {
		cleanup();
	}

	@Override
	public void quit() {
		cleanup();
	}

	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(sortedFile()), 1024);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer = new OutputStreamBitWriter(out);
		coded = new CodedWriter(writer, context.getCoding());
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.log("Failed to flush writer", e);
			} finally {
				writer = null;
				coded = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				context.log("Failed to close file", e);
			} finally {
				out = null;
			}
		}
	}

	private void cleanup() {
		if (context != null) {
			context = null;
		}
	}
	
}
