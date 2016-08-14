package com.maximusvladimir.ttuauth.data;

public class ScheduleNode {
	private int startHour;
	private int startMin;
	private int endHour;
	private int endMin;
	private String location;
	private String course;
	private DayOfWeek[] daysOfWeek;
	
	public ScheduleNode() {
		
	}

	public int getStartMin() {
		return startMin;
	}

	public void setStartMin(int startMin) {
		this.startMin = startMin;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getEndMin() {
		return endMin;
	}

	public void setEndMin(int endMin) {
		this.endMin = endMin;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public DayOfWeek[] getDaysOfWeek() {
		return daysOfWeek;
	}

	public void setDaysOfWeek(DayOfWeek[] daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}
	
	public String toString() {
		String b = "{\"Start\": \"" + String.format("%02d",startHour) + ":" + String.format("%02d",startMin) + "\", \"End\": \"" +
				String.format("%02d",endHour) + ":" + String.format("%02d",endMin) + "\", \"Location\": \"" + getLocation() + "\", \"Course\": \"" +
				getCourse() + "\", \"DaysOfWeek\": [";
		for (int i = 0; i < daysOfWeek.length; i++) {
			b += "\"" + daysOfWeek[i].name() + "\"";
			if (i != daysOfWeek.length - 1) {
				b += ",";
			}
		}
		b += "]}";
		return b;
	}
}
