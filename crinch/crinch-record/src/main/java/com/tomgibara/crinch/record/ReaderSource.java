package com.tomgibara.crinch.record;

import java.io.IOException;
import java.io.Reader;

public interface ReaderSource {

	String getName();
	
	Reader open() throws IOException;
	
}
