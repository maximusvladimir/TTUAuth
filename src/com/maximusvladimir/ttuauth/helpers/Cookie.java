package com.maximusvladimir.ttuauth.helpers;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class Cookie {
	private String _key;
	private String _value;
	public Cookie() {
	}
	
	public Cookie(String key, String value) {
		_key = key;
		_value = value;
	}
	
	public String getKey() {
		return _key;
	}
	
	public String getValue() {
		return _value;
	}
	
	public void setKey(String key) {
		_key = key;
	}
	
	public void setValue(String value) {
		_value = value;
	}
	
	public String toString() {
		return _key + ":" + _value;
	}
	
	public static String chain(ArrayList<Cookie> args) {
		if (args == null)
			return "";
		Cookie[] cks = new Cookie[args.size()];
		for (int i = 0; i < cks.length; i++) {
			cks[i] = args.get(i);
		}
		return chain(cks);
	}
	
	public static String chain(Cookie... args) {
		if (args == null)
			return "";
		
		String cookie = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				System.out.println("Danger! Cookie at index " + i + " is null.");
				Thread.dumpStack();
				continue;
				//throw new NullPointerException("Error: Cookie at index " + i + " is null.");
			}
			
			cookie += args[i].getKey() + "=" + args[i].getValue();
			if (i != args.length - 1)
				cookie += ';';
		}
		return cookie;
	}
	
	public void toCookie(HttpURLConnection conn) {
		String str = getKey() + "=" + getValue();
		conn.setRequestProperty("Cookie", str);
	}
	 
	public static ArrayList<Cookie> getCookies(HttpURLConnection conn) {
		ArrayList<Cookie> cookies = new ArrayList<Cookie>();
		
		if (conn == null)
			return cookies;
		
		if (conn.getHeaderFields().containsKey("Set-Cookie")) {
			List<String> cookiesRaw = conn.getHeaderFields().get("Set-Cookie");
			for (int i = 0; i < cookiesRaw.size(); i++) {
				String cookie = cookiesRaw.get(i);
				String name = cookie.substring(0, cookie.indexOf('='));
				String value = cookie.substring(cookie.indexOf('=') + 1);
				if (value.indexOf(";") != -1) {
					value = value.substring(0, value.indexOf(";"));
				}
				cookies.add(new Cookie(name, value));
			}
		}
		
		return cookies;
	}
	
	public static Cookie getCookie(ArrayList<Cookie> cookies, String key) {
		if (cookies == null)
			return null;
		
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().equals(key)) {
				return cookies.get(i);
			}
		}
		return null;
	}
	
	public static Cookie getCookieStartsWith(ArrayList<Cookie> cookies, String startsWith) {
		if (cookies == null)
			return null;
		
		for (int i = 0; i < cookies.size(); i++) {
			//System.out.println(cookies.get(i).getKey() + " " + startsWith);
			if (cookies.get(i).getKey().startsWith(startsWith)) {
				return cookies.get(i);
			}
		}
		return null;
	}
}
