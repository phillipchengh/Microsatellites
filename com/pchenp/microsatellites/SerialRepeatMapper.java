package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

public class SerialRepeatMapper extends RepeatMapper<LinkedList<ShortTandemRepeat>> {

	public SerialRepeatMapper() {
		this(Default.READ_BUF_SIZE, Default.MIN_PAT_LEN, Default.MAX_PAT_LEN, Default.MIN_REPEATS);
	}

	public SerialRepeatMapper(int bufSize, int minPat, int maxPat, int minRepeats) {
		super(bufSize, minPat, maxPat, minRepeats);
	}
	
	@Override
	protected HashMap<String, LinkedList<ShortTandemRepeat>> mapFile(String fileName) {
		HashMap<String, LinkedList<ShortTandemRepeat>> repeatMap = new HashMap<String, LinkedList<ShortTandemRepeat>>();
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = this.getClass().getResourceAsStream(fileName);
			bis = new BufferedInputStream(is);
			System.out.println("Mapping \"" + fileName + "\"...");
			byte[] buf = new byte[readBufSize];
			int numBytes = bis.read(buf);
			MapParam next = mapBuf(repeatMap, buf, numBytes, null);
			while (bis.available() > 0) {
				numBytes = bis.read(buf);
				next = mapBuf(repeatMap, buf, numBytes, next);
			}
			if (next.repeat != null) {
				insertRepeat(repeatMap, next.chr, next.repeat, next.numRepeat, next.repeatPos);
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
		return repeatMap;
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
			repeatHistory = new LinkedList<Byte>();
			for (i = 0; i < maxPat*2; i++) {
				repeatHistory.add((byte) 0);
			}
			chrHistory = null;
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
			if (repeat == null) {
				if ((repeat = Utilities.findRepeat(repeatHistory, minPat, maxPat)) != null) {
					repeatIdx = 0;
					repeatPos = seqPos - 2*repeat.length + 1;
					numRepeat = 2;
				}
			} else {
				if (repeat[repeatIdx] != buf[i]) {
					insertRepeat(repeatMap, chr, repeat, numRepeat, repeatPos);
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
		return new MapParam(seqPos, gtFlag, crFlag, lfFlag, chrHistory, repeatHistory, chr, repeat, repeatIdx, repeatPos, numRepeat);
	}

	private void insertRepeat(HashMap<String, LinkedList<ShortTandemRepeat>> repeatMap, byte[] chr, byte[] repeat, int numRepeat, long repeatPos) {
		if (numRepeat < minRepeats) {
			return;
		}
		LinkedList<ShortTandemRepeat> repeatList;
		String keyCurRepeat = new String(repeat);
		if ((repeatList = repeatMap.get(keyCurRepeat)) == null) {
			repeatList = new LinkedList<ShortTandemRepeat>();
			repeatMap.put(keyCurRepeat, repeatList);
		}
		repeatList.add(new ShortTandemRepeat(chr, repeat, numRepeat, repeatPos));
	}
	
	private class MapParam {
		public long seqPos;
		public boolean gtFlag;
		public boolean crFlag;
		public boolean lfFlag;
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
			this.chrHistory = chrHistory;
			this.repeatHistory = repeatHistory;
			this.chr = chr;
			this.repeat = repeat;
			this.repeatIdx = repeatIdx;
			this.repeatPos = repeatPos;
			this.numRepeat = numRepeat;
		}
	} 
	
}
