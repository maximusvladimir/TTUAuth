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
	 * Occurs when some other problem has occurred.
	 */
	OTHER,
	/**
	 * Occurs when the session has expired.
	 */
	SESSION_EXPIRED
}
