package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public class FinalGradeNode implements Serializable {
	private static final long serialVersionUID = 3043370717438001721L;
	private String section;
	private String course;
	private String courseTitle;
	private String crn;
	private String grade;
	private String hours;
	
	public FinalGradeNode() {
		
	}

	public String getSubject() {
		return section;
	}

	public void setSubject(String subject) {
		this.section = subject;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public String toString() {
		return "{\"Subject\": \"" + getSubject() + "\", \"Course\": \"" + getCourse() + "\", \"CourseTitle\": \"" + getCourseTitle() + 
				"\", \"CRN\": \"" + getCrn() + "\", \"Grade\": \"" + getGrade() + "\", \"CreditHours\": \"" + 
				getHours() + "\"}";
	}
}
