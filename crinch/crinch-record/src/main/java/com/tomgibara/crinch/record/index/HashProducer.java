package com.tomgibara.crinch.record.index;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.bits.ByteBasedBitReader;
import com.tomgibara.crinch.bits.FileBitReaderFactory;
import com.tomgibara.crinch.bits.FileBitReaderFactory.Mode;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.Murmur3_32Hash;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.compact.CompactProducer;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.process.ProcessContext;

public class HashProducer implements RecordProducer<LinearRecord> {

	private static ClassConfig sConfig = new ClassConfig(true, false, false);

	// constructor state
	private final SubRecordDef subRecDef;

	// prepared state
	private RecordStats recStats;
	private HashStats hashStats;
	DynamicRecordFactory keyFactory;
	DynamicRecordFactory recFactory;
	private Murmur3_32Hash<LinearRecord>[] hashes;
	private FileBitReaderFactory fbrf;
	
	public HashProducer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalArgumentException("no record stats");
		hashStats = new HashStats(context, subRecDef);
		hashStats.read();
		
		keyFactory = DynamicRecordFactory.getInstance(hashStats.definition);
		recFactory = subRecDef == null ? keyFactory : DynamicRecordFactory.getInstance(hashStats.definition.getBasis());
		
		HashSource<LinearRecord> hashSource = new RecordHashSource(hashStats.definition.getTypes());
		hashes = new Murmur3_32Hash[hashStats.hashSeeds.length];
		for (int i = 0; i < hashes.length; i++) {
			hashes[i] = new Murmur3_32Hash<LinearRecord>(hashSource, hashStats.hashSeeds[i]);
		}
		
		File file = context.file(hashStats.type, false, hashStats.definition);
		fbrf = new FileBitReaderFactory(file, Mode.CHANNEL);
	}

	@Override
	public Accessor open() {
		return new Accessor();
	}


	@Override
	public void complete() {
		fbrf = null;
	}
	
	//TODO minor optimization is possible: look out for repeated requests on the same key
	public class Accessor implements RecordSequence<LinearRecord> {
		
		// move hash stats data locally for better performance
		private final DynamicRecordFactory keyFactory = HashProducer.this.keyFactory;
		private final DynamicRecordFactory recFactory = HashProducer.this.recFactory;
		private final boolean hasSubRecDef = subRecDef != null;
		private final Murmur3_32Hash<LinearRecord>[] hashes = HashProducer.this.hashes;
		private final int recordBits = hashStats.positionBits + hashStats.ordinalBits;
		private final int tableSize = hashStats.tableSize;
		private final boolean hasPosition = hashStats.positional;
		private final boolean hasOrdinal = hashStats.ordinal;
		private final int positionBits = hashStats.positionBits;
		private final int ordinalBits = hashStats.ordinalBits;
		private final int noPosition = (1 << hashStats.positionBits) - 1;
		private final int noOrdinal = (1 << hashStats.ordinalBits) - 1;
		
		private final ByteBasedBitReader reader;

		private CompactProducer.Accessor records = null;
		private PositionProducer.Accessor positions = null;
		private LinearRecord key;
		private int[] hashcodes = new int[hashStats.hashSeeds.length];

		private LinearRecord next = null;
		
		Accessor() {
			reader = fbrf.openReader();
		}
		
		//TODO remove when context can look-up components
		public Accessor setRecords(CompactProducer.Accessor records) {
			this.records = records;
			return this;
		}

		//TODO remove when context can look-up components
		public Accessor setPositions(PositionProducer.Accessor positions) {
			this.positions = positions;
			return this;
		}

		public Accessor setKey(LinearRecord key) {
			if (key != null) {
				key = keyFactory.newRecord(sConfig, key, false);
				key.mark();
				for (int i = 0; i < hashes.length; i++) {
					hashcodes[i] = (hashes[i].hashAsInt(key) & 0x7fffffff) % tableSize;
					key.reset();
				}
			}
			this.key = key;
			next = null;
			return this;
		}
		
		@Override
		public boolean hasNext() {
			if (key == null) return false;
			advance();
			return next != null;
		}

		@Override
		public LinearRecord next() {
			if (next == null) throw new NoSuchElementException();
			LinearRecord tmp = next;
			next = null;
			return tmp;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void close() {
			fbrf.closeReader(reader);
		}
		
		private void advance() {
			if (records == null) throw new IllegalStateException("no records");
			
			if (key != null) {
				for (int i = 0; i < hashcodes.length; i++) {
	
					reader.setPosition(recordBits * hashcodes[i]);
	
					long position;
					long ordinal;

					if (hasPosition) {
						position = reader.read(positionBits);
						if (position == noPosition) position = -1;
					} else {
						position = -1;
					}
					if (hasOrdinal) {
						ordinal = reader.read(ordinalBits);
						if (ordinal == noOrdinal) ordinal = -1;
					} else {
						ordinal = -1;
					}
	
					if (ordinal == -1L && position == -1L) continue;
					
					if (position == -1L) {
						if (positions == null) throw new IllegalStateException("no positions available");
						throw new UnsupportedOperationException("positions doesn't support direct lookup");
						//otherwise would set position based on result from accessor
					}
					
					records.setPosition(position, ordinal);
					if (!records.hasNext()) continue;
					LinearRecord next = recFactory.newRecord(sConfig, records.next());
					next.mark();
					if (keyFactory.newRecord(sConfig, next, !hasSubRecDef).equals(key)) {
						next.reset();
						this.next = next;
						this.key = null;
						return;
					}
				}
				key = null;
			}
			next = null;
		}
		
	}
	
}
