package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public class ScheduleNode implements Serializable {
	private static final long serialVersionUID = 7139292809034429100L;
	
	// the start hour that the course begins on (military).
	private int startHour;
	private int startMin;
	// the end hour that the course ends on (military).
	private int endHour;
	private int endMin;
	private String location;
	// the room in the building that the course will be in. (e.g. 00353)
	private String room;
	// the name of the course. (e.g. ENGL 1302)
	private String course;
	// the full name of the course. (e.g. Advanced College Rhetoric)
	private String title;
	// the course id/section id (e.g. 22780)
	private String courseID;
	// the days of of the week the course is on.
	private DayOfWeek[] daysOfWeek;
	// the name of the primary instructor teaching the course.
	private String instructorNames;
	// the total number of credits the course is.
	private double creditHours;
	
	public ScheduleNode() {
		
	}
	
	public void setInstructor(String instructor) {
		instructorNames = instructor;
	}
	
	public String getInstructor() {
		return instructorNames;
	}
	
	public void setCreditHours(double hours) {
		creditHours = hours;
	}
	
	public double getCreditHours() {
		return creditHours;
	}
	
	public String getRoomNumber() {
		return room;
	}
	
	public void setRoomNumber(String roomNo) {
		room = roomNo;
	}
	
	public String getCourseID() {
		return courseID;
	}
	
	public void setCourseID(String id) {
		courseID = id;
	}
	
	public String getCourseTitle() {
		return title;
	}
	
	public void setCourseTitle(String title) {
		this.title = title;
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

	/**
	 * The building the course will be located in.
	 * Example: MCOM
	 * @param location The building code of the location.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	public String getCourse() {
		return course;
	}

	/**
	 * The short name of the course.
	 * Example: ENGL 1302.
	 * @param
	 */
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
				String.format("%02d",endHour) + ":" + String.format("%02d",endMin) + "\", \"Location\": \"" + getLocation() + "\", \"Room\": \"" + getRoomNumber()
				+ "\", \"Instructor\": \"" + getInstructor() + "\", \"CreditHours\": \"" + getCreditHours() + "\", \"CourseID\": \"" + getCourseID() + "\", \"Course\": \"" +
				getCourse() + "\"";
		if (daysOfWeek != null) {
			b += ", \"DaysOfWeek\": [";
			for (int i = 0; i < daysOfWeek.length; i++) {
				b += "\"" + daysOfWeek[i].name() + "\"";
				if (i != daysOfWeek.length - 1) {
					b += ",";
				}
			}
			b += "]}";
		} else {
			b += "}";
		}
		return b;
	}
}
