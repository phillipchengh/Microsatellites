package com.pchenp.microsatellites;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentRepeatReader extends RepeatReader {

	private long totalTime = -1;
	private int lost = 0;
	private int found = 0;
	private int disqualified = 0;
	
	abstract public static class RBuilder<T extends RBuilder<T>> extends RepeatReader.RBuilder<T> {
		
		public ConcurrentRepeatReader build() {
			return new ConcurrentRepeatReader(this);
		}
		
	}

	private static class RBuilder2 extends RBuilder<RBuilder2> {

		@Override
		protected RBuilder2 self() {
			return this;
		}
	}
	
	public static RBuilder<?> builder() {
		return new RBuilder2();
	}

	public ConcurrentRepeatReader(RBuilder<?> sb) {
		super(sb);
	}
	
	@Override
	protected void printStats() {
		System.out.println("\n#####Reader Stats#####");
		System.out.println("Total time:" + totalTime/Math.pow(10, 3));
		System.out.println("Disqualified repeats: " + disqualified);
		System.out.println("Identified repeats: " + found);
		System.out.println("Unidentified repeats: " + lost);
		System.out.println("Ratio: " + (double) found/(found + lost));
	}

	@Override
	public HashMap<ShortTandemRepeat, ShortTandemRepeat> readFile(String fileName) {
		totalTime = -1;
		lost = 0;
		found = 0;
		disqualified = 0;
		totalTime = System.currentTimeMillis();
		File stripped = stripFile(fileName);
		Broker broker = new Broker();
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		try {
			for (int i = 0; i < numThreads-1; i++) {
				System.out.println("Executing consumer thread " + (i+1) + ".");
				threadPool.execute(new Consumer(broker));	
			}
			System.out.println("Starting production thread.");
			Future<?> producerStatus = threadPool.submit(new Producer(broker, fileName, stripped));
			producerStatus.get();
			threadPool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		HashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap = new HashMap<ShortTandemRepeat, ShortTandemRepeat>(broker.repeatMap);
		coverCounts(repeatMap);
		totalTime = System.currentTimeMillis() - totalTime;
		if (stats) {
			printStats();	
		}
		return repeatMap;
	}

	private class Broker {
		
		private BlockingQueue<byte[]> queue;
		public ConcurrentHashMap<ShortTandemRepeat, ShortTandemRepeat> repeatMap;
		public boolean producing;
		
		public Broker() {
			 this.queue = new LinkedBlockingQueue<byte[]>();
			 repeatMap = new ConcurrentHashMap<ShortTandemRepeat, ShortTandemRepeat>();
			 this.producing = true;
		}
		
		public void put(byte[] read) throws InterruptedException {
			queue.put(read);
		}
		
		public byte[] take() throws InterruptedException {
			return queue.poll(1, TimeUnit.MILLISECONDS);
		}
	}
	
	public class Producer implements Runnable {

		private Broker broker;
		private String fileName;
		private File stripped; 
		
		public Producer(Broker broker, String fileName, File stripped) {
			this.broker = broker;
			this.fileName = fileName;
			this.stripped = stripped;
		}
		
		@Override
		public void run() {
			InputStream is = null;
			BufferedInputStream bis = null;
			RandomAccessFile raf = null; 
			try {
				is = new FileInputStream(stripped);
				bis = new BufferedInputStream(is);
				raf = new RandomAccessFile(stripped, "r");
				long seqLen = raf.length();
				long numReads = (seqLen/readSize)*coverage;
				System.out.println("Conducting coverage " + coverage + "x " + "(Number of Reads: " + numReads + ")" + " short reads on \"" + fileName + "\"...");
				for (long j = 0; j < numReads; j++) {
					long seqPos = (long) (Math.random()*seqLen);
					int maxRead;
					if ((seqPos + readSize) < seqLen) {
						maxRead = readSize;
					} else {
						maxRead = (int) (seqLen - seqPos);
					}
					byte[] buf = new byte[maxRead];
					try {
						raf.seek(seqPos);
						raf.read(buf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					broker.put(buf);
				}
				broker.producing = false;
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
				stripped.deleteOnExit();
			}
		}
		
	}
	
	public class Consumer implements Runnable {

		private Broker broker;
		
		public Consumer(Broker broker) {
			this.broker = broker;
		}
		
		@Override
		public void run() {
			try {
				byte[] read = broker.take();
				while (broker.producing || read != null) {
					read = broker.take();
					if (read != null) {
						mapShortRead(read);	
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void mapShortRead(byte[] buf) {
			int i;
			int readSize = buf.length;
			byte[] repeat = null;
			int repeatIdx = 0;
			int numRepeat = 0;
			int repeatPos = 0;
			LinkedList<Byte> repeatHistory = new LinkedList<Byte>();
			for (i = 0; i < maxPat*2; i++) {
				repeatHistory.add((byte) 0);
			}
			for (i = 0; i < readSize; i++) {
				repeatHistory.remove();
				repeatHistory.add(buf[i]);
				if (repeat == null) {
					if ((repeat = Utilities.findRepeat(repeatHistory, minPat, maxPat)) != null) {
						repeatIdx = 0;
						numRepeat = 2;
						repeatPos = i - 2*repeat.length + 1;
					}
				} else {
					if (repeat[repeatIdx] != buf[i]) {
						insertRepeat(repeat, numRepeat, buf, repeatPos);
						repeat = null;
					} else {
						repeatIdx++;
						if (repeatIdx > repeat.length-1) {
							repeatIdx = 0;
							numRepeat++;	
						}
					}
				}
			}
			if (repeat != null) {
				insertRepeat(repeat, numRepeat, buf, repeatPos);
			}
		}
		
		private void insertRepeat(byte[] repeat, int numRepeat, byte[] buf, int repeatPos) {
			if (numRepeat < minRepeats) {
				if (repeatPos == 0) {
				} else if (repeatPos > (buf.length-(repeat.length*minRepeats))) {
					int afterPos = repeatPos + repeat.length*numRepeat;
					int repeatIdx = 0;
					for (int i = afterPos; i < buf.length; i++, repeatIdx++) {
						if (buf[i] != repeat[repeatIdx]) {
							disqualified++;
							return;
						}
					}
				} else {
					disqualified++;
					return;	
				}
			}
			ShortTandemRepeat refKey = bestMatch(repeat, numRepeat, buf, repeatPos);
			if (refKey == null) {
				Set<ShortTandemRepeat> keys = broker.repeatMap.keySet();
				Iterator<ShortTandemRepeat> iter = keys.iterator();
				while (iter.hasNext()) {
					ShortTandemRepeat s = iter.next();
					byte[] keyRepeat = s.getSequence();
					if (s.isLost()) {
						if (Arrays.equals(keyRepeat, repeat)) {
							refKey = s;
							break;
						}
					}
				}
				if (refKey == null) {
					refKey = new ShortTandemRepeat(repeat, numRepeat);	
				}
				lost++;
			} else {
				found++;
			}
			ShortTandemRepeat str = broker.repeatMap.get(refKey);
			if (str == null) {
				str = new ShortTandemRepeat(refKey);
				str.setRepeats(numRepeat);
				broker.repeatMap.put(refKey, str);
			} else {
				str.setRepeats(str.getRepeats()+numRepeat);
			}
		}
	}
	
}
