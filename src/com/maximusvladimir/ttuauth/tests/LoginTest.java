package com.maximusvladimir.ttuauth.tests;

import java.util.HashMap;
import java.util.Properties;

import com.maximusvladimir.ttuauth.BlackboardAuth;
import com.maximusvladimir.ttuauth.RaiderFundAuth;
import com.maximusvladimir.ttuauth.TTUAuth;

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

		long start = 0, end = 0;
		
		TTUAuth auth = getAuthFromFile();

		start = System.currentTimeMillis();
		auth.login();
		end = System.currentTimeMillis();
		
		System.out.println("Login took: " + (end - start) + " ms.");

		start = System.currentTimeMillis();
		RaiderFundAuth rfa = new RaiderFundAuth(auth);
		rfa.login();
		System.out.println(rfa.getRaiderFunds());
		end = System.currentTimeMillis();
		
		System.out.println("RFA GET took: " + (end - start) + " ms.");

		start = System.currentTimeMillis();
		HashMap<Integer, String> grades = auth.getFinalGradeList();
		for (Integer i : grades.keySet()) {
			System.out.println("ID: " + i);
			System.out.println(auth.getFinalGrade(i));
		}
		System.out.println(auth.getSchedule());
		end = System.currentTimeMillis();
		
		System.out.println("FINAL GRADE + SCHEDULE GET took: " + (end - start)
				+ " ms.");

		start = System.currentTimeMillis();
		BlackboardAuth bb = new BlackboardAuth(auth);
		bb.login();
		HashMap<String, String> classes = bb.getCurrentClasses();
		for (String classID : classes.keySet()) {
			System.out.println(bb.getClassGrades(classID));
		}
		end = System.currentTimeMillis();
		
		System.out.println("CLS GET took: " + (end - start) + " ms.");

		auth.logout();
	}
	
	private static TTUAuth getAuthFromFile() {
		try {
			String path = LoginTest.class.getResource("cred.dat").toString();
			if (path.indexOf("file:/") != -1)
				path = path.replace("file:/", "");
			java.io.FileInputStream fis = new java.io.FileInputStream(path);
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(fis));
			String line = null;
			String username = null;
			String password = null;
			while ((line = br.readLine()) != null) {
				if (username == null) {
					username = line;
				} else {
					password = line;
				}
			}
			br.close();

			return new TTUAuth(username, password);
		} catch (java.io.FileNotFoundException fnfe) {
			System.err
					.println("You did not add a credential file to the folder (cred.dat).");
		} catch (Throwable t) {

		}
		return null;
	}
}