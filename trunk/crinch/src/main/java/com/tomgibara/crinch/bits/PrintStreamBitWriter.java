/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

import java.io.PrintStream;


public class PrintStreamBitWriter extends AbstractBitWriter {

	private final PrintStream stream;
	
	public PrintStreamBitWriter() {
		stream = System.out;
	}
	
	public PrintStreamBitWriter(PrintStream stream) {
		if (stream == null) throw new IllegalArgumentException("null stream");
		this.stream = stream;
	}
	
    @Override
    public int writeBit(int bit) {
        String s = (bit & 1) == 1 ? "1" : "0";
        stream.print(s);
        return 1;
    }
    
}
