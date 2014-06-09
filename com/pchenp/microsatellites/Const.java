package com.pchenp.microsatellites;

/**
 * @author Phillip
 * 
 */
public class Const {
	/**
	 * Forgive me for the bad names/values. 
	 * DEF_: default
	 * MIN_: minimum
	 * MAX_: maximum
	 */
	public static final int DEF_BUF = 65536;
	public static final int MIN_BUF = 1;
	public static final int MAX_BUF = Integer.MAX_VALUE;
	public static final int DEF_READ = 30;
	public static final int MIN_READ = 1;
	public static final int MAX_READ = Integer.MAX_VALUE;
	public static final int DEF_COVERAGE = 1;
	public static final int MIN_COVERAGE = 1;
	public static final int MAX_COVERAGE = Integer.MAX_VALUE;
	public static final int DEF_MIN_REPEATS = 4;
	public static final int MIN_MIN_REPEATS = 2;
	public static final int MAX_MIN_REPEATS = Integer.MAX_VALUE;
	public static final int DEF_MIN_PAT = 2;
	public static final int DEF_MAX_PAT = 5;
	public static final int MIN_MIN_PAT = 2;
	public static final int MAX_MAX_PAT = Integer.MAX_VALUE;
	public static final int DEF_THREADS = 4;
	public static final int MIN_THREADS = 2;
	public static final int MAX_THREADS = 64;
	public static final double DEF_MIN_SCORE = 0.5;
	public static final double MIN_MIN_SCORE = 0;
	public static final double MAX_MIN_SCORE = 1;
	public static final boolean STATS = false;
	public static final byte[] BASES = {(byte) 'A', (byte) 'C', (byte) 'G', (byte) 'T'};
	public static final int MAX_READ_LEN = 3000000;
}
