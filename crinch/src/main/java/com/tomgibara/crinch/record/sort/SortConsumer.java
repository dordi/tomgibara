package com.tomgibara.crinch.record.sort;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.PriorityQueue;

import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.compact.RecordCompactor;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.Definition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.Order;

public class SortConsumer implements RecordConsumer<LinearRecord> {

	private final Order[] orders;
	
	private ProcessContext context;
	private DynamicRecordFactory factory;
	private PriorityQueue<LinearRecord> queue; 
	private OutputStream out;
	private OutputStreamBitWriter writer;
	private CodedWriter coded;
	
	public SortConsumer(Order... orders) {
		this.orders = orders;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		Definition def = new Definition(false, false, types, orders);
		factory = DynamicRecordFactory.getInstance(def);
	}

	@Override
	public int getRequiredPasses() {
		return file().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		//TODO splitting
		//TODO need to size
		queue = new PriorityQueue<LinearRecord>();
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

	private File file() {
		//TODO append definition name?
		return new File(context.getOutputDir(), context.getDataName() + "." + factory.getName());
	}
	
	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(file()), 1024);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer = new OutputStreamBitWriter(out);
		coded = new CodedWriter(writer, context.getCoding());
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToByteBoundary();
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
		factory = null;
		if (context != null) {
			context = null;
		}
	}
	
}
