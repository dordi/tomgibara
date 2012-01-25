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
package com.tomgibara.crinch.record.compact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class CompactConsumer implements RecordConsumer<LinearRecord> {

	private ProcessContext context;
	private RecordCompactor compactor;
	private OutputStream out;
	private OutputStreamBitWriter writer;
	private CodedWriter coded;
	private long bitsWritten;
	
	public CompactConsumer() {
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		if (context.getRecordDef() == null) throw new IllegalArgumentException("context has no record definition");
		if (context.isClean()) file().delete();
	}

	@Override
	public int getRequiredPasses() {
		return file().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		context.setPassName("Compacting data");
		compactor = new RecordCompactor(context, null, 0);
		open();
	}

	@Override
	public void consume(LinearRecord record) {
		bitsWritten += compactor.compact(coded, record);
	}

	@Override
	public void endPass() {
		CompactStats stats = new CompactStats("compact", context);
		stats.bitsWritten = bitsWritten;
		stats.write();
	}

	@Override
	public void complete() {
		close();
	}

	@Override
	public void quit() {
		close();
	}

	private File file() {
		return context.file("compact", false, context.getRecordDef().getBasisOrSelf());
	}
	
	private void open() {
		try {
			out = new BufferedOutputStream(new FileOutputStream(file()), 1024);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer = new OutputStreamBitWriter(out);
		coded = new CodedWriter(writer, context.getCoding());
		bitsWritten = 0L;
	}
	
	private void close() {
		if (writer != null) {
			try {
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (RuntimeException e) {
				context.getLogger().log(Level.ERROR, "Failed to flush writer", e);
			} finally {
				writer = null;
				coded = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				context.getLogger().log(Level.WARN, "Failed to close file", e);
			} finally {
				out = null;
			}
		}
		if (context != null) {
			context = null;
		}
	}
	
}
