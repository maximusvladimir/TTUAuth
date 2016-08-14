package com.maximusvladimir.ttuauth.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.maximusvladimir.ttuauth.BlackboardAuth;
import com.maximusvladimir.ttuauth.RaiderFundAuth;
import com.maximusvladimir.ttuauth.TTUAuth;
import com.maximusvladimir.ttuauth.data.RaiderFund;

public class LoginTest {
	public static void main(String[] args) {
		Properties sysProperties = System.getProperties();
		sysProperties.put("https.proxyHost", "127.0.0.1");
		sysProperties.put("https.proxyPort", "8888");
		sysProperties.put("http.proxyHost", "127.0.0.1");
		sysProperties.put("http.proxyPort", "8889");
		System.setProperty("javax.net.ssl.trustStore",
				"C:\\Program Files\\Java\\jre1.8.0_91\\lib\\security\\FiddlerKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		long start = System.currentTimeMillis();	
		TTUAuth auth = new TTUAuth();
		auth.login("", "");
		long end = System.currentTimeMillis();
		System.out.println("Login took: " + (end - start) + " ms.");
		
		auth.logout();
		
		if (3 == 3)
			return;
		
		start = System.currentTimeMillis();
		RaiderFundAuth rfa = new RaiderFundAuth();
		rfa.login(auth);
		try {
			System.out.println(rfa.getRaiderFunds());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println("RFA GET took: " + (end - start) + " ms.");

		start = System.currentTimeMillis();
		try {
			auth.getFinalGradeList();
			System.out.println(auth.getFinalGrade(201527));
			System.out.println(auth.getFinalGrade(201557));
			System.out.println(auth.getFinalGrade(201627));
			System.out.println(auth.getFinalGrade(201657));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(auth.getSchedule());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println("FINAL GRADE + SCHEDULE GET took: " + (end - start) + " ms.");

		start = System.currentTimeMillis();
		BlackboardAuth bb = new BlackboardAuth();
		bb.login(auth);

		try {
			bb.getCurrentClasses();
			System.out.println(bb.getClassGrades("_30474_1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println("CLS GET took: " + (end - start) + " ms.");

		auth.logout();
	}
}