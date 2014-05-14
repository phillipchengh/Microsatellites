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
//		LinkedList<byte[]> perms = allPerms();
//		Iterator<byte[]> iter = perms.iterator();
//		while (iter.hasNext()) {
//			byte[] perm = iter.next();
//			repeatMap.put(perm, new LinkedList<ShortTandemRepeat>());
//		}
	}

	@Override
	public HashMap<String, LinkedList<ShortTandemRepeat>> getRepeatMap() {
		return (HashMap<String, LinkedList<ShortTandemRepeat>>) repeatMap;
	}
	
	private class MapParam {
		public long bufOffset;
		public LinkedList<Byte> history;
		public byte[] repeat;
		public int repeatIdx;
		public long repeatPos;
		public int numRepeat;
		public boolean gtFlag;
		public boolean crFlag;
		public boolean lfFlag;
		
		public MapParam(long bufOffset, LinkedList<Byte> history, byte[] repeat, int repeatIdx, long repeatPos, int numRepeat, boolean gtFlag, boolean crFlag, boolean lfFlag) {
			this.bufOffset = bufOffset;
			this.history = history;
			this.repeat = repeat;
			this.repeatIdx = repeatIdx;
			this.repeatPos = repeatPos;
			this.numRepeat = numRepeat;
			this.gtFlag = gtFlag;
			this.crFlag = crFlag;
			this.lfFlag = lfFlag;
		}
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
			bis.read(buf);
			MapParam next = mapBuf(buf, null);
			while (bis.available() > 0) {
				bis.read(buf);
				next = mapBuf(buf, next);
			}
			bis.close();
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
	
	public MapParam mapBuf(byte[] buf, MapParam params) {
		int i, j;
		int bufSize = buf.length;
		long bufOffset;
		LinkedList<Byte> history = null;
		byte[] curRepeat;
		int curRepeatIdx = -1;
		long curRepeatPos = -1;
		int numRepeat = -1;
		int seqPos = 0;
		boolean foundRepeat, gtFlag, crFlag, lfFlag;
		LinkedList<Byte> chrHistory = null;
		byte[] curChr;
		if (params == null) {
			history = new LinkedList<Byte>();
			for (i = 0; i < maxPat*2; i++) {
				history.add((byte) 0);
			}
			bufOffset = 0;
			gtFlag = false;
			crFlag = false;
			lfFlag = true;
		} else {
			history = params.history;
			bufOffset = params.bufOffset;
			gtFlag = params.gtFlag;
			crFlag = params.crFlag;
			lfFlag = params.lfFlag;
		}
		if (params == null || params.repeat == null) {
			curRepeat = null;
			foundRepeat = false;
		} else {
			curRepeat = params.repeat;
			curRepeatIdx = params.repeatIdx;
			curRepeatPos = params.repeatPos;
			numRepeat = params.numRepeat;
			foundRepeat = true;
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
					curChr = new byte[chrLen];
					for (j = 0; j < chrLen; j++) {
						curChr[j] = chrHistory.get(j);
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
//				throw new RuntimeException("Invalid Char: " + (char) buf[i]);
			}
			history.remove();
			history.add(buf[i]);
			if (!foundRepeat) {
				if ((curRepeat = findRepeat(history)) != null) {
					foundRepeat = true;
					curRepeatIdx = 0;
					curRepeatPos = bufOffset + seqPos - 2*curRepeat.length + 1;
					numRepeat = 2;
				}
			} else {
				if (curRepeat[curRepeatIdx] != buf[i]) {
					foundRepeat = false;
					if (numRepeat >= Constants.MIN_REPEATS) {
						LinkedList<ShortTandemRepeat> repeatList;
						String keyCurRepeat = new String(curRepeat);
						if ((repeatList = repeatMap.get(keyCurRepeat)) == null) {
							repeatList = new LinkedList<ShortTandemRepeat>();
							repeatList.add(new ShortTandemRepeat(curRepeat, curRepeatPos, numRepeat));
							repeatMap.put(keyCurRepeat, repeatList);
						} else {
							repeatList.add(new ShortTandemRepeat(curRepeat, curRepeatPos, numRepeat));	
						}
					}
				} else {
					curRepeatIdx++;
					if (curRepeatIdx > curRepeat.length-1) {
						curRepeatIdx = 0;
						numRepeat++;	
					}
				}
			}
			seqPos++;
		}
		MapParam next;
		if (foundRepeat) {
			next = new MapParam(bufOffset+i, history, curRepeat, curRepeatIdx, curRepeatPos, numRepeat, gtFlag, crFlag, lfFlag);
		} else {
			next = new MapParam(bufOffset+i, history, null, -1, -1, -1, gtFlag, crFlag, lfFlag);
		}
		return next;
	}
	
	public byte[] findRepeat(LinkedList<Byte> historyList) {
		int i, j;
		int first, second;
		int historyLen = historyList.size();
		boolean foundFlag;
		byte[] history = new byte[historyLen];
		for (i = 0; i < historyLen; i++) {
			history[i] = historyList.get(i).byteValue();
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

}
