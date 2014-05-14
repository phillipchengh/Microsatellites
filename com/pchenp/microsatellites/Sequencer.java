package com.pchenp.microsatellites;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public abstract class Sequencer implements Iterator<byte[]> {

	protected List<Integer> genomeMap;
	protected File file;
	protected RandomAccessFile raf;
	protected long genomeLen;
	
	protected Sequencer(String fileName) {
		URL url = this.getClass().getResource(fileName);
		file = null;
		raf = null;
		try {
			file = new File(url.toURI());
			raf = new RandomAccessFile(file, "r");
			System.out.println("Opened \"" + fileName + "\"");
			genomeLen = raf.length();
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}
	
	public void close() {
		try {
			if (raf != null) {
				raf.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
