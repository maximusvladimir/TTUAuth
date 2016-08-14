package com.maximusvladimir.ttuauth;

public enum ErrorType {
	/**
	 * Occurs when the error requires a reconstruction of the TTUAuth instance.
	 */
	Fatal,
	/**
	 * Occurs when an error has occured, but TTUAuth can still continue to be used.
	 */
	Severe,
	/**
	 * Occurs when an error is probably about to occur due to a site change.
	 */
	APIChange,
	/**
	 * Only used internally.
	 */
	None
}
