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
	/**
	 * Prints to standard out when a null cookie is passed to Cookie.chain(...). This can be the sign of an underlying problem.
	 */
	public static boolean MESSAGE_ON_NULL_COOKIE = true;
	/**
	 * Throws an exception when a null cookie is passed to Cookie.chain(...). This can be the sign of an underlying problem.
	 * Note: Be careful with enabling this, as some error reporting chains cookies together!
	 */
	public static boolean EXCEPTION_ON_NULL_COOKIE = false;
	/**
	 * Logs all Utility.getLocation results to standard out when turned on.
	 */
	public static boolean LOG_HEADER_LOCATION = false;
}
