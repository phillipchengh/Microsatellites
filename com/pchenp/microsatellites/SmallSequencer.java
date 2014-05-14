package com.pchenp.microsatellites;

public abstract class SmallSequencer extends Sequencer {

	protected int genomeLen;
	
	protected SmallSequencer(String fileName) {
		super(fileName);
		try {
			long fileLen = raf.length();
			if (fileLen > Constants.MAX_READ_LEN) {
				throw new Exception(fileName + " is too large for SmallSequencer.");
			}
			genomeLen = (int) fileLen;
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

}
