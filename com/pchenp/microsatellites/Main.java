package com.pchenp.microsatellites;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Missing input file.");
		}
		SerialRepeatMapper map = new SerialRepeatMapper();
		HashMap<String, LinkedList<ShortTandemRepeat>> genomeMap = map.mapFile(args[0]);
		Iterator<LinkedList<ShortTandemRepeat>> iter = genomeMap.values().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		System.out.println("READER RESULTS:");
		SerialRepeatReader reader = new SerialRepeatReader(Default.READ_BUF_SIZE, Default.SHORT_READ_LEN, 1, Default.MIN_PAT_LEN, Default.MAX_PAT_LEN, Default.MIN_REPEATS);
		HashMap<String, ShortTandemRepeat> readMap = reader.readFile(args[0]);
		Iterator<ShortTandemRepeat> iter2 = readMap.values().iterator();
		while (iter2.hasNext()) {
			System.out.println(iter2.next().toString());
		}
	}

}
