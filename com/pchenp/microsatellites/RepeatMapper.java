package com.pchenp.microsatellites;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Phillip
 *
 */
public abstract class RepeatMapper<T extends List<ShortTandemRepeat>> {
	
	protected File file;
	protected int readBufSize;
	protected int minPat;
	protected int maxPat;
	protected Map<String, T> repeatMap;
	
	protected RepeatMapper() {
		this(Constants.READ_BUF_SIZE, Constants.MIN_PAT_LEN, Constants.MAX_PAT_LEN);
	}
	
	protected RepeatMapper(int bufSize, int minPat, int maxPat) {
		this.readBufSize = bufSize;
		this.minPat = minPat;
		this.maxPat = maxPat;
	}
	
	protected LinkedList<byte[]> allPerms() {
		int i, j, k;
		byte[] bases = Constants.BASES;
		int baseNum = Constants.BASES.length;
		int subPerm = 1;
		int numLens = maxPat - minPat + 1;
		int[] numPatLenPerms = new int[numLens];
		for (i = 0; i < minPat-1; i++) {
			subPerm *= baseNum;
		}
		int numPerm = subPerm;
		for (i = minPat; i <= maxPat; i++) {
			numPerm *= baseNum;
			numPatLenPerms[i-minPat] = numPerm;
		}
		LinkedList<byte[]> patPerms = new LinkedList<byte[]>();
		int[] idx;
		for (i = minPat; i <= maxPat; i++) {
			idx = new int[i];
			numPerm = numPatLenPerms[i-minPat];
			for (j = 0; j < numPerm; j++) {
				byte[] pat = new byte[i];
				for (k = 0; k < i; k++) {
					pat[k] = bases[idx[k]];
				}
				if (validPerm(pat)) {
					patPerms.add(pat);	
				}
				for (k = i-1; k >= 0; k--) {
					idx[k]++;
					if (idx[k] > baseNum-1) {
						idx[k] = 0;
					} else {
						break;
					}
				}
			}
		}
		return patPerms;
	}
	
	protected boolean validPerm(byte[] perm) {
		int i;
		LinkedList<Integer> factors = new LinkedList<Integer>();
		int permLen = perm.length;
		if (permLen <= minPat) {
			return true;
		} else {
			byte testByte = perm[0];
			boolean uniformPerm = true;
			for (i = 1; i < permLen; i++) {
				if (testByte != perm[i]) {
					uniformPerm = false;
				}
			}
			if (uniformPerm) {
				return false;
			}
		}
		for (i = minPat; i <= permLen/2; i++) {
			if ((permLen % i) == 0) {
				factors.add(i);
			}
		}
		Iterator<Integer> iter = factors.iterator();
		while (iter.hasNext()) {
			int factLen = iter.next();
			byte[] factPerm = new byte[factLen];
			for (i = 0; i < factLen; i++) {
				factPerm[i] = perm[i];
			}
			if (permMadeOfFact(factPerm, perm)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean permMadeOfFact(byte[] factPerm, byte[] perm) {
		int i, j;
		int factLen = factPerm.length;
		int permLen = perm.length;
		for (i = factLen; i < permLen; i+=factLen) {
			for (j = 0; j < factLen; j++) {
				if (perm[j+i] != factPerm[j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	abstract public HashMap<String, LinkedList<ShortTandemRepeat>> getRepeatMap();
	
	abstract protected void mapFile(String fileName);
	
	abstract protected byte[] findRepeat(LinkedList<Byte> historyList);
	
}
