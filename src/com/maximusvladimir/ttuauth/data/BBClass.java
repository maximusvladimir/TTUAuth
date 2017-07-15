package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;
import java.util.Date;

import com.maximusvladimir.ttuauth.helpers.Utility;

/**
 * Contains information about a course on Blackboard.
 */
public class BBClass implements Serializable {
	private static final long serialVersionUID = -6842569435209778799L;

	private String bbID, courseID;
	private Date enrollDate, startDate, endDate;
	
	public BBClass() {
		
	}
	
	/**
	 * Sets the ID of the Blackboard course. It typically contains weird formating (i.e. _71718_1).
	 * @param bbID
	 */
	public void setBlackboardID(String bbID) {
		this.bbID = bbID;
	}
	
	/**
	 * Gets the ID of the Blackboard course. It typically contains weird formating (i.e. _71718_1).
	 * @return
	 */
	public String getBlackboardID() {
		return bbID;
	}
	
	/**
	 * Sets the course ID of the Blackboard course (i.e. 201627-MATH-1301-001).
	 * @param courseID
	 */
	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}
	
	/**
	 * Gets the course ID of the Blackboard course (i.e. 201627-MATH-1301-001).
	 * @return
	 */
	public String getCourseID() {
		return courseID;
	}
	
	/**
	 * Sets the date that the course was added to Blackboard.
	 * @param enrollDate
	 */
	public void setEnrollDate(Date enrollDate) {
		this.enrollDate = enrollDate;
	}
	
	/**
	 * Gets the date that the course was added to Blackboard.
	 * @return
	 */
	public Date getEnrollDate() {
		return enrollDate;
	}
	
	/**
	 * Sets the date that the course will open/start on Blackboard.
	 * @param startDate
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Gets the date that the course will open/start on Blackboard.
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Sets the date that the course will close/potentially disappear on Blackboard.
	 * @param endDate
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Gets the date that the course will close/potentially disappear on Blackboard.
	 * @param endDate
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	@Override
	public String toString() {
		return "{\"BlackboardID\": \"" + getBlackboardID() + "\", \"CourseID\": \"" + getCourseID() + "\", \"EnrollDate\": \""
				+ Utility.formatDate(getEnrollDate()) + "\", \"StartDate\": \"" + Utility.formatDate(getStartDate()) + "\", \"EndDate\": \""
				+ Utility.formatDate(getEndDate()) + "\"}";
	}
}
