package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public class ScheduleKey implements Serializable {
	private static final long serialVersionUID = -7263703628344903066L;
	
	private String scheduleName;
	private String startDate;
	private String endDate;
	private String termID;
	
	public ScheduleKey(String name, String start, String end) {
		scheduleName = name;
		startDate = start;
		endDate = end;
	}
	
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getScheduleName() {
		return scheduleName;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public void setTermID(String term) {
		termID = term;
	}
	
	public String getTermID() {
		return termID;
	}
	
	@Override
	public int hashCode() {
		return scheduleName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return scheduleName.equals(o);
	}
	
	@Override
	public String toString() {
		return "{\"ScheduleName\": \"" + getScheduleName() + "\", \"StartDate\": \"" + getStartDate() + "\", \"EndDate\": \"" + getEndDate() + "\"}";
	}
}
