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
package com.tomgibara.crinch.record.process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreamException;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.FileBitReaderFactory;
import com.tomgibara.crinch.bits.InputStreamBitReader;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.bits.FileBitReaderFactory.Mode;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.EliasOmegaCoding;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.ColumnParser;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.StdColumnParser;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.fact.Asserter;
import com.tomgibara.crinch.record.fact.AssertionType;
import com.tomgibara.crinch.record.fact.FactDomain;
import com.tomgibara.crinch.record.fact.Facts;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class StdProcessContext implements ProcessContext {

	private static final ProcessLogger sDefaultLogger = new PrintStreamLogger();
	
	private ProcessLogger logger = sDefaultLogger;
	
	private float progressStep = 1.0f;
	private ExtendedCoding coding = EliasOmegaCoding.extended;
	private boolean inMemory;
	private boolean clean = false;
	private ColumnParser columnParser = new StdColumnParser();
	private File dataDir = new File("");
	private String dataName = "default";
	
	private long recordsTransferred = -1L;
	private float progress;
	private float lastProgress;
	private String passName;
	private RecordStats recordStats;
	private List<ColumnType> columnTypes;
	private RecordDef recordDef;
	private Set<Asserter<?>> asserters = Collections.emptySet();
	private Facts facts;

	public StdProcessContext() {
		load();
		resetProgress();
	}

	public void setLogger(ProcessLogger logger) {
		if (logger == null) throw new IllegalArgumentException("null logger");
		this.logger = logger;
	}
	
	@Override
	public ProcessLogger getLogger() {
		return logger;
	}
	
	public void setProgressStep(float progressStep) {
		if (progressStep < 0f) throw new IllegalArgumentException("negative progress");
		this.progressStep = progressStep;
	}

	@Override
	public void setCoding(ExtendedCoding coding) {
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.coding = coding;
	}

	@Override
	public ExtendedCoding getCoding() {
		return coding;
	}
	
	@Override
	public void setInMemory(boolean inMemory) {
		this.inMemory = inMemory;
	}
	
	@Override
	public boolean isInMemory() {
		return inMemory;
	}
	
	@Override
	public void setClean(boolean clean) {
		this.clean = clean;
	}
	
	@Override
	public boolean isClean() {
		return clean;
	}
	
	@Override
	public void setColumnParser(ColumnParser columnParser) {
		if (columnParser == null) throw new IllegalArgumentException("null columnParser");
		this.columnParser = columnParser;
	}
	
	@Override
	public ColumnParser getColumnParser() {
		return columnParser;
	}
	
	@Override
	public void setDataDir(File dataDir) {
		if (dataDir == null) throw new IllegalArgumentException("null dataDir");
		if (!dataDir.equals(this.dataDir)) {
			logger.log("Data directory: " + dataDir);
			this.dataDir = dataDir;
			load();
		}
	}
	
	@Override
	public File getDataDir() {
		return dataDir;
	}
	
	@Override
	public void setDataName(String dataName) {
		if (dataName == null) throw new IllegalArgumentException("null dataName");
		if (dataName.isEmpty()) throw new IllegalArgumentException("empty dataName");
		if (!dataName.equals(this.dataName)) {
			logger.log("Data name: " + dataName);
			this.dataName = dataName;
			load();
		}
	}
	
	@Override
	public String getDataName() {
		return dataName;
	}
	
	@Override
	public void setRecordsTransferred(long recordsTransferred) {
		if (recordStats == null) return;
		if (recordsTransferred < 0L) recordsTransferred = -1L;
		if (recordsTransferred == -1L) {
			resetProgress();
		} else {
			long recordCount = recordStats.getRecordCount();
			float progress;
			if (recordsTransferred >= recordCount) {
				progress = 1f;
			} else if (recordsTransferred <= 0) {
				progress = 0f;
			} else {
				progress = (float)(recordsTransferred / (double) recordCount);
			}
			if (progress < this.progress || progress > lastProgress + progressStep || progress > lastProgress && progress == 1f) {
				reportProgress(progress);
			}
		}
		this.recordsTransferred = recordsTransferred;
	}
	
	@Override
	public void setPassName(String passName) {
		if (passName != null && !passName.equals(this.passName)) {
			logger.log("Pass: " + passName);
		}
		this.passName = passName;
	}
	
	@Override
	public String getPassName() {
		return passName;
	}
	
	public void setRecordCount(long recordCount) {
		if (recordCount < 0L) {
			setRecordStats(null);
			return;
		}
		
		if (recordStats == null || recordCount != recordStats.getRecordCount()) {
			setRecordStats(new RecordStats(recordCount));
			recordsTransferred = Math.min(recordsTransferred, recordCount);
			if (recordCount < 0L) {
				logger.log("Record count unknown");
			} else {
				logger.log("Record count: " + recordCount);
			}
			if (recordCount <= 0L) resetProgress();
		}
	}
	
	@Override
	public long getRecordCount() {
		return recordStats == null ? -1 : recordStats.getRecordCount();
	}
	
	@Override
	public void setRecordStats(RecordStats recordStats) {
		RecordStats oldStats = this.recordStats;
		this.recordStats = recordStats;
		if (recordStats != null && !recordStats.equals(oldStats)) {
			int col = 1;
			for (ColumnStats stats : recordStats.getColumnStats()) {
				logger.log("Statistics - column " + col++ + ": " + stats);
			}
		}
		//TODO should only write if changed
		writeRecordStats();
	}
	
	@Override
	public RecordStats getRecordStats() {
		return recordStats;
	}

	@Override
	public void setColumnTypes(List<ColumnType> columnTypes) {
		if (columnTypes != null && !columnTypes.equals(this.columnTypes)) {
			int col = 1;
			for (ColumnType type : columnTypes) {
				logger.log("Type - column " + col++ + ": " + type);
			}
		}
		this.columnTypes = columnTypes;
		
		setRecordDef(columnTypes == null ? null : RecordDef.fromTypes(columnTypes).build());
		//TODO should only write if changed
		writeColumnTypes();
	}

	@Override
	public List<ColumnType> getColumnTypes() {
		return columnTypes;
	}

	@Override
	public RecordDef getRecordDef() {
		return recordDef;
	}
	
	@Override
	public File file(String type, boolean stats, RecordDef def) {
		StringBuilder sb = new StringBuilder(dataName);
		if (type != null) {
			sb.append('.').append(type);
			if (stats) sb.append("-stats");
		} else {
			sb.append(".stats");
		}
		if (def != null) sb.append('.').append(def.getId());
		return new File(dataDir, sb.toString());
	}
	
	@Override
	public Set<Asserter<?>> getAsserters() {
		return asserters;
	}
	
	@Override
	public void setAsserters(Set<Asserter<?>> asserters) {
		if (asserters == null) throw new IllegalArgumentException("null asserters");
		if (asserters.equals(this.asserters)) return;
		this.asserters = Collections.unmodifiableSet(new HashSet<Asserter<?>>(asserters));
		resetFacts();
	}
	
	@Override
	public Facts getFacts() {
		return facts;
	}

	@Override
	public void persistFacts() {
		writeFacts();
	}
	
	private void setRecordDef(RecordDef recordDef) {
		this.recordDef = recordDef;
		resetFacts();
	}
	
	private void resetProgress() {
		progress = 0f;
		lastProgress = -1f;
	}
	
	private void reportProgress(float progress) {
		if (progressStep >= 1.0f) return;
		this.progress = progress;
		lastProgress = progress;
		logger.log("Progress: " + Math.round(progress * 100f) + "%");
	}
	
	private void load() {
		readRecordStats();
		readColumnTypes();
		readFacts();
	}
	
	private File getRecordStatsFile() {
		return file(null, true, null);
	}
	
	private void writeRecordStats() {
		write(new WriteOp() {
			@Override
			public boolean isNull() { return recordStats == null; }
			@Override
			public void write(CodedWriter coded) { RecordStats.write(coded, recordStats); }
		}, getRecordStatsFile());
	}
	
	private void readRecordStats() {
		recordStats = read(new ReadOp<RecordStats>() {
			@Override
			public RecordStats read(CodedReader coded) {
				return RecordStats.read(coded);
			}
		}, getRecordStatsFile());
		if (recordStats != null) setRecordCount(recordStats.getRecordCount());
	}

	private File getColumnTypesFile() {
		return file("types", false, null);
	}
	
	private void writeColumnTypes() {
		write(new WriteOp() {
			@Override
			public boolean isNull() { return columnTypes == null; }
			@Override
			public void write(CodedWriter coded) { CodedStreams.writeEnumList(coded, columnTypes); }
		}, getColumnTypesFile());
	}

	private void readColumnTypes() {
		setColumnTypes(
			read(new ReadOp<List<ColumnType>>() {
				@Override
				public List<ColumnType> read(CodedReader coded) {
					return CodedStreams.readEnumList(coded, ColumnType.class);
				}
			}, getColumnTypesFile()));
	}
	
	private File factsFile() {
		return new File(dataDir, dataName + ".facts");
	}

	private FactDomain factDomain() {
		if (recordDef == null) return null;
		Set<AssertionType<?>> types = new HashSet<AssertionType<?>>();
		for (Asserter<?> asserter : this.asserters) {
			types.add(asserter.getAssertionType());
		}
		return new FactDomain(recordDef, types);
	}
	
	private void resetFacts() {
		FactDomain domain = factDomain();
		facts = domain == null ? null : new Facts(domain);
		writeFacts();
	}
	
	private void writeFacts() {
		write(new WriteOp() {
			@Override
			public boolean isNull() {
				return facts == null || facts.isEmpty();
			}
			@Override
			public void write(CodedWriter coded) {
				facts.write(coded);
			}
		}, factsFile());
	}
	
	private void readFacts() {
		final FactDomain domain = factDomain();
		if (domain == null) {
			facts = null;
			return;
		}
		facts = read(new ReadOp<Facts>() {
			@Override
			public Facts read(CodedReader coded) {
				return Facts.read(domain, coded);
			}
		}, factsFile());
		if (facts == null) facts = new Facts(domain);
	}
	
	private <T> T read(ReadOp<T> op, File file) {
		if (!file.isFile()) {
			return null;
		} else if (clean) {
			file.delete();
			return null;
		} else {
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(file), 1024);
				BitReader reader = new InputStreamBitReader(in);
				CodedReader coded = new CodedReader(reader, coding);
				return op.read(coded);
			} catch (IOException e) {
				throw new BitStreamException(e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.log(Level.WARN, "problem closing file", e);
					}
				}
			}
		}
	}
	
	private void write(WriteOp op, File file) {
		if (op.isNull()) {
			file.delete();
		} else {
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(file), 1024);
				BitWriter writer = new OutputStreamBitWriter(out);
				CodedWriter coded = new CodedWriter(writer, coding);
				op.write(coded);
				writer.flush();
			} catch (IOException e) {
				throw new BitStreamException(e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						logger.log(Level.WARN, "problem closing file", e);
					}
				}
			}
		}
	}
	
	private interface ReadOp<T> {
		
		T read(CodedReader coded);
		
	}
	
	private interface WriteOp {
		
		boolean isNull();
		
		void write(CodedWriter coded);
		
	}
	
}
