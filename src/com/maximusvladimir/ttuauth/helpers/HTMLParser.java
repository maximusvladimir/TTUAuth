package com.maximusvladimir.ttuauth.helpers;

import java.util.ArrayList;

public class HTMLParser {
	private String input;

	public HTMLParser(String input) {
		this.input = input;
	}

	public ArrayList<HTMLTag> getAllByTag(String tag) {
		ArrayList<HTMLTag> tags = new ArrayList<HTMLTag>();
		char lastChar = input.charAt(0);
		boolean inDoubleQuotes = false;
		boolean inSingleQuotes = false;
		String currentTag = "" + lastChar;
		boolean activeTag = false;
		for (int i = 1; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '"' && inSingleQuotes || c == '\'' && inDoubleQuotes) {
				currentTag += c;
			} else if (c == '"' && inDoubleQuotes) {
				inDoubleQuotes = false;
				currentTag += '"';
			} else if (c == '\'' && inSingleQuotes) {
				inSingleQuotes = false;
				currentTag += "'";
			} else if (c == '"' && !inDoubleQuotes) {
				inDoubleQuotes = true;
				currentTag += '"';
			} else if (c == '\'' && !inSingleQuotes) {
				inSingleQuotes = true;
				currentTag += "'";
			} else if (!inSingleQuotes && !inDoubleQuotes) {
				if (activeTag && c == '>') {
					tags.add(new HTMLTag(currentTag + c));
					activeTag = false;
					currentTag = "";
				}
				if (lastChar == '<') {
					String tgn = "" + c;
					while (i < input.length()) {
						lastChar = c;
						c = input.charAt(++i);
						if (c == ' ' || c == '>') {
							break;
						}
						tgn += c;
					}
					if (tgn.equals(tag)) {
						currentTag = "<" + tgn + c;
						activeTag = true;
					}
					if (tgn.equals("/" + tag)) {
						tags.add(new HTMLTag(currentTag));
						activeTag = false;
						currentTag = "";
					}
				} else {
					currentTag += c;
				}
			} else {
				currentTag += c;
			}
			lastChar = c;
		}

		return tags;
	}
}