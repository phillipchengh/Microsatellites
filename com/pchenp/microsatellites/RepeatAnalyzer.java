package com.pchenp.microsatellites;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RepeatAnalyzer {
	
	public static HashMap<String, ShortTandemRepeat> analyzeReads(HashMap<String, LinkedList<ShortTandemRepeat>> ref, HashMap<String, ShortTandemRepeat> priv) {
		HashMap<String, ShortTandemRepeat> approxRef = mergeRepeatsWithSimiliarPatterns(mergeRepeatsWithSamePosition(ref));
		return null;
	}
	
	public static HashMap<String, ShortTandemRepeat> mergeRepeatsWithSamePosition(HashMap<String, LinkedList<ShortTandemRepeat>> ref) {
		HashMap<String, ShortTandemRepeat> mergedRef = new HashMap<String, ShortTandemRepeat>();
		Iterator<String> refPatsIter = ref.keySet().iterator();
		while (refPatsIter.hasNext()) {
			String p = refPatsIter.next();
			LinkedList<ShortTandemRepeat> refStrList = ref.get(p);
			ShortTandemRepeat t;
			if (refStrList.size() == 1) {
				t = new ShortTandemRepeat(refStrList.getFirst());
			} else {
				Iterator<ShortTandemRepeat> refStr = ref.get(p).iterator();
				ShortTandemRepeat s = null;
				int numRepeats = 0;
				while (refStr.hasNext()) {
					s = refStr.next();
					numRepeats += s.getRepeats();
				}
				t = new ShortTandemRepeat(s.getSequence(), numRepeats);
			}
			mergedRef.put(p, t);
		}
		return mergedRef;
	}
	
	public static HashMap<String, ShortTandemRepeat> mergeRepeatsWithSimiliarPatterns(HashMap<String, ShortTandemRepeat> ref) {
		HashMap<String, ShortTandemRepeat> mergedMap = new HashMap<String, ShortTandemRepeat>();
		List<String> refPatsList = new LinkedList<String>(ref.keySet());
		while (refPatsList.size() > 0) {
			String p = refPatsList.remove(0);
			ShortTandemRepeat cur = ref.get(p);
			int numRepeats = cur.getRepeats();
			for (int i = 0; i < p.length()-1; i++) {
				p = p.substring(1) + p.charAt(0);
				if (refPatsList.contains(p)) {
					ShortTandemRepeat s = ref.get(p);
					numRepeats += s.getRepeats();
					if (s.getRepeats() > cur.getRepeats()) {
						cur = s;
					}
					refPatsList.remove(p);
				}
			}
			if (numRepeats == cur.getRepeats()) {
				mergedMap.put(new String(cur.getSequence()), cur);	
			} else {
				ShortTandemRepeat t = new ShortTandemRepeat(cur.getSequence(), numRepeats);
				mergedMap.put(new String(cur.getSequence()), t);
			}
		}
		return mergedMap;
	}
	
	public static HashMap<String, ShortTandemRepeat> mergeReads(HashMap<String, ShortTandemRepeat> ref, HashMap<String, ShortTandemRepeat> priv) {
		HashMap<String, ShortTandemRepeat> mergedReads = new HashMap<String, ShortTandemRepeat>();
		List<String> privPatsList = new LinkedList<String>(priv.keySet());
		while (privPatsList.size() > 0) {
			String p = privPatsList.get(0);
			boolean inRef = false;
			ShortTandemRepeat cur = priv.get(p);
			int numRepeats = 0;
			for (int i = 0; i < p.length(); i++) {
				if (privPatsList.contains(p)) {
					ShortTandemRepeat s = priv.get(p);
					numRepeats += s.getRepeats();
					if (!inRef && (s.getRepeats() > cur.getRepeats())) {
						cur = s;
					}
					privPatsList.remove(p);
				}
				if (!inRef && ref.containsKey(p)) {
					cur = ref.get(p);
					inRef = true;
				}
				p = p.substring(1) + p.charAt(0);
			}
			ShortTandemRepeat mergedStr = new ShortTandemRepeat(cur);
			mergedStr.setRepeats(numRepeats);
			if (inRef) {
				mergedReads.put(new String(mergedStr.getSequence()), mergedStr);
			}
		}
		return mergedReads;
	}
	
}
