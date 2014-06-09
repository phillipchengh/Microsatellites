package com.pchenp.microsatellites;

abstract public class RepeatMapper {

	abstract public static class MBuilder<T extends MBuilder<T>> {
		private int bufSize = Const.DEF_BUF;
		private int readSize = Const.DEF_READ;
		private int minPat = Const.DEF_MIN_PAT;
		private int maxPat = Const.DEF_MAX_PAT;
		private int minRepeats = Const.DEF_MIN_REPEATS;
		private boolean stats = Const.STATS;
		
		abstract protected T self();

		abstract public RepeatMapper build();
		
		public T bufSize(int bufSize) {
			this.bufSize = bufSize;
			return self();
		}

		public T readSize(int readSize) {
			this.readSize = readSize;
			return self();
		}
		
		public T minPat(int minPat) {
			this.minPat = minPat;
			return self();
		}
		
		public T maxPat(int maxPat) {
			this.maxPat = maxPat;
			return self();
		}
		
		public T minRepeats(int minRepeats) {
			this.minRepeats = minRepeats;
			return self();
		}
		
		public T stats(boolean stats) {
			this.stats = stats;
			return self();
		}

	}

	protected final int bufSize;
	protected final int readSize;
	protected final int minPat;
	protected final int maxPat;
	protected final int minRepeats;
	protected final boolean stats;
	
	protected RepeatMapper(MBuilder<?> b) {
		bufSize = b.bufSize;
		readSize = b.readSize;
		minPat = b.minPat;
		maxPat = b.maxPat;
		minRepeats = b.minRepeats;
		stats = b.stats;
	}
}
