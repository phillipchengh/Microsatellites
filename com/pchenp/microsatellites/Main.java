package com.pchenp.microsatellites;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Missing input file.");
		}
		SerialRepeatMapper map = SerialRepeatMapper.builder().build();
		HashMap<String, LinkedList<ShortTandemRepeat>> genomeMap = map.mapFile(args[0]);
		Iterator<LinkedList<ShortTandemRepeat>> iter = genomeMap.values().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		System.out.println("READER RESULTS:");
		SerialRepeatReader reader = SerialRepeatReader.builder().coverage(1).build();
		HashMap<String, ShortTandemRepeat> readMap = reader.readFile(args[0]);
		Iterator<ShortTandemRepeat> iter2 = readMap.values().iterator();
		while (iter2.hasNext()) {
			System.out.println(iter2.next().toString());
		}
		
	}

}
