package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;

public class SerialRepeatReader extends RepeatReader {

	public SerialRepeatReader() {
		this(Default.READ_BUF_SIZE, Default.SHORT_READ_LEN, Default.COVERAGE, Default.MIN_PAT_LEN, Default.MAX_PAT_LEN, Default.MIN_REPEATS);
	}
	
	public SerialRepeatReader(int bufSize, int readSize, int coverage, int minPat, int maxPat, int minRepeats) {
		super(bufSize, readSize, coverage, minPat, maxPat, minRepeats);
	}
	
	public HashMap<String, ShortTandemRepeat> readFile(String fileName) {
		HashMap<String, ShortTandemRepeat> repeatMap = new HashMap<String, ShortTandemRepeat>();
		File stripped = stripFile(fileName);
		InputStream is = null;
		BufferedInputStream bis = null;
		RandomAccessFile raf = null; 
		try {
			is = new FileInputStream(stripped);
			bis = new BufferedInputStream(is);
			raf = new RandomAccessFile(stripped, "r");
			long seqLen = raf.length();
			long numReads = (seqLen/shortReadSize)*coverage;
			LinkedList<Byte> history = new LinkedList<Byte>();
			for (int i = 0; i < maxPat*2; i++) {
				history.add((byte) 0);
			}
			System.out.println("Conducting coverage " + coverage + "x " + "(Number of Reads: " + numReads + ")" + " short reads on \"" + fileName + "\"...");
			for (long j = 0; j < numReads; j++) {
				mapShortRead(repeatMap, raf, seqLen, history);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			stripped.deleteOnExit();
		}
		return repeatMap;
	}
	
	private void mapShortRead(HashMap<String, ShortTandemRepeat> repeatMap, RandomAccessFile raf, long seqLen, LinkedList<Byte> repeatHistory) {
		int i;
		long seqPos = (long) (Math.random()*seqLen);
		int maxRead;
		byte[] repeat = null;
		int repeatIdx = 0;
		int numRepeat = 0;
		if ((seqPos + shortReadSize) < seqLen) {
			maxRead = shortReadSize;
		} else {
			maxRead = (int) (seqLen - seqPos);
		}
		byte[] buf = new byte[maxRead];
		try {
			raf.seek(seqPos);
			raf.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (i = 0; i < maxRead; i++) {
			repeatHistory.remove();
			repeatHistory.add(buf[i]);
			if (repeat == null) {
				if ((repeat = Utilities.findRepeat(repeatHistory, minPat, maxPat)) != null) {
					repeatIdx = 0;
					numRepeat = 2;
				}
			} else {
				if (repeat[repeatIdx] != buf[i]) {
					insertRepeat(repeatMap, repeat, numRepeat);
					repeat = null;
				} else {
					repeatIdx++;
					if (repeatIdx > repeat.length-1) {
						repeatIdx = 0;
						numRepeat++;	
					}
				}
			}
		}
		if (repeat != null) {
			insertRepeat(repeatMap, repeat, numRepeat);
		}
	}
	
	private File stripFile(String fileName) {
		File temp = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream os = null;
		BufferedOutputStream bos = null;
		try {
			is = this.getClass().getResourceAsStream(fileName);
			bis = new BufferedInputStream(is);
			temp = File.createTempFile("awildtemporaryfilehasappeared", ".temp");
			os = new FileOutputStream(temp);
			bos = new BufferedOutputStream(os);
			System.out.println("Stripping out \"" + fileName + "\"...");
			byte[] buf = new byte[readBufSize];
			int numBytes;
			while (bis.available() > 0) {
				numBytes = bis.read(buf);
				writeStrippedBuf(bos, buf, numBytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return temp;
	}
	
	private int writeStrippedBuf(BufferedOutputStream bos, byte[] inBuf, int inBufSize) {
		int i;
		byte[] outBuf = new byte[inBufSize];
		int j = 0;
		for (i = 0; i < inBufSize; i++) {
			if (Utilities.validBase(inBuf[i])) {
				outBuf[j] = inBuf[i];
				j++;
			}
		}
		try {
			bos.write(outBuf, 0, j);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return j;
	}
	
	private void insertRepeat(HashMap<String, ShortTandemRepeat> repeatMap, byte[] repeat, int numRepeat) {
		if (numRepeat < minRepeats) {
			return;
		}
		ShortTandemRepeat str;
		String keyCurRepeat = new String(repeat);
		if ((str = repeatMap.get(keyCurRepeat)) == null) {
			str = new ShortTandemRepeat(repeat, numRepeat);
			repeatMap.put(keyCurRepeat, str);
		} else {
			str = repeatMap.get(keyCurRepeat);
			str.setRepeats(str.getRepeats()+numRepeat);
			repeatMap.put(keyCurRepeat, str);
		}
	}
}
