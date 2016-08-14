package com.maximusvladimir.ttuauth.data;

public enum DayOfWeek {
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
		case 0:
			return Mon;
		case 1:
			return Tue;
		case 2:
			return Wed;
		case 3:
			return Thu;
		case 4:
			return Fri;
		case 5:
			return Sat;
		case 6:
			return Sun;
		}
		return Unk;
	}
}
