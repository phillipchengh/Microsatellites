package com.pchenp.microsatellites;

public class RepeatReader {

	abstract public static class Builder<T extends Builder<T>> {
		private int bufSize = Default.BUF_SIZE;
		private int readSize = Default.SHORT_READ_LEN;
		private int coverage = Default.COVERAGE;
		private int minPat = Default.MIN_PAT_LEN;
		private int maxPat = Default.MAX_PAT_LEN;
		private int minRepeats = Default.MIN_REPEATS;
		
		abstract protected T self();
		
		public RepeatReader build() {
			return new RepeatReader(this);
		}
		
		public T bufSize(int bufSize) {
			this.bufSize = bufSize;
			return self();
		}
		
		public T readSize(int readSize) {
			this.readSize = readSize;
			return self();
		}
		
		public T coverage(int coverage) {
			this.coverage = coverage;
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
	}

	private static class Builder2 extends Builder<Builder2> {

		@Override
		protected Builder2 self() {
			return this;
		}
	}
	
	public static Builder<?> builder() {
		return new Builder2();
	}

	protected final int bufSize;
	protected final int readSize;
	protected final int coverage;
	protected final int minPat;
	protected final int maxPat;
	protected final int minRepeats;

	protected RepeatReader(Builder<?> b) {
		bufSize = b.bufSize;
		readSize = b.readSize;
		coverage = b.coverage;
		minPat = b.minPat;
		maxPat = b.maxPat;
		minRepeats = b.minRepeats;
	}
}