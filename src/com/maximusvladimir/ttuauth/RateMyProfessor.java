package com.maximusvladimir.ttuauth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.Utility;

public class RateMyProfessor {
	private static String PRIM = "http://search.mtvnservices.com/typeahead/suggest/?solrformat=true&rows=100&callback=noCB&q=??????+AND+schoolid_s%3A1011&siteName=rmp&rows=20&start=0&fl=pk_id+teacherfirstname_t+teacherlastname_t+total_number_of_ratings_i+averageratingscore_rf+schoolid_s&fq=";
	public RateMyProfessor() {
		
	}
	
	/**
	 * Returns a list of possibly matching professors with the full name as the key
	 * and a double with the value of their rating.
	 * 
	 * If a rating is not set or found, the rating might be 0 or -1. It will not be null.
	 * @param query The string to search for.
	 * @return Will not return null.
	 */
	public Map<String, Double> getTeacherRatingsByQuery(String query) {
		String html = "";
		Map<String, Double> collection = new HashMap<String, Double>();
		try {
			HttpURLConnection conn = Utility.getGetConn(PRIM.replace("??????", URLEncoder.encode(query, "UTF-8")));
			if (Math.random() < 0.5) {
				conn.setRequestProperty("Cookie", Cookie.chain(new Cookie("DNT", "1")));
			}
			html = Utility.read(conn);
			// remove callback functions:
			html = html.substring(html.indexOf("{"));
		    html = html.substring(0, html.lastIndexOf("}") + 1);
			
			JsonElement jelement = new JsonParser().parse(html);
		    JsonObject jobject = jelement.getAsJsonObject();
		    JsonObject response = jobject.getAsJsonObject("response");
		    if (response.get("numFound").getAsInt() == 0)
		    	return collection;
		    
		    for (JsonElement possible : response.getAsJsonArray("docs")) {
		    	JsonObject p = possible.getAsJsonObject();
		    	double score = -1;
		    	if (p.has("averageratingscore_rf") && !p.get("averageratingscore_rf").isJsonNull()) {
		    		try {
		    			score = p.get("averageratingscore_rf").getAsDouble();
		    		} catch (Throwable t22) {}
		    	}
		    	String fn = p.get("teacherfirstname_t").getAsString();
		    	String ln = p.get("teacherlastname_t").getAsString();
		    	if (!fn.endsWith(" ")) {
		    		fn += " ";
		    	}
		    	collection.put(fn + ln, score);
		    }
		} catch (IOException t) {
			TTUAuth.logError(t, "rmp", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "rmpgeneral", ErrorType.APIChange, html);
		}
		
		return collection;
	}
	
	/**
	 * Returns a teacher rating for a given full name.
	 * A "full name" could consitute as any of the following:
	 * John Smith
	 * John A. Smith
	 * John Anthony Smith
	 * Smith, John
	 * Smith, John Anthony
	 * @param fullName
	 * @return If a suitable rating is not found, -1 is returned, otherwise a score between 0 and 5.
	 */
	public Double getTeacherRating(String fullName) {
		Map<String, Double> results = null;
		String newName = "";
		
		if (fullName.indexOf(' ') != -1) {
			// we have a valid "name".
			String[] splits = fullName.split(" ");
			if (splits.length > 1) {
				if (splits[0].indexOf(",") != -1) {
					String tmp = splits[0].replace(",", "");
					for (int i = 1; i < splits.length; i++) {
						splits[i - 1] = splits[i];
					}
					splits[splits.length - 1] = tmp;
				}
			}
			
			if (splits.length == 2) {
				// this one is easy.
				newName = splits[0] + " " + splits[1];
			}
			if (splits.length == 3) {
				// this one is a bit tricky because we might want to include the middle name as
				// part of the first name (?)
				newName = splits[0] + " " + splits[2];
			}
			results = getTeacherRatingsByQuery(newName);
		}
		
		if (results != null) {
			for (String name : results.keySet()) {
				if (name.equals(fullName) || name.equals(newName)) {
					// easy pea-sy
					return results.get(name);
				}
			}
		}
		
		return -1.0;
	}
}
