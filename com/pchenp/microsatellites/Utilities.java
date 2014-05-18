package com.pchenp.microsatellites;

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
	
}
