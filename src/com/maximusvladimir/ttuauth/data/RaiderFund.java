package com.maximusvladimir.ttuauth.data;

public class RaiderFund {
	private String accountName;
	private String amount;
	
	public RaiderFund() {
		
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String toString() {
		return "{\"AccountName\": \"" + getAccountName() + "\", \"Amount\": \"" + getAmount() + "\"}";
	}
}
