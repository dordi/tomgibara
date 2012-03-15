package com.tomgibara.crinch.coding;

import java.util.Arrays;



public class UnaryCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Arrays.asList(UnaryCoding.oneExtended, UnaryCoding.zeroExtended);
	}

	// put a limit on it, otherwise encodings take too much memory
	@Override
	int getMaxEncodableValue(ExtendedCoding coding) {
		return 100;
	}
	
}