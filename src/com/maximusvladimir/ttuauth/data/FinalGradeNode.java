package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Structure for expressing a individual final grade.
 */
public class FinalGradeNode implements Serializable {
	private static final long serialVersionUID = 3043370717438001721L;
	private String section;
	private String course;
	private String courseTitle;
	private String crn;
	private String grade;
	private String hours;
	private Date updated;
	
	public FinalGradeNode() {
		
	}
	
	/**
	 * Gets the date the grade was updated.
	 * Can be null.
	 * @return
	 */
	public Date getUpdatedDate() {
		return updated;
	}
	
	/**
	 * Sets the date the grade was updated.
	 * Can be null.
	 * @param d
	 */
	public void setUpdatedDate(Date d) {
		updated = d;
	}

	/**
	 * Gets the section number (i.e. 001).
	 * @return
	 */
	public String getSection() {
		return section;
	}

	/**
	 * Sets the section number (i.e. 001).
	 * @param subject
	 */
	public void setSection(String section) {
		this.section = section;
	}

	/**
	 * Gets the course name (i.e. POLS 1301).
	 * @return
	 */
	public String getCourse() {
		return course;
	}

	/**
	 * Sets the course name (i.e. POLS 1301).
	 * @param course
	 */
	public void setCourse(String course) {
		this.course = course;
	}

	/**
	 * Gets the full course title (i.e. Introduction to Psychology).
	 * @return
	 */
	public String getCourseTitle() {
		return courseTitle;
	}

	/**
	 * Sets the full course title (i.e. Introduction to Psychology).
	 * @param courseTitle
	 */
	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	/**
	 * Gets the CRN of the course (i.e. 12018).
	 * @return
	 */
	public String getCrn() {
		return crn;
	}

	/**
	 * Sets the CRN of the course (i.e. 12018).
	 * @param crn
	 */
	public void setCrn(String crn) {
		this.crn = crn;
	}

	/**
	 * Gets the number of credit hours the course offers (i.e. 3).
	 * @return
	 */
	public String getHours() {
		return hours;
	}

	/**
	 * Sets the number of credit hours the course offers (i.e. 3).
	 * @param hours
	 */
	public void setHours(String hours) {
		this.hours = hours;
	}

	/**
	 * Gets the grade received in the class (i.e. A).
	 * Known values are "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F", and "DG".
	 * Can be null.
	 * @return
	 */
	public String getGrade() {
		return grade;
	}

	/**
	 * Sets the grade received in the class (i.e. A).
	 * Known values are "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F", and "DG".
	 * Can be null;
	 * @param grade
	 */
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	/**
	 * Converts this Final Grade Node to a JSON string.
	 */
	public String toString() {
		return "{\"Section\": \"" + getSection() + "\", \"Course\": \"" + getCourse() + "\", \"CourseTitle\": \"" + getCourseTitle() + 
				"\", \"CRN\": \"" + getCrn() + "\", \"Grade\": \"" + getGrade() + "\", \"CreditHours\": \"" + 
				getHours() + "\"}";
	}
}
