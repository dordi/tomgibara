package com.tomgibara.crinch.coding;

import java.util.Arrays;

public class TruncatedBinaryCodingTest extends CodingTest<TruncatedBinaryCoding> {

	@Override
	Iterable<TruncatedBinaryCoding> getCodings() {
		return Arrays.asList(new TruncatedBinaryCoding(1), new TruncatedBinaryCoding(100), new TruncatedBinaryCoding(256)) ;
	}
	
	@Override
	int getMaxEncodableValue(TruncatedBinaryCoding coding) {
		return coding.getSize().intValue() - 1;
	}
	
}
