/**
 * 
 */
package com.pchenp.microsatellites;

import java.io.Serializable;

/**
 * @author Phillip
 *
 */
public class ShortTandemRepeat implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] chr;
	private byte[] sequence;
	private int repeats;
	private long position;

	ShortTandemRepeat(byte[] sequence, int repeats) {
		this(null, sequence, repeats, -1);
	}
	
	ShortTandemRepeat(byte[] chr, byte[] sequence, int repeats, long position) {
		this.chr = chr;
		this.sequence = sequence;
		this.position = position;
		this.repeats = repeats;
	}
	
	public int getRepeats() {
		return repeats;
	}
	
	public void setRepeats(int repeats) {
		this.repeats = repeats;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (chr != null) {
			sb.append(new String(chr));
			sb.append(',');
		}
		sb.append(new String(sequence));
		sb.append(',');
		sb.append(repeats);
		if (position > -1) {
			sb.append(',');
			sb.append(Long.valueOf(position).toString());
		}
		return sb.toString();
	}
}
