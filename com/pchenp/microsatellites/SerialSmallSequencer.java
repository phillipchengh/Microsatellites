package com.pchenp.microsatellites;

import java.io.IOException;
import java.util.ArrayList;

public class SerialSmallSequencer extends SmallSequencer {

	private int numRead;
	
	public SerialSmallSequencer(String fileName) {
		super(fileName);
		genomeMap = new ArrayList<Integer>(genomeLen);
		for (int i = 0; i < genomeLen; i++) {
			genomeMap.add(i);
		}
		numRead = 0;
	}

	@Override
	public boolean hasNext() {
		return (numRead < genomeLen);
	}

	@Override
	public byte[] next() {
		int mapSize = genomeMap.size();
		int mapPos = (int) (Math.random()*mapSize);
		int genomePos = genomeMap.get(mapPos);
		int maxRead;
		if ((mapPos + Constants.READ_LEN) < mapSize) {
			maxRead = Constants.READ_LEN;
		} else {
			maxRead = mapSize - mapPos;
		}
		int i;
		for (i = 0; i < maxRead; i++) {
			if ((genomePos+i) != genomeMap.get(mapPos)) {
				break;
			}
			genomeMap.remove(mapPos);
		}
		numRead += i;
		byte[] genomeRead = new byte[i];
		try {
			raf.seek(genomePos);
			raf.read(genomeRead);
			return genomeRead;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

}
