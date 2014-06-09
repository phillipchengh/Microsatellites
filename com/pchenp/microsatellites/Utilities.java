package com.pchenp.microsatellites;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Utilities {

	public static void printIdentified(HashMap<String, LinkedList<ShortTandemRepeat>> ref, HashMap<ShortTandemRepeat, ShortTandemRepeat> priv) {
		int total;
		int found = 0;
		int lost = 0;
		int zero = 0;
		List<ShortTandemRepeat> refs = refToSortedList(ref);
		total = refs.size();
		Iterator<ShortTandemRepeat> refIter = refs.iterator();
		while (refIter.hasNext()) {
			ShortTandemRepeat r = refIter.next();
			if (priv.containsKey(r)) {
				ShortTandemRepeat p = priv.get(r);
				if (p.getRepeats() == 0) {
					zero++;
					lost++;
				} else {
					found++;	
				}
			} else {
				lost++;
			}
		}
		double accuracy = 0;
		if (total != 0) {
			accuracy = (double) found/total * 100; 
		}
		System.out.println("\n####Identification####");
		System.out.println("Reference total STRs: " + total);
		System.out.println("Private identified STRs: " + found);
		System.out.println("Private identified STRs with 0 count: " + zero);
		System.out.println("Private unidentified STRs (including 0 counts): " + lost);
		System.out.println("Identification accuracy: " + accuracy + "%");
	}
	
	public static void printCounts(HashMap<ShortTandemRepeat, ShortTandemRepeat> res) {
		int total = 0;
		int found = 0;
		int lost = 0;
		int mismatches = 0;
		double accuracy = 0;
		int repeatCount = 0;
		Set<ShortTandemRepeat> ref = res.keySet();
		Iterator<ShortTandemRepeat> refIter = ref.iterator();
		while (refIter.hasNext()) {
			ShortTandemRepeat refStr = refIter.next();
			ShortTandemRepeat readStr = res.get(refStr);
			if (refStr.isLost()) {
				lost += readStr.getRepeats();
			} else {
				total += refStr.getRepeats();
				found += readStr.getRepeats();
				mismatches += Math.abs(refStr.getRepeats()-readStr.getRepeats());
				accuracy += (double) (total-mismatches)/total;
				repeatCount++;
			}
		}
		if (repeatCount != 0) {
			accuracy = accuracy / repeatCount * 100;
		}
		System.out.println("\n####Aggregate Counts####");
		System.out.println("Reference count: " + total);
		System.out.println("Private identified count: " + found);
		System.out.println("Private unidentified count: " + lost);
		System.out.println("Private mismatch count: " + mismatches);
		System.out.println("Average STR accuracy: " + accuracy + "%");
	}
	
	public static void printRef(HashMap<String, LinkedList<ShortTandemRepeat>> ref, boolean printAll) {
		List<ShortTandemRepeat> strs = refToSortedList(ref);
		System.out.println("\n####Reference Results####");
		if (printAll) {
			for (ShortTandemRepeat str : strs) {
				System.out.println(str.toString());
			}	
		} else {
			for (ShortTandemRepeat str : strs) {
				System.out.println(str.toSmallString());
			}
		}
	}
	
	public static void writeRef(String outFileName, HashMap<String, LinkedList<ShortTandemRepeat>> ref, boolean writeAll) {
		List<ShortTandemRepeat> strs = refToSortedList(ref);
		writeStrs(outFileName, strs, writeAll);
		System.out.println("Finished writing reference results to " + outFileName);
	}
	
	public static void printPriv(HashMap<ShortTandemRepeat, ShortTandemRepeat> priv, boolean printAll) {
		List<ShortTandemRepeat> strs = privToSortedList(priv);
		System.out.println("\n####Private results####");
		if (printAll) {
			for (ShortTandemRepeat str : strs) {
				System.out.println(str.toString());
			}		
		} else {
			for (ShortTandemRepeat str : strs) {
				if (str.isLost()) {
					break;
				}
				System.out.println(str.toSmallString());
			}
		}
	}
	
	public static void writePriv(String outFileName, HashMap<ShortTandemRepeat, ShortTandemRepeat> priv, boolean writeAll) {
		List<ShortTandemRepeat> strs = privToSortedList(priv);
		writeStrs(outFileName, strs, writeAll);
		System.out.println("Finished writing reference results to " + outFileName);
	}
	
	private static void writeStrs(String outFileName, List<ShortTandemRepeat> strs, boolean writeAll) {
		BufferedWriter bw = null;
		File outFile = new File(outFileName);
		try {
			if (outFile.exists()) {
				outFile.delete();
			}
			bw = new BufferedWriter(new FileWriter(outFile));
			if (writeAll) {
				for (ShortTandemRepeat str : strs) {
					bw.write(str.toString());
					bw.newLine();
				}	
			} else {
				for (ShortTandemRepeat str : strs) {
					bw.write(str.toSmallString());
					bw.newLine();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void writeCM124Answer(String refFileName, String outFileName, HashMap<String, LinkedList<ShortTandemRepeat>> ref) {
		BufferedReader br = null;
		String genomeName = null;
		try {
			br = new BufferedReader(new InputStreamReader(Utilities.class.getResourceAsStream(refFileName), "UTF-8"));
			genomeName = br.readLine();
			System.out.println("Formatting for genome: " + genomeName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (genomeName == null) {
			throw new Error(refFileName + " does not have a genome name.");
		}
		List<ShortTandemRepeat> strs = refToSortedList(ref);
		BufferedWriter bw = null;
		File outFile = new File(outFileName);
		try {
			if (outFile.exists()) {
				outFile.delete();
			}
			if (!outFile.createNewFile()) {
				throw new RuntimeException(outFileName + " already exists.");
			}
			bw = new BufferedWriter(new FileWriter(outFile));
			bw.write(genomeName);
			bw.newLine();
			bw.write(">STR");
			for (ShortTandemRepeat str : strs) {
				bw.newLine();	
				bw.write(str.toString());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static List<ShortTandemRepeat> refToSortedList(HashMap<String, LinkedList<ShortTandemRepeat>> ref) {
		List<LinkedList<ShortTandemRepeat>> listStrs = new ArrayList<LinkedList<ShortTandemRepeat>>(ref.values());
		List<ShortTandemRepeat> strs = new ArrayList<ShortTandemRepeat>();
		for (List<ShortTandemRepeat> l : listStrs) {
			Iterator<ShortTandemRepeat> iter = l.iterator();
			while (iter.hasNext()) {
				strs.add(iter.next());	
			}
		}
		Collections.sort(strs);
		return strs;
	}
	
	private static List<ShortTandemRepeat> privToSortedList(HashMap<ShortTandemRepeat, ShortTandemRepeat> priv) {
		List<ShortTandemRepeat> strs = new ArrayList<ShortTandemRepeat>(priv.values());
		Collections.sort(strs);
		return strs;
	}
	
	public static boolean validBase(byte b) {
		for (int i = 0; i < Const.BASES.length; i++) {
			if (b == Const.BASES[i]) {
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
		byte[] bases = Const.BASES;
		int baseNum = Const.BASES.length;
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
