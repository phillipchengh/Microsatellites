package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class SerialRepeatMapper extends RepeatMapper<LinkedList<ShortTandemRepeat>> {

	public SerialRepeatMapper() {
		this(Constants.READ_BUF_SIZE, Constants.MIN_PAT_LEN, Constants.MAX_PAT_LEN);
	}

	public SerialRepeatMapper(int bufSize, int minPat, int maxPat) {
		super(bufSize, minPat, maxPat);
		repeatMap = new HashMap<String, LinkedList<ShortTandemRepeat>>();
	}

	@Override
	public HashMap<String, LinkedList<ShortTandemRepeat>> getRepeatMap() {
		return (HashMap<String, LinkedList<ShortTandemRepeat>>) repeatMap;
	}
	
	private boolean validBase(byte b) {
		for (int i = 0; i < Constants.BASES.length; i++) {
			if (b == Constants.BASES[i]) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void mapFile(String fileName) {
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = this.getClass().getResourceAsStream(fileName);
			bis = new BufferedInputStream(is);
			System.out.println("Opened \"" + fileName + "\"");
			byte[] buf = new byte[readBufSize];
			int numBytes = bis.read(buf);
			MapParam next = mapBuf(buf, numBytes, null);
			while (bis.available() > 0) {
				numBytes = bis.read(buf);
				next = mapBuf(buf, numBytes, next);
			}
			bis.close();
			if (next.repeatFlag) {
				insertRepeat(next.chr, next.repeat, next.numRepeat, next.repeatPos);
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
	}

	private class MapParam {
		public long seqPos;
		public boolean gtFlag;
		public boolean crFlag;
		public boolean lfFlag;
		public boolean repeatFlag;
		public LinkedList<Byte> chrHistory;
		public LinkedList<Byte> repeatHistory;
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
				boolean repeatFlag,
				LinkedList<Byte> chrHistory,
				LinkedList<Byte> repeatHistory,
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
			this.repeatFlag = repeatFlag;
			this.chrHistory = chrHistory;
			this.repeatHistory = repeatHistory;
			this.chr = chr;
			this.repeat = repeat;
			this.repeatIdx = repeatIdx;
			this.repeatPos = repeatPos;
			this.numRepeat = numRepeat;
		}
	} 
	
	public MapParam mapBuf(byte[] buf, int numBytes, MapParam params) {
		int i, j;
		int bufSize = numBytes;
		long seqPos = 0;
		boolean gtFlag;
		boolean crFlag;
		boolean lfFlag;
		boolean repeatFlag;
		LinkedList<Byte> chrHistory;
		LinkedList<Byte> repeatHistory;
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
			repeatFlag = false;
			repeatHistory = new LinkedList<Byte>();
			for (i = 0; i < maxPat*2; i++) {
				repeatHistory.add((byte) 0);
			}
			chrHistory = null;
			chr = null;
			repeat = null;
			repeatIdx = -1;
			repeatPos = -1;
			numRepeat = -1;
		} else {
			seqPos = params.seqPos;
			gtFlag = params.gtFlag;
			crFlag = params.crFlag;
			lfFlag = params.lfFlag;
			repeatFlag = params.repeatFlag;
			repeatHistory = params.repeatHistory;
			chrHistory = params.chrHistory;
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
					throw new RuntimeException("Multiple carriage return.");
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
			if (!validBase(buf[i])) {
				throw new RuntimeException("Invalid Char: " + (char) buf[i]);
			}
			repeatHistory.remove();
			repeatHistory.add(buf[i]);
			if (!repeatFlag) {
				if ((repeat = findRepeat(repeatHistory)) != null) {
					repeatFlag = true;
					repeatIdx = 0;
					repeatPos = seqPos - 2*repeat.length + 1;
					numRepeat = 2;
				}
			} else {
				if (repeat[repeatIdx] != buf[i]) {
					repeatFlag = false;
					insertRepeat(chr, repeat, numRepeat, repeatPos);
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
		return new MapParam(seqPos, gtFlag, crFlag, lfFlag, repeatFlag, chrHistory, repeatHistory, chr, repeat, repeatIdx, repeatPos, numRepeat);
	}
	
	protected byte[] findRepeat(LinkedList<Byte> repeatHistory) {
		int i, j;
		int first, second;
		int historyLen = repeatHistory.size();
		boolean foundFlag;
		byte[] history = new byte[historyLen];
		for (i = 0; i < historyLen; i++) {
			history[i] = repeatHistory.get(i).byteValue();
		}
		for (i = minPat; i <= maxPat; i++) {
			first = historyLen - 2*i;
			second = historyLen - i;
			foundFlag = true;
			for (j = 0; j < i; j++) {
				if (history[first+j] != history[second+j]) {
					foundFlag = false;
					break;
				}
			}
			if (foundFlag) {
				byte[] repeat = new byte[i];
				for (j = 0; j < i; j++) {
					repeat[j] = history[first+j];
				}
				return repeat;
			}
		}
		return null;
	}

	private void insertRepeat(byte[] chr, byte[] repeat, int numRepeat, long repeatPos) {
		LinkedList<ShortTandemRepeat> repeatList;
		String keyCurRepeat = new String(repeat);
		if ((repeatList = repeatMap.get(keyCurRepeat)) == null) {
			repeatList = new LinkedList<ShortTandemRepeat>();
			repeatMap.put(keyCurRepeat, repeatList);
		}
		repeatList.add(new ShortTandemRepeat(chr, repeat, numRepeat, repeatPos));
	}
}
