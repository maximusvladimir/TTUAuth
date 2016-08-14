package com.maximusvladimir.ttuauth;

public interface IErrorHandler {
	public abstract void error(Throwable t, String localSource, ErrorType type, String additionalInfo);
}
