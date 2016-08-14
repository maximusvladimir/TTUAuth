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
		Cookie[] cks = new Cookie[args.size()];
		for (int i = 0; i < cks.length; i++) {
			cks[i] = args.get(i);
		}
		return chain(cks);
	}
	
	public static String chain(Cookie... args) {
		String cookie = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				continue;
			
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
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().equals(key)) {
				return cookies.get(i);
			}
		}
		return null;
	}
}
