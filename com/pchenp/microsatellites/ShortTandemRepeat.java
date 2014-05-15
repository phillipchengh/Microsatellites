/**
 * 
 */
package com.pchenp.microsatellites;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

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
	
	ShortTandemRepeat(byte[] chr, byte[] sequence, int repeats, long position) {
		this.chr = chr;
		this.sequence = sequence;
		this.position = position;
		this.repeats = repeats;
	}
	
	public String toString() {
		String str = new String(chr) + ',' + new String(sequence) + ',' + repeats + ',' + Long.valueOf(position).toString();
		return str;
	}
}
