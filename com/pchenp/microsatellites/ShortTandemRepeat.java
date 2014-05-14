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
	
	private byte[] pattern;
	private long position;
	private int repeats;
	
	ShortTandemRepeat(byte[] pattern, long position, int repeats) {
		this.pattern = pattern;
		this.position = position;
		this.repeats = repeats;
	}
	
	public byte[] getPattern() {
		return pattern;
	}
	
	public long getPosition() {
		return position;
	}
	
	public int getRepeats() {
		return repeats;
	}
	
	public String toString() {
		String str = "";
		str += new String(pattern) + ' ' + Long.valueOf(position).toString() + ' ' + repeats;
		return str;
	}
}
