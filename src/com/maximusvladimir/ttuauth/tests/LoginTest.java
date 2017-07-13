package com.maximusvladimir.ttuauth.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.maximusvladimir.ttuauth.BlackboardAuth;
import com.maximusvladimir.ttuauth.RaiderFundAuth;
import com.maximusvladimir.ttuauth.RateMyProfessor;
import com.maximusvladimir.ttuauth.TTUAuth;
import com.maximusvladimir.ttuauth.data.ScheduleKey;
import com.maximusvladimir.ttuauth.data.ScheduleNode;

public class LoginTest {
	public static final boolean USE_FIDDLER = false;//true;
	private static String keystoreloc = null;
	private static String keystorepassword = null;

	public static void main(String[] args) {
		if (USE_FIDDLER) {
			Properties sysProperties = System.getProperties();
			sysProperties.put("https.proxyHost", "127.0.0.1");
			sysProperties.put("https.proxyPort", "8888");
			sysProperties.put("http.proxyHost", "127.0.0.1");
			sysProperties.put("http.proxyPort", "8889");
			getKeyStoreFromFile();
			System.setProperty("javax.net.ssl.trustStore", keystoreloc);
			System.setProperty("javax.net.ssl.trustStorePassword", keystorepassword);
		}
		
		System.setProperty("https.protocols", "TLSv1.1,TLSv1.2,TLSv1");

		long start = 0, end = 0;
		
		/*if (1 < 2) {
			start = System.currentTimeMillis();
			RateMyProfessor rmp = new RateMyProfessor();
			String pro = "Nakarmi, Upama";
			System.out.println(pro + ": " + rmp.getTeacherRating(pro) + " time: " + (System.currentTimeMillis() - start) + " ms.");
			return;
		}*/

		TTUAuth auth = getAuthFromFile();

		start = System.currentTimeMillis();
		System.out.println(auth.login().toString());
		//System.out.println("Expires in: " + auth.getPasswordExpirationDays() + " days.");
		end = System.currentTimeMillis();

		System.out.println("Login took: " + (end - start) + " ms.");
		
		start = System.currentTimeMillis();
		System.out.println(auth.retrieveProfileImageURL());
		end = System.currentTimeMillis();

		System.out.println("Profile Image: " + (end - start)
				+ " ms.");

		/*start = System.currentTimeMillis();
		RaiderFundAuth rfa = new RaiderFundAuth(auth);
		rfa.login();
		System.out.println(rfa.getRaiderFunds());
		end = System.currentTimeMillis();

		System.out.println("RFA GET took: " + (end - start) + " ms.");
*/
		start = System.currentTimeMillis();
		auth.getFinalGrades();
		end = System.currentTimeMillis();

		System.out.println("Final Grades: " + (end - start) + " ms.");
		
		
		
		start = System.currentTimeMillis();
		HashMap<ScheduleKey, ArrayList<ScheduleNode>> m = auth.getSchedule();
		for (ScheduleKey k : m.keySet()) {
			System.out.println(k.getTermID() + ": ");
			for (ScheduleNode node : m.get(k)) {
				System.out.println("\t" + node.getCourse());
			}
		}
		end = System.currentTimeMillis();

		System.out.println("Schedule: " + (end - start) + " ms.");
/*
		start = System.currentTimeMillis();
		BlackboardAuth bb = new BlackboardAuth(auth);
		bb.login();
		HashMap<String, String> classes = bb.getCurrentClasses();
		for (String classID : classes.keySet()) {
			System.out.println(bb.getClassGrades(classID));
		}
		end = System.currentTimeMillis();

		System.out.println("CLS GET took: " + (end - start) + " ms.");
*/
		//System.out.println(auth.retrieveProfileImageURL());
		
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
	
	private static void getKeyStoreFromFile() {
		try {
			String path = LoginTest.class.getResource("keystore.dat").toString();
			if (path.indexOf("file:/") != -1)
				path = path.replace("file:/", "");
			java.io.FileInputStream fis = new java.io.FileInputStream(path);
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(fis));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (keystoreloc == null) {
					keystoreloc = line;
				} else {
					keystorepassword = line;
				}
			}
			br.close();
		} catch (java.io.FileNotFoundException fnfe) {
			System.err
					.println("You did not add a key store location file to the folder (keystore.dat).");
		} catch (Throwable t) {

		}
	}
}