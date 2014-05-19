package com.pchenp.microsatellites;

public class RepeatMapper {

	abstract public static class Builder<T extends Builder<T>> {
		private int bufSize = Default.BUF_SIZE;
		private int minPat = Default.MIN_PAT_LEN;
		private int maxPat = Default.MAX_PAT_LEN;
		private int minRepeats = Default.MIN_REPEATS;
		
		abstract protected T self();
		
		public RepeatMapper build() {
			return new RepeatMapper(this);
		}
		
		public T bufSize(int bufSize) {
			this.bufSize = bufSize;
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
	protected final int minPat;
	protected final int maxPat;
	protected final int minRepeats;
	
	protected RepeatMapper(Builder<?> b) {
		bufSize = b.bufSize;
		minPat = b.minPat;
		maxPat = b.maxPat;
		minRepeats = b.minRepeats;
	}
}
