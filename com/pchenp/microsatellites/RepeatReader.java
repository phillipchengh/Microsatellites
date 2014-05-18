package com.pchenp.microsatellites;

import java.util.Map;

public abstract class RepeatReader {

	protected int readBufSize;
	protected int shortReadSize;
	protected int coverage;
	protected int minPat;
	protected int maxPat;
	protected int minRepeats;
	
	protected RepeatReader(int bufSize, int readSize, int coverage, int minPat, int maxPat, int minRepeats) {
		this.readBufSize = bufSize;
		this.shortReadSize = readSize;
		this.coverage = coverage;
		this.minPat = minPat;
		this.maxPat = maxPat;
		this.minRepeats = minRepeats;
	}
	
	abstract protected Map<String, ShortTandemRepeat> readFile(String fileName);
}
