/**
 * 
 */
package com.pchenp.microsatellites;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Phillip
 *
 */
public class ShortTandemRepeat implements Serializable, Comparable<ShortTandemRepeat> {
	
	private static final long serialVersionUID = 1L;
	private byte[] chrName;
	private byte[] sequence;
	private int repeats;
	private long position;
	private int chrNum;
	private ArrayList<Byte> before;
	private ArrayList<Byte> after;

	ShortTandemRepeat(byte[] sequence, int repeats) {
		this(null, sequence, repeats, -1, null);
	}
	
	ShortTandemRepeat(byte[] chrName, byte[] sequence, int repeats, long position, ArrayList<Byte> before) {
		this.chrName = chrName;
		this.sequence = sequence;
		this.position = position;
		this.repeats = repeats;
		if (chrName != null) {
			String s = new String(chrName);
			String chrNumStr = "";
			for (int i = s.length()-1; i > 0; i--) {
				char c = s.charAt(i);
				if (Character.isDigit(c)) {
					chrNumStr = c + chrNumStr;
				} else {
					break;
				}
			}
			chrNum = Integer.valueOf(chrNumStr);
		} else {
			chrNum = -1;
		}
		this.before = before;
		this.after = new ArrayList<Byte>();
	}
	
	ShortTandemRepeat(ShortTandemRepeat str) {
		this.chrName = str.chrName;
		this.sequence = str.sequence;
		this.position = str.position;
		this.repeats = str.repeats;
		this.chrNum = str.chrNum;
		this.before = str.before;
		this.after = str.after;
	}
	
	public byte[] getSequence() {
		return sequence;
	}
	
	public int getRepeats() {
		return repeats;
	}
	
	public long getPosition() {
		return position;
	}
	
	public ArrayList<Byte> getBefore() {
		return before;
	}
	
	public ArrayList<Byte> getAfter() {
		return after;
	}
	
	public void setRepeats(int repeats) {
		this.repeats = repeats;
	}
	
	public boolean appendAfter(byte b) {
		if (after.size() >= before.size()) {
			return false;
		}
		after.add(b);
		return true;
	}

	public boolean isLost() {
		if (before == null) {
			return true;
		}
		return false;
	}
	
	public String toSmallString() {
		StringBuilder sb = new StringBuilder();
		if (chrName != null) {
			sb.append(chrNum);
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (chrName != null) {
			sb.append(chrNum);
			sb.append(',');
		}
		sb.append(new String(sequence));
		sb.append(',');
		sb.append(repeats);
		if (position > -1) {
			sb.append(',');
			sb.append(Long.valueOf(position).toString());
		}
		if (before != null && after != null) {
			byte[] beforeArray = new byte[before.size()];
			byte[] afterArray = new byte[after.size()];
			int i;
			for (i = 0; i < before.size(); i++) {
				beforeArray[i] = before.get(i);
			}
			for (i = 0; i < after.size(); i++) {
				afterArray[i] = after.get(i);
			}
			sb.append(',');
			sb.append(new String(beforeArray));
			sb.append(',');
			sb.append(new String(afterArray));	
		}
		return sb.toString();
	}

	@Override
	public int compareTo(ShortTandemRepeat other) {
		if (this.position == -1 || other.position == -1) {
			if (this.position == -1 && other.position == -1) {
				return 0;
			} else if (this.position != -1) {
				return -1;
			} else {
				return 1;
			}
		} else {
			if (this.position > other.position) {
				return 1;
			} else if (this.position < other.position) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
