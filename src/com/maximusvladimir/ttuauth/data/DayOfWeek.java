package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public enum DayOfWeek implements Serializable {
	Mon,
	Tue,
	Wed,
	Thu,
	Fri,
	Sat,
	Sun,
	Unk;
	
	public static DayOfWeek fromInt(int val) {
		switch (val) {
		case 2:
			return Mon;
		case 3:
			return Tue;
		case 4:
			return Wed;
		case 5:
			return Thu;
		case 6:
			return Fri;
		case 7:
			return Sat;
		case 1:
			return Sun;
		}
		return Unk;
	}
}
