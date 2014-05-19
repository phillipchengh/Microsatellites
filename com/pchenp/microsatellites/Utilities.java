package com.pchenp.microsatellites;

import java.util.Iterator;
import java.util.LinkedList;

public class Utilities {
	
	public static boolean validBase(byte b) {
		for (int i = 0; i < Default.BASES.length; i++) {
			if (b == Default.BASES[i]) {
				return true;
			}
		}
		return false;
	}
	
	public static byte[] findRepeat(LinkedList<Byte> repeatHistory, int minPat, int maxPat) {
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
	
	public LinkedList<byte[]> allPerms(int minPat, int maxPat) {
		int i, j, k;
		byte[] bases = Default.BASES;
		int baseNum = Default.BASES.length;
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
				if (validPerm(pat, minPat)) {
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
	
	public boolean validPerm(byte[] perm, int minPat) {
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
	
}
