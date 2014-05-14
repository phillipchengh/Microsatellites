package com.pchenp.microsatellites;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Missing input file.");
		}
		SerialRepeatMapper map = new SerialRepeatMapper();
		map.mapFile(args[0]);
		HashMap<String, LinkedList<ShortTandemRepeat>> genomeMap = map.getRepeatMap();
		Iterator<LinkedList<ShortTandemRepeat>> iter = genomeMap.values().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
//		LinkedList<byte[]> perms = map.allPerms();
//		Iterator<byte[]> iter = perms.iterator();
//		while(iter.hasNext()) {
//			byte[] perm = iter.next();
//			System.out.println(new String(perm).toCharArray());
//		}
//		Byte[] history = {(byte) 'A', (byte) 'C', (byte) 'T', (byte) 'G', (byte) 'A', (byte) 'A', (byte) 'C', (byte) 'T', (byte) 'G', (byte) 'A'};
//		LinkedList<Byte> historyList = new LinkedList<Byte>(Arrays.asList(history));
//		System.out.println(map.findRepeat(historyList));
	}

}
