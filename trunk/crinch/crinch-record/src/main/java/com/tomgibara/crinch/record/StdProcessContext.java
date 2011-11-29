package com.tomgibara.crinch.record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreamException;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.InputStreamBitReader;
import com.tomgibara.crinch.bits.OutputStreamBitWriter;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.EliasOmegaCoding;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDefinition;

public class StdProcessContext implements ProcessContext {

	private float progressStep = 1.0f;
	private ExtendedCoding coding = EliasOmegaCoding.extended;
	private boolean clean = false;
	private ColumnParser columnParser = new StdColumnParser();
	private File outputDir = new File("");
	private String dataName = "default";
	
	private long recordCount = -1L;
	private long recordsTransferred = -1L;
	private float progress;
	private float lastProgress;
	private String passName;
	private RecordStats recordStats;
	private List<ColumnType> columnTypes;
	private List<ColumnOrder> columnOrders;
	private RecordDefinition recordDef;

	public StdProcessContext() {
		load();
		resetProgress();
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
	public void setOutputDir(File outputDir) {
		if (outputDir == null) throw new IllegalArgumentException("null outputDir");
		if (!outputDir.equals(this.outputDir)) {
			log("Output directory: " + outputDir);
			this.outputDir = outputDir;
			load();
		}
	}
	
	@Override
	public File getOutputDir() {
		return outputDir;
	}
	
	@Override
	public void setDataName(String dataName) {
		if (dataName == null) throw new IllegalArgumentException("null dataName");
		if (dataName.isEmpty()) throw new IllegalArgumentException("empty dataName");
		if (!dataName.equals(this.dataName)) {
			log("Data name: " + dataName);
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
		if (recordCount == -1L) return;
		if (recordsTransferred < 0L) recordsTransferred = -1L;
		if (recordsTransferred == -1L) {
			resetProgress();
		} else {
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
			log("Pass: " + passName);
		}
		this.passName = passName;
	}
	
	@Override
	public String getPassName() {
		return passName;
	}
	
	public void setRecordCount(long recordCount) {
		if (recordCount < 0L) recordCount = -1L;
		if (recordStats != null && recordCount != recordStats.getRecordCount()) { 
			setRecordStats(null);
		}
		if (recordCount != this.recordCount) {
			this.recordCount = recordCount;
			recordsTransferred = Math.min(recordsTransferred, recordCount);
			log("Record count: " + recordCount);
			if (recordCount <= 0L) resetProgress();
		}
	}
	
	
	@Override
	public long getRecordCount() {
		return recordCount;
	}
	
	@Override
	public void setRecordStats(RecordStats recordStats) {
		RecordStats oldStats = this.recordStats;
		this.recordStats = recordStats;
		if (recordStats != null && !recordStats.equals(oldStats)) {
			int col = 1;
			for (ColumnStats stats : recordStats.getColumnStats()) {
				log("Statistics - column " + col++ + ": " + stats);
			}
		}
		setRecordCount(recordStats == null ? -1L : recordStats.getRecordCount());
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
				log("Type - column " + col++ + ": " + type);
			}
		}
		this.columnTypes = columnTypes;
		recordDef = null;
		//TODO should only write if changed
		writeColumnTypes();
	}

	@Override
	public List<ColumnType> getColumnTypes() {
		return columnTypes;
	}

	@Override
	public RecordDefinition getRecordDef() {
		if (recordDef == null) {
			if (columnTypes != null) {
				RecordDefinition def;
				def = RecordDefinition.fromTypes(columnTypes).build();
				if (columnOrders != null) {
					def = def.withOrdering(columnOrders);
				}
				recordDef = def;
			}
		}
		return recordDef;
	}
	
	@Override
	public void setColumnOrders(List<ColumnOrder> columnOrders) {
		if (columnOrders != null && !columnOrders.equals(this.columnOrders)) {
			int col = 1;
			for (ColumnOrder order : columnOrders) {
				log("Order - column " + col++ + ": " + (order.isAscending() ? "ascending" : "descending") + " " + (order.isNullFirst() ? "(null first)" : "(null last)"));
			}
		}
		this.columnOrders = columnOrders;
		recordDef = null;
	}
	
	@Override
	public List<ColumnOrder> getColumnOrders() {
		return columnOrders;
	}
	
	@Override
	public void log(String message) {
		System.out.println(message);
	}
	
	@Override
	public void log(String message, Throwable t) {
		System.err.println(message + "(records transferred: " + recordsTransferred + ")");
		t.printStackTrace();
	}

	@Override
	public File file(String type, boolean stats, RecordDefinition def) {
		StringBuilder sb = new StringBuilder(dataName);
		if (type != null) {
			sb.append('.').append(type);
			if (stats) sb.append("-stats");
		} else {
			sb.append(".stats");
		}
		if (def != null) sb.append('.').append(def.getId());
		return new File(outputDir, sb.toString());
	}
	
	private void resetProgress() {
		progress = 0f;
		lastProgress = -1f;
	}
	
	private void reportProgress(float progress) {
		if (progressStep >= 1.0f) return;
		this.progress = progress;
		lastProgress = progress;
		log("Progress: " + Math.round(progress * 100f) + "%");
	}
	
	private void load() {
		readRecordStats();
		readColumnTypes();
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
		columnTypes = read(new ReadOp<List<ColumnType>>() {
			@Override
			public List<ColumnType> read(CodedReader coded) {
				return CodedStreams.readEnumList(coded, ColumnType.class);
			}
		}, getColumnTypesFile());
		recordDef = null;
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
						log("problem closing file", e);
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
				writer.padToBoundary(BitBoundary.BYTE);
				writer.flush();
			} catch (IOException e) {
				throw new BitStreamException(e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						log("problem closing file", e);
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
