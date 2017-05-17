package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public class FinalGradeNode implements Serializable {
	private static final long serialVersionUID = 3043370717438006721L;
	private String subject;
	private String course;
	private String courseTitle;
	private String crn;
	private String campus;
	private String grade;
	private String hours;
	
	public FinalGradeNode() {
		
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
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
				"\", \"CRN\": \"" + getCrn() + "\", \"Campus\": \"" + getCampus() + "\", \"Grade\": \"" + getGrade() + "\", \"CreditHours\": \"" + 
				getHours() + "\"}";
	}
}
