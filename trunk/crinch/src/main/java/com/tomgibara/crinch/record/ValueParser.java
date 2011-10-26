package com.tomgibara.crinch.record;

public interface ValueParser {

	String parseString(String str);
	
	char parseChar(String str);

	boolean parseBoolean(String str);

	byte parseByte(String str);

	short parseShort(String str);

	int parseInt(String str);

	long parseLong(String str);

	float parseFloat(String str);

	double parseDouble(String str);

}
