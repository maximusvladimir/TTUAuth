package com.maximusvladimir.ttuauth;

import com.maximusvladimir.ttuauth.data.LoginResult;

public interface IAuth {
	/**
	 * Logs into the Auth service.
	 * @return The result of the login.
	 */
	public abstract LoginResult login();
	
	/**
	 * Is the log in active?
	 * @return A value indicating whether the auth is logged in.
	 */
	public abstract boolean isLoggedIn();
}
