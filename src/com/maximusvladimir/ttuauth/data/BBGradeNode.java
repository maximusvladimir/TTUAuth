package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;
import java.util.Date;

import com.maximusvladimir.ttuauth.helpers.Utility;

public class BBGradeNode implements Serializable {
	private static final long serialVersionUID = -4598173210310024122L;
	
	private String name, bbid;
	private Double grade = null, possiblePoints = null, median = null, mean = null;
	private Date lastInstructorActivity, lastStudentActivity, dueDate;
	private String comments = "";
	
	public BBGradeNode() {
		
	}

	/**
	 * Gets the Blackboard Unique Identifier of the grade node (i.e. _XXXXXX_1).
	 * This is NOT the course Blackboard ID.
	 * @return the bbid
	 */
	public String getBlackboardID() {
		return bbid;
	}

	/**
	 * Sets the Blackboard Unique Identifier of the grade node (i.e. _XXXXXX_1).
	 * This is NOT the course Blackboard ID.
	 * @param bbid the bbid to set
	 */
	public void setBlackboardID(String bbid) {
		this.bbid = bbid;
	}

	/**
	 * Gets the name of the assignment (i.e. HW 2).
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the assignment (i.e. HW 2).
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the maximum number of points that the assignment has (i.e. 100).
	 * @return the possiblePoints. Can be null.
	 */
	public Double getPossiblePoints() {
		return possiblePoints;
	}

	/**
	 * Sets the maximum number of points that the assignment has (i.e. 100).
	 * @param possiblePoints the possiblePoints to set
	 */
	public void setPossiblePoints(Double possiblePoints) {
		this.possiblePoints = possiblePoints;
	}

	/**
	 * Gets the class median for this assignment (i.e. 81).
	 * @return the median. Can be null.
	 */
	public Double getClassMedian() {
		return median;
	}

	/**
	 * Sets the class median for this assignment (i.e. 81).
	 * @param median the median to set
	 */
	public void setClassMedian(Double median) {
		this.median = median;
	}

	/**
	 * Gets the class average for this assignment (i.e. 65).
	 * @return the mean
	 */
	public Double getClassMean() {
		return mean;
	}

	/**
	 * Sets the class average for this assignment (i.e. 65).
	 * @param mean the mean to set
	 */
	public void setClassMean(Double mean) {
		this.mean = mean;
	}

	/**
	 * Gets the grade that the logged in user recieved for the assignment.
	 * @return the grade. Will be null if the grade hasn't been entered.
	 */
	public Double getGrade() {
		return grade;
	}

	/**
	 * Sets the grade that the logged in user recieved for the assignment.
	 * @param grade the grade to set
	 */
	public void setGrade(Double grade) {
		this.grade = grade;
	}

	/**
	 * Gets the date the last time the instructor interacted with this assignment.
	 * This is typically when the instructor has graded an assignment or when an instructor
	 * has posted the assignment or made it visible.
	 * @return the lastInstructorActivity
	 */
	public Date getLastInstructorActivityDate() {
		return lastInstructorActivity;
	}

	/**
	 * Sets the date the last time the instructor interacted with this assignment.
	 * This is typically when the instructor has graded an assignment or when an instructor
	 * has posted the assignment or made it visible.
	 * @param lastInstructorActivity the lastInstructorActivity to set
	 */
	public void setLastInstructorActivityDate(Date lastInstructorActivity) {
		this.lastInstructorActivity = lastInstructorActivity;
	}

	/**
	 * Gets the date the student last interacted with this assignment. Typically this is only
	 * set when the logged in user uploaded an electronic assignment to Blackboard.
	 * @return the lastStudentActivity
	 */
	public Date getLastStudentActivityDate() {
		return lastStudentActivity;
	}

	/**
	 * Sets the date the student last interacted with this assignment. Typically this is only
	 * set when the logged in user uploaded an electronic assignment to Blackboard.
	 * @param lastStudentActivity the lastStudentActivity to set
	 */
	public void setLastStudentActivityDate(Date lastStudentActivity) {
		this.lastStudentActivity = lastStudentActivity;
	}

	/**
	 * Gets the date that this assignment was due.
	 * @return the dueDate
	 */
	public Date getDueDate() {
		return dueDate;
	}

	/**
	 * Sets the date that this assignment was due.
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Gets comments associated with this grade.
	 * @return the comments. If there are no comments, this returns an EMPTY string (NOT null).
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * Sets comments associated with this grade. This should be stripped of HTML and all BOMs.
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		if (comments == null)
			this.comments = "";
		else
			this.comments = comments;
	}
	
	/**
	 * Gets this Blackboard grade node formatted as a JSON object.
	 */
	public String toString() {
		return "{\"BlackboardID\": \"" + getBlackboardID() + "\", \"Name\": \"" + getName() + "\", \"PossiblePoints\": " +
				getPossiblePoints() + ", \"Comments:\": \"" + Utility.safeReplace(getComments(), "\n", "\\n") + "\", \"Grade\": " + getGrade() +
				", \"ClassMedian\": " + getClassMedian() + ", \"ClassAverage\": " + getClassMean() + ", \"LastInstructorActivity\": \"" +
				Utility.formatDate(getLastInstructorActivityDate()) + "\", \"LastStudentActivityDate\": \"" + 
				Utility.formatDate(getLastStudentActivityDate()) + "\", \"DueDate\": \"" + Utility.formatDate(getDueDate()) + "\"}";
		
	}
}