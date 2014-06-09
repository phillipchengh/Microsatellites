package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class SerialRepeatMapper extends RepeatMapper {

	private long totalTime = -1;
	private int disqualified = 0;
	
	private static class MBuilder2 extends MBuilder<MBuilder2> {

		public SerialRepeatMapper build() {
			return new SerialRepeatMapper(this);
		}
		
		@Override
		protected MBuilder2 self() {
			return this;
		}
	}
	
	public static MBuilder<?> builder() {
		return new MBuilder2();
	}

	public SerialRepeatMapper(MBuilder<?> sb) {
		super(sb);
	}
	
	protected HashMap<String, LinkedList<ShortTandemRepeat>> mapFile(String fileName) {
		totalTime = System.currentTimeMillis();
		HashMap<String, LinkedList<ShortTandemRepeat>> repeatMap = new HashMap<String, LinkedList<ShortTandemRepeat>>();
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = this.getClass().getResourceAsStream(fileName);
			bis = new BufferedInputStream(is);
			System.out.println("Mapping reference \"" + fileName + "\"...");
			byte[] buf = new byte[bufSize];
			int numBytes = bis.read(buf);
			MapParam next = mapBuf(repeatMap, buf, numBytes, null);
			while (bis.available() > 0) {
				numBytes = bis.read(buf);
				next = mapBuf(repeatMap, buf, numBytes, next);
			}
			if (next.repeat != null) {
				insertRepeat(repeatMap, next.chr, next.repeat, next.numRepeat, next.repeatPos, next.before);
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
		totalTime = System.currentTimeMillis() - totalTime;
		if (stats) {
			printStats();	
		}
		return repeatMap;
	}
	
	private void printStats() {
		System.out.println("\n#####Mapper Stats#####");
		System.out.println("Total time:" + totalTime/Math.pow(10, 3));
		System.out.println("Disqualified: " + disqualified);
	}

	public MapParam mapBuf(HashMap<String, LinkedList<ShortTandemRepeat>> repeatMap, byte[] buf, int numBytes, MapParam params) {
		int i, j;
		int bufSize = numBytes;
		long seqPos = 0;
		boolean gtFlag;
		boolean crFlag;
		boolean lfFlag;
		LinkedList<Byte> chrHistory;
		LinkedList<Byte> repeatHistory;
		ArrayList<Byte> before;
		LinkedList<ShortTandemRepeat> afterHistory;
		byte[] chr;
		byte[] repeat;
		int repeatIdx;
		long repeatPos;
		int numRepeat;
		if (params == null) {
			seqPos = 0;
			gtFlag = false;
			crFlag = false;
			lfFlag = true;
			chrHistory = null;
			repeatHistory = new LinkedList<Byte>();
			int historySize = readSize; 
			for (i = 0; i < historySize; i++) {
				repeatHistory.add((byte) 0);
			}
			before = null;
			afterHistory = new LinkedList<ShortTandemRepeat>();
			chr = null;
			repeat = null;
			repeatIdx = 0;
			repeatPos = 0;
			numRepeat = 0;
		} else {
			seqPos = params.seqPos;
			gtFlag = params.gtFlag;
			crFlag = params.crFlag;
			lfFlag = params.lfFlag;
			repeatHistory = params.repeatHistory;
			chrHistory = params.chrHistory;
			before = params.before;
			afterHistory = params.afterHistory;
			chr = params.chr;
			repeat = params.repeat;
			repeatIdx = params.repeatIdx;
			repeatPos = params.repeatPos;
			numRepeat = params.numRepeat;
		}
		for (i = 0; i < bufSize; i++) {
			if (gtFlag) {
				if (buf[i] != 13 && buf[i] != 10) {
					chrHistory.add(buf[i]);
					continue;
				}
			}
			if (buf[i] == 13 || buf[i] == 10) {
				if (lfFlag) {
					throw new RuntimeException("Multiple new line error.");
				}
				if (gtFlag) {
					int chrLen = chrHistory.size();
					chr = new byte[chrLen];
					for (j = 0; j < chrLen; j++) {
						chr[j] = chrHistory.get(j);
					}
					chrHistory = null;
					gtFlag = false;
				}
			}
			if (buf[i] == 13) {
				if (crFlag) {
					throw new RuntimeException("Multiple carriage return error.");
				}
				crFlag = true;
				continue;
			} else if (buf[i] == 10) {
				lfFlag = true;
				continue;
			}
			crFlag = false;
			lfFlag = false;
			if (buf[i] == 62) {
				gtFlag = true;
				chrHistory = new LinkedList<Byte>();
				continue;
			}
			if (!Utilities.validBase(buf[i])) {
				throw new RuntimeException("Invalid Char: " + (char) buf[i]);
			}
			repeatHistory.remove();
			repeatHistory.add(buf[i]);
			Iterator<ShortTandemRepeat> iter = afterHistory.iterator();
			while (iter.hasNext()) {
				ShortTandemRepeat str = iter.next();
				if (!str.appendAfter(buf[i])) {
					iter.remove();
				}
			}
			if (repeat == null) {
				if ((repeat = Utilities.findRepeat(repeatHistory, minPat, maxPat)) != null) {
					repeatIdx = 0;
					repeatPos = seqPos - 2*repeat.length + 1;
					numRepeat = 2;
					before = new ArrayList<Byte>();
					int end = repeatHistory.size() - 2*repeat.length;
					for (j = 0; j < end; j++) {
						before.add(repeatHistory.get(j));
					}
				}
			} else {
				if (repeat[repeatIdx] != buf[i]) {
					ShortTandemRepeat istr = insertRepeat(repeatMap, chr, repeat, numRepeat, repeatPos, before);
					if (istr != null) {
						afterHistory.add(istr);
						int off = repeatHistory.size() - 1 - repeatIdx;
						for (j = 0; j < repeatIdx; j++) {
							istr.appendAfter(repeatHistory.get(off+j));
						}
						istr.appendAfter(buf[i]);
					}
					repeat = null;
				} else {
					repeatIdx++;
					if (repeatIdx > repeat.length-1) {
						repeatIdx = 0;
						numRepeat++;	
					}
				}
			}
			seqPos++;
		}
		return new MapParam(seqPos, gtFlag, crFlag, lfFlag, chrHistory, repeatHistory, before, afterHistory, chr, repeat, repeatIdx, repeatPos, numRepeat);
	}

	private ShortTandemRepeat insertRepeat(HashMap<String, LinkedList<ShortTandemRepeat>> repeatMap, byte[] chr, byte[] repeat, int numRepeat, long repeatPos, ArrayList<Byte> before) {
		if (numRepeat < minRepeats) {
			disqualified++;
			return null;
		}
		LinkedList<ShortTandemRepeat> repeatList;
		String keyCurRepeat = new String(repeat);
		if ((repeatList = repeatMap.get(keyCurRepeat)) == null) {
			repeatList = new LinkedList<ShortTandemRepeat>();
			repeatMap.put(keyCurRepeat, repeatList);
		}
		ShortTandemRepeat str = new ShortTandemRepeat(chr, repeat, numRepeat, repeatPos, before);
		repeatList.add(str);
		return str;
	}
	
	private class MapParam {
		public long seqPos;
		public boolean gtFlag;
		public boolean crFlag;
		public boolean lfFlag;
		public LinkedList<Byte> chrHistory;
		public LinkedList<Byte> repeatHistory;
		public ArrayList<Byte> before;
		public LinkedList<ShortTandemRepeat> afterHistory;
		public byte[] chr;
		public byte[] repeat;
		public int repeatIdx;
		public long repeatPos;
		public int numRepeat;
		
		public MapParam(
				long seqPos,
				boolean gtFlag,
				boolean crFlag,
				boolean lfFlag,
				LinkedList<Byte> chrHistory,
				LinkedList<Byte> repeatHistory,
				ArrayList<Byte> before,
				LinkedList<ShortTandemRepeat> afterHistory,
				byte[] chr,
				byte[] repeat,
				int repeatIdx,
				long repeatPos,
				int numRepeat
				) {
			this.seqPos = seqPos;
			this.gtFlag = gtFlag;
			this.crFlag = crFlag;
			this.lfFlag = lfFlag;
			this.chrHistory = chrHistory;
			this.repeatHistory = repeatHistory;
			this.before = before;
			this.afterHistory = afterHistory;
			this.chr = chr;
			this.repeat = repeat;
			this.repeatIdx = repeatIdx;
			this.repeatPos = repeatPos;
			this.numRepeat = numRepeat;
		}
	} 
	
}
