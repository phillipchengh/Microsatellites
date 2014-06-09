package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class SerialRepeatReader extends RepeatReader {

	private long ioTime = -1;
	private long compTime = -1;
	private long totalTime = -1;
	private long tempTime;
	private int lost = 0;
	private int found = 0;
	private int disqualified = 0;

	private static class RBuilder2 extends RBuilder<RBuilder2> {

		public SerialRepeatReader build() {
			return new SerialRepeatReader(this);
		}	
		
		@Override
		protected RBuilder2 self() {
			return this;
		}
	}
	
	public static RBuilder<?> builder() {
		return new RBuilder2();
	}

	public SerialRepeatReader(RBuilder<?> sb) {
		super(sb);
	}
	
	protected void printStats() {
		System.out.println("\n#####Reader Stats#####");
		System.out.println("Total time:" + totalTime/Math.pow(10, 3));
		System.out.println("IO time: " + ioTime/Math.pow(10, 3));
		System.out.println("Computation time:" + compTime/Math.pow(10, 3));
		System.out.println("Disqualified repeats: " + disqualified);
		System.out.println("Identified repeats: " + found);
		System.out.println("Unidentified repeats: " + lost);
		System.out.println("Ratio: " + (double) found/(found + lost));
	}
	
	public HashMap<ShortTandemRepeat, ShortTandemRepeat> readFile(String fileName) {
		ioTime = -1;
		compTime = -1;
		totalTime = -1;
		lost = 0;
		found = 0;
		disqualified = 0;
		totalTime = System.currentTimeMillis();
		HashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap = new HashMap<ShortTandemRepeat, ShortTandemRepeat>();
		File stripped = stripFile(fileName);
		InputStream is = null;
		BufferedInputStream bis = null;
		RandomAccessFile raf = null; 
		try {
			is = new FileInputStream(stripped);
			bis = new BufferedInputStream(is);
			raf = new RandomAccessFile(stripped, "r");
			long seqLen = raf.length();
			long numReads = (seqLen/readSize)*coverage;
			System.out.println("Conducting coverage " + coverage + "x " + "(Number of Reads: " + numReads + ")" + " short reads on \"" + fileName + "\"...");
			for (long j = 0; j < numReads; j++) {
				mapShortRead(repeatMap, raf, seqLen);
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
		coverCounts(repeatMap);
		totalTime = System.currentTimeMillis() - totalTime;
		if (stats) {
			printStats();	
		}
		return repeatMap;
	}
	
	private void mapShortRead(HashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap, RandomAccessFile raf, long seqLen) {
		int i;
		long seqPos = (long) (Math.random()*seqLen);
		int maxRead;
		byte[] repeat = null;
		int repeatIdx = 0;
		int numRepeat = 0;
		int repeatPos = 0;
		LinkedList<Byte> repeatHistory = new LinkedList<Byte>();
		for (i = 0; i < maxPat*2; i++) {
			repeatHistory.add((byte) 0);
		}
		if ((seqPos + readSize) < seqLen) {
			maxRead = readSize;
		} else {
			maxRead = (int) (seqLen - seqPos);
		}
		byte[] buf = new byte[maxRead];
		if (ioTime == -1) {
			ioTime = 0;
		}
		if (compTime == -1) {
			compTime = 0;
		}
		tempTime = System.currentTimeMillis();
		try {
			raf.seek(seqPos);
			raf.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tempTime = System.currentTimeMillis() - tempTime;
		ioTime += tempTime;
		tempTime = System.currentTimeMillis();
		for (i = 0; i < maxRead; i++) {
			repeatHistory.remove();
			repeatHistory.add(buf[i]);
			if (repeat == null) {
				if ((repeat = Utilities.findRepeat(repeatHistory, minPat, maxPat)) != null) {
					repeatIdx = 0;
					numRepeat = 2;
					repeatPos = i - 2*repeat.length + 1;
				}
			} else {
				if (repeat[repeatIdx] != buf[i]) {
					insertRepeat(repeatMap, repeat, numRepeat, buf, repeatPos);
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
			insertRepeat(repeatMap, repeat, numRepeat, buf, repeatPos);
		}
		tempTime = System.currentTimeMillis() - tempTime;
		compTime += tempTime;
	}
	
	private void insertRepeat(HashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap, byte[] repeat, int numRepeat, byte[] buf, int repeatPos) {
		if (numRepeat < minRepeats) {
			if (repeatPos == 0) {
			} else if (repeatPos > (buf.length-(repeat.length*minRepeats))) {
				int afterPos = repeatPos + repeat.length*numRepeat;
				int repeatIdx = 0;
				for (int i = afterPos; i < buf.length; i++, repeatIdx++) {
					if (buf[i] != repeat[repeatIdx]) {
						disqualified++;
						return;
					}
				}
			} else {
				disqualified++;
				return;	
			}
		}
		ShortTandemRepeat refKey = bestMatch(repeat, numRepeat, buf, repeatPos);
		if (refKey == null) {
			Set<ShortTandemRepeat> keys = repeatMap.keySet();
			Iterator<ShortTandemRepeat> iter = keys.iterator();
			while (iter.hasNext()) {
				ShortTandemRepeat s = iter.next();
				byte[] keyRepeat = s.getSequence();
				if (s.isLost()) {
					if (Arrays.equals(keyRepeat, repeat)) {
						refKey = s;
						break;
					}
				}
			}
			if (refKey == null) {
				refKey = new ShortTandemRepeat(repeat, numRepeat);	
			}
			lost++;
		} else {
			found++;
		}
		ShortTandemRepeat str = repeatMap.get(refKey);
		if (str == null) {
			str = new ShortTandemRepeat(refKey);
			str.setRepeats(numRepeat);
			repeatMap.put(refKey, str);
		} else {
			str.setRepeats(str.getRepeats()+numRepeat);
		}
	}
}
