package com.maximusvladimir.ttuauth;

public class AuthSettings {
	/**
	 * The value in milliseconds for how long a connect should time out in.
	 * 0 means no timeout.
	 */
	public static int TIMEOUT_CONNECT = 0;
	/**
	 * The value in milliseconds for how long a read should time out in.
	 * 0 means no timeout;
	 */
	public static int TIMEOUT_READ = 0;
	/**
	 * The user-agent all connections should use.
	 */
	public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/580.36 (KHTML, like Gecko) Chrome/65.0.6228.0 Safari/580.36";
}
