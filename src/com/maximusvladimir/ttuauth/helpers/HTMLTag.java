package com.maximusvladimir.ttuauth.helpers;

public class HTMLTag {
	private String html;

	public HTMLTag(String html) {
		this.html = html;
	}

	public String toString() {
		return html;
	}

	public String getAttr(String name) {
		try {
			int index = html.indexOf(name);
			if (index == -1)
				return null;

			String v = html.substring(index + name.length() + 1);
			if (v.length() <= 1)
				return null;

			char end = v.charAt(0);
			v = v.substring(1);
			return v.substring(0, v.indexOf(end));
		} catch (Throwable t) {
			return null;
		}
	}
}