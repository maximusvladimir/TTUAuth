package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;

public class RaiderFund implements Serializable {
	private static final long serialVersionUID = 1271621828323020642L;
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
