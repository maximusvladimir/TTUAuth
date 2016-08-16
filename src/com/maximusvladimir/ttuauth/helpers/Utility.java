package com.maximusvladimir.ttuauth.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.maximusvladimir.ttuauth.AuthSettings;

public class Utility {
	private static String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	
	public static HttpURLConnection getGetConn(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setConnectTimeout(AuthSettings.TIMEOUT_CONNECT);
		conn.setReadTimeout(AuthSettings.TIMEOUT_READ);
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", ACCEPT);
		conn.setRequestProperty("User-Agent", AuthSettings.USER_AGENT);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
		return conn;
	}
	
	public static HttpURLConnection getPostConn(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setConnectTimeout(AuthSettings.TIMEOUT_CONNECT);
		conn.setReadTimeout(AuthSettings.TIMEOUT_READ);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", ACCEPT);
		conn.setRequestProperty("User-Agent", AuthSettings.USER_AGENT);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		return conn;
	}
	
	public static String getLocation(HttpURLConnection conn) {
		Map<String, List<String>> headers = conn.getHeaderFields();
		if (!headers.containsKey("Location"))
			return null;
		
		List<String> locations = headers.get("Location");
		if (locations == null || locations.size() < 1)
			return null;
		
		return locations.get(0);
	}
	
	public static String read(HttpURLConnection conn) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}
	
	public static void readByte(HttpURLConnection conn) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		rd.read();
		rd.close();
	}
	
	private static Calendar cacheCalendar;

	public static int getFirstMonday(int year, int month) {
		if (cacheCalendar == null) {
			cacheCalendar = Calendar.getInstance();
		}
		
	    cacheCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
	    cacheCalendar.set(Calendar.MONTH, month);
	    cacheCalendar.set(Calendar.YEAR, year);
	    return cacheCalendar.get(Calendar.DATE);
	}
	
	public static String safeRemove(String str, String removal) {
		if (str == null || removal == null)
			return null;
		
		if (str.indexOf(removal) == -1)
			return str;
		
		return str.replace(removal, "");
	}
	
	public static int[] convertToTime(String str) {
		if (str == null || str.length() <= 1)
			return null;
		
		if (str.indexOf("-") == -1)
			return null;
		
		int[] times = new int[4];
		
		str = str.toLowerCase();
		
		String[] splits = str.split("-");
		
		String start = safeRemove(splits[0], " ");
		boolean isAM = start.indexOf("am") != -1;
		start = safeRemove(start, "am");
		start = safeRemove(start, "pm");
		if (start.indexOf(":") == -1)
			return null;
		String[] pieces = start.split(":");
		try {
			times[0] = Integer.parseInt(pieces[0]);
			times[1] = Integer.parseInt(pieces[1]);
		} catch (Throwable t) {
			return null;
		}
		if (!isAM && times[0] != 12) {
			times[0] += 12;
		}
		
		String end = safeRemove(splits[1], " ");
		isAM = end.indexOf("am") != -1;
		end = safeRemove(end, "am");
		end = safeRemove(end, "pm");
		if (end.indexOf(":") == -1)
			return null;
		pieces = end.split(":");
		try {
			times[2] = Integer.parseInt(pieces[0]);
			times[3] = Integer.parseInt(pieces[1]);
		} catch (Throwable t) {
			return null;
		}
		if (!isAM && times[2] != 12) {
			times[2] += 12;
		}
		
		return times;
	}
}
