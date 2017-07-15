package com.maximusvladimir.ttuauth.helpers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.maximusvladimir.ttuauth.AuthSettings;

public class Utility {
	private static String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	
	public static String safeReplace(String str, String from, String to) {
		if (str == null)
			return str;
		if (str.indexOf(from) != -1)
			str = str.replace(from, to);
		return str;
	}
	
	public static String safeRemove(String str, String needle) {
		return safeReplace(str, needle, "");
	}
	
	public static void writeQuery(HttpURLConnection conn, KeyValue... keyValues) throws IOException {
		String query = "";
		for (int i = 0; i < keyValues.length; i++) {
			KeyValue kv = keyValues[i];
			try {
				query += kv.key + "=" + URLEncoder.encode(kv.value, "UTF-8") + "&";
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (query.endsWith("&")) {
			query = query.substring(0, query.length() - 1);
		}
		conn.setRequestProperty("Content-Length", query.length() + "");
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeBytes(query);
		dos.close();
	}
	
	public static HttpURLConnection getGetConn(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("GET");
		if (AuthSettings.TIMEOUT_CONNECT != 0) {
			conn.setConnectTimeout(AuthSettings.TIMEOUT_CONNECT);
		}
		if (AuthSettings.TIMEOUT_READ != 0) {
			conn.setReadTimeout(AuthSettings.TIMEOUT_READ);
		}
		//conn.setDoOutput(true); this causes a bug on Android.
		conn.setRequestProperty("Accept", ACCEPT);
		conn.setRequestProperty("User-Agent", AuthSettings.USER_AGENT);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
		return conn;
	}
	
	public static HttpURLConnection getPostConn(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("POST");
		if (AuthSettings.TIMEOUT_CONNECT != 0) {
			conn.setConnectTimeout(AuthSettings.TIMEOUT_CONNECT);
		}
		if (AuthSettings.TIMEOUT_READ != 0) {
			conn.setReadTimeout(AuthSettings.TIMEOUT_READ);
		}
		conn.setDoInput(true);
		conn.setDoOutput(true);
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
		
		String loc = locations.get(0);
		if (AuthSettings.LOG_HEADER_LOCATION)
			System.out.println(loc);
		return loc;
	}
	
	public static void sleep(long time) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < time) {
			Thread.yield();
		}
	}
	
	/**
	 * Parses a date in the format:
	 * 2017-07-13T22:48:18-0500
	 * or
	 * 2016-09-06T09:48:16-0500Z
	 * @param str The string to parse. Must NOT be null!!!
	 * @return Returns null on failure.
	 */
	public static Date parseUTCDate(String str) {
		if (str.endsWith("Z")) {
			str = str.substring(0, str.length() - 1);
		}
		try {
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	        return parser.parse(str);
		} catch (Throwable t) {
			return null;
		}
	}
	
	/**
	 * Formats a given date into the format:
	 * 2016-01-31 18:01:20
	 * (yyyy-MM-dd HH:mm:ss)
	 * @param d The date. If null, we will return null.
	 * @return
	 */
	public static String formatDate(Date d) {
		if (d == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(d);
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