package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

abstract public class RepeatReader {

	abstract public static class RBuilder<T extends RBuilder<T>> {
		private int bufSize = Const.DEF_BUF;
		private int readSize = Const.DEF_READ;
		private int coverage = Const.DEF_COVERAGE;
		private int minPat = Const.DEF_MIN_PAT;
		private int maxPat = Const.DEF_MAX_PAT;
		private int minRepeats = Const.DEF_MIN_REPEATS;
		private double minScore = Const.DEF_MIN_SCORE;
		private boolean stats = Const.STATS;
		private int numThreads = Const.DEF_THREADS;
		private HashMap<String, LinkedList<ShortTandemRepeat>> ref;
		
		abstract protected T self();
		
		public T bufSize(int bufSize) {
			this.bufSize = bufSize;
			return self();
		}
		
		public T readSize(int readSize) {
			this.readSize = readSize;
			return self();
		}
		
		public T coverage(int coverage) {
			this.coverage = coverage;
			return self();
		}
		
		public T minPat(int minPat) {
			this.minPat = minPat;
			return self();
		}
		
		public T maxPat(int maxPat) {
			this.maxPat = maxPat;
			return self();
		}
		
		public T minRepeats(int minRepeats) {
			this.minRepeats = minRepeats;
			return self();
		}
		
		public T minScore(double minScore) {
			this.minScore = minScore;
			return self();
		}
		
		public T stats(boolean stats) {
			this.stats = stats;
			return self();
		}
		
		public T numThreads(int numThreads) {
			this.numThreads = numThreads;
			return self();
		}
		
		public T ref(HashMap<String, LinkedList<ShortTandemRepeat>> ref) {
			this.ref = ref;
			return self();
		}

		abstract public RepeatReader build();
	}

	protected final int bufSize;
	protected final int readSize;
	protected final int coverage;
	protected final int minPat;
	protected final int maxPat;
	protected final int minRepeats;
	protected final double minMat;
	protected final boolean stats;
	protected final int numThreads;
	protected final HashMap<String, LinkedList<ShortTandemRepeat>> ref;

	protected RepeatReader(RBuilder<?> b) {
		bufSize = b.bufSize;
		readSize = b.readSize;
		coverage = b.coverage;
		minPat = b.minPat;
		maxPat = b.maxPat;
		minRepeats = b.minRepeats;
		numThreads = b.numThreads;
		minMat = b.minScore;
		stats = b.stats;
		ref = b.ref;
	}
	
	abstract protected void printStats();
	
	abstract public HashMap<ShortTandemRepeat, ShortTandemRepeat> readFile(String fileName);
	
	protected ShortTandemRepeat bestMatch(byte[] repeat, int numRepeats, byte[] buf, int repeatPos) {
		int i, j;
		int readSize = buf.length;
		int repeatSize = repeat.length;
		int numCandidates = 0;
		int origPos = repeatPos;
		int origRepeats = numRepeats;
		String pat = new String(repeat);
		double bestMat = (double) minMat;
		double curMat = 0;
		ShortTandemRepeat bestStr = null;
		ShortTandemRepeat uniqueStr = null;
		ArrayList<Byte> priv = new ArrayList<Byte>();
		boolean before, after;
		for (i = 0; i < readSize; i++) {
			priv.add(buf[i]);
		}
		if (repeatPos == 0) {
			before = false;
		} else {
			before = true;
		}
		for (i = 0; i < repeatSize; i++) {
			numRepeats = 0;
			int repeatIdx = i;
			int repeatTrack = 0;
			after = false;
			for (j = repeatPos; j < readSize; j++) {
				if (repeat[repeatIdx] != buf[j]) {
					after = true;
					break;
				}
				repeatIdx++;
				if (repeatIdx == repeatSize) {
					repeatIdx = 0;
				}
				repeatTrack++;
				if (repeatTrack == repeatSize) {
					numRepeats++;
					repeatTrack = 0;
				}
			}
			if (numRepeats < 2) {
				break;
			}
			int afterPos = repeatPos + repeatSize*numRepeats;
			if (ref.containsKey(pat)) {
				LinkedList<ShortTandemRepeat> strs = ref.get(pat);
				curMat = 0;
				for (ShortTandemRepeat str : strs) {
					curMat = calcScore(str, priv, repeatPos, afterPos, readSize, before, after);
					if (curMat > bestMat) {
						bestMat = curMat;
						bestStr = str;
					}
					uniqueStr = str;
					numCandidates++;
				}
			}
			if (i == (repeatSize-1)) {
				break;
			}
			repeatPos++;
			pat = pat.substring(1) + pat.charAt(0);
		}
		if (numCandidates == 1) {
			if ((repeatSize*origRepeats) > (readSize-repeatSize)) {
				int repeatIdx = repeatSize-origPos;
				boolean entirelyRepeat = true;
				if (repeatIdx > 0 && repeatIdx < repeatSize) {
					for (i = 0; i < origPos; i++) {
						if (buf[i] != repeat[repeatIdx]) {
							entirelyRepeat = false;
							break;
						}
						repeatIdx++;
					}	
				}
				if (entirelyRepeat) {
					int afterPos = origPos + repeatSize*origRepeats;
					repeatIdx = 0;
					for (i = afterPos; i < buf.length; i++) {
						if (buf[i] != repeat[repeatIdx]) {
							entirelyRepeat = false;
							break;
						}
						repeatIdx++;
					}
					if (entirelyRepeat) {
						return uniqueStr;
					}
				}
				
			}
		}
		return bestStr;
	}
	
	protected double calcScore(ShortTandemRepeat str, ArrayList<Byte> priv, int repeatPos, int afterPos, int readSize, boolean before, boolean after) {
		int checks = 0;
		ArrayList<Byte> beforeTag = null;
		ArrayList<Byte> afterTag = null;
		int count = 0;
		if (before) {
			checks += repeatPos;
			beforeTag = str.getBefore();
			count += countMatches(beforeTag, priv, 0, repeatPos, true);
		}
		if (after) {
			checks += (readSize - afterPos);
			afterTag = str.getAfter();
			count += countMatches(afterTag, priv, afterPos, readSize, false);
		}
		if (checks <= 0) {
			return 0.0;
		}
		double score = ((double) count/checks);
		return score;
	}
	
	protected double countMatches(ArrayList<Byte> ref, ArrayList<Byte> priv, int start, int end, boolean before) {
		int i;
		int count = 0;
		int refOff;
		int privOff;
//		byte[] rtemp = new byte[ref.size()];
//		byte[] ptemp = new byte[priv.size()];
//		for (i = 0; i < ref.size(); i++) {
//			rtemp[i] = ref.get(i);
//		}
//		for (i = 0; i < priv.size(); i++) {
//			ptemp[i] = priv.get(i);
//		}
//		String rdebug = new String(rtemp);
//		String pdebug = new String(ptemp);
		if (before) {
			refOff = ref.size() - end;
			privOff = 0;
		} else {
			refOff = 0;
			privOff = start;
		}
		int checks = end - start;
		for (i = 0; i < checks; i++) {
			byte p = priv.get(i+privOff);
			byte r = ref.get(i+refOff);
			if (p == r) {
				count++;
			}
		}
		return count;
	}

	protected File stripFile(String fileName) {
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
			System.out.println("\nWriting stripped \"" + fileName + "\" to temp file...");
			byte[] buf = new byte[bufSize];
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
	
	protected void coverCounts(HashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap) {
		Set<ShortTandemRepeat> keys = repeatMap.keySet();
		Iterator<ShortTandemRepeat> iter = keys.iterator();
		while (iter.hasNext()) {
			ShortTandemRepeat next = iter.next();
			ShortTandemRepeat str = repeatMap.get(next);
			int normNumRepeats = str.getRepeats()/coverage;
			if (normNumRepeats == 0 && str.isLost()) {
				iter.remove();
			} else {
				str.setRepeats(normNumRepeats);	
			}
		}
	}
	
	protected int writeStrippedBuf(BufferedOutputStream bos, byte[] inBuf, int inBufSize) {
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
}