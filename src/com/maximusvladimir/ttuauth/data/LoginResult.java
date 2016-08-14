package com.maximusvladimir.ttuauth.data;

public enum LoginResult {
	/** 
	 * Occurs when the login has succeeded.
	 */
	SUCCESS,
	/**
	 * Occurs when the username or password is bad.
	 */
	BAD_AUTH,
	/**
	 * Occurs when the password is going to expire soon.
	 */
	PASSWORD_EXPIRING,
	/**
	 * Occurs when the password has expired.
	 */
	PASSWORD_EXPIRED,
	/**
	 * Occurs when the user is logged out of the main TTU site.
	 */
	MAIN_LOGOUT,
	/**
	 * Occurs when some other problem has occurred.
	 * This result is unsuccessful.
	 */
	OTHER
}
