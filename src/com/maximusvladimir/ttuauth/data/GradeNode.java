package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;
import java.util.Date;

public class GradeNode implements Serializable {
	private static final long serialVersionUID = -4595482010310024496L;
	private Date activityDate;
	private String activityType;
	private String assignment;
	private String comments;
	private String grade;
	public GradeNode() {
		
	}
	
	public Date getActivityDate() {
		return activityDate;
	}
	
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	
	public String getAssignment() {
		return assignment;
	}
	
	public void setAssignment(String assignment) {
		this.assignment = assignment;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getActivityType() {
		return activityType;
	}
	
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	
	public String getGrade() {
		return grade;
	}
	
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	/*private Date activityDate;
	private String activityType;
	private String assignment;
	private String comments;
	private String grade;*/
	public String toString() {
		return "{\"ActivityDate\": \"" + getActivityDate() + "\", \"ActivityType\": \"" + getActivityType() + "\", \"AssignmentName\": \"" +
	getAssignment() + "\", \"Comments:\": \"" + getComments() + "\", \"Grade\": \"" + getGrade() + "\"}";
	}
}