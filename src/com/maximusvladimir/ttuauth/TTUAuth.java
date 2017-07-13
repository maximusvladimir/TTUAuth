package com.maximusvladimir.ttuauth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maximusvladimir.ttuauth.data.DayOfWeek;
import com.maximusvladimir.ttuauth.data.FinalGradeNode;
import com.maximusvladimir.ttuauth.data.FinalGradeTerm;
import com.maximusvladimir.ttuauth.data.LoginResult;
import com.maximusvladimir.ttuauth.data.ScheduleKey;
import com.maximusvladimir.ttuauth.data.ScheduleNode;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.HTMLParser;
import com.maximusvladimir.ttuauth.helpers.HTMLTag;
import com.maximusvladimir.ttuauth.helpers.KeyValue;
import com.maximusvladimir.ttuauth.helpers.Utility;

/**
 * A general purpose class for accessing the main TTU website.
 */
public class TTUAuth implements IAuth {
	private static String PORTAL_PAGE = "https://portal.texastech.edu/";
	private static String LOGOUT_PAGE = "https://portal.texastech.edu/c/portal/logout";
	private static String FINAL_GRADE_PAGE = "https://ssb.texastech.edu/TTUSPRD/bwskogrd.P_ViewTermGrde";
	private static String FINAL_GRADE_POST_PAGE = "https://ssb.texastech.edu/TTUSPRD/bwskogrd.P_ViewGrde";
	private static String FINAL_GRADE_NEW = "https://mobile.texastech.edu/ProdTTU-banner-mobileserver/api/2.0/grades/";
	private static String SCH_AUTH = "https://mobile.texastech.edu/ProdTTU-banner-mobileserver/api/2.0/security/validate-web-auth";
	private static String SCH_PAGE = "https://mobile.texastech.edu/ProdTTU-banner-mobileserver/api/2.0/courses/overview/";
	private static String GF_COOKIE_PAGE = "https://ttumsc.gradesfirst.com/cas/schools/163-texas_tech_university/session/new";
	private static long MAX_LOGIN_TIME = 1000 * 60 * 20;

	// Direct portal login:
	private static String PORTAL_CAS_LOGIN = "https://cas.texastech.edu:443/sso3.4/login?service=https%3A%2F%2Fportal.texastech.edu%2Fc%2Fportal%2Flogin";
	// we need this to get a Cookie: ASPSESSIONIDCAABBRRS
	private static String ERAIDER_CAS_LOGIN = "https://eraider.ttu.edu/signin.asp?redirect=https%3A%2F%2Fcas.texastech.edu%2Fsso3.4%3FredirectUrl%3D%252Fsso3.4%252Flogin%253Fservice%253Dhttps%25253A%25252F%25252Fportal.texastech.edu%25252Fc%25252Fportal%25252Flogin";
	// we need this to push us to the federated sign in page. This could be merged with the previous (?). From here, grab the Location, then post to it.
	private static String ERAIDER_CAS_LOGIN_FEDERATE = "https://eraider.ttu.edu/signin.aspx?redirect=https%3A%2F%2Fcas.texastech.edu%2Fsso3.4%3FredirectUrl%3D%252Fsso3.4%252Flogin%253Fservice%253Dhttps%25253A%25252F%25252Fportal.texastech.edu%25252Fc%25252Fportal%25252Flogin";
	
	// Cookie for initial TTUAuth login. Do not use after that.
	private Cookie casInitialCookie;
	// Cookie for eRaider Auth system (TTUAuth login).
	private Cookie casERaider;
	// Cookie for if the portal needs to be re-retrieved:
	private Cookie cookiePortal;
	// Cookie from the result of the federated login:
	private Cookie cookieMSISAuth;
	// eRaider.ttu.edu generates these. These might be used by later connections:
	private Cookie cookieFedAuth;
	private Cookie cookieFedAuth1;
	private Cookie cookieELC;
	private Cookie cookieESI;
	private Cookie cookieASPNET_SESSIONID;
	private Cookie casCookie;
	private Cookie cookieMobile;
	
	
	// Used for sites accessing webapps.itsd.ttu.edu
	private Cookie phpCookie;

	private boolean isLoggedIn = false;
	private boolean isSSBLoggedIn = false;
	private String raiderID = null;
	private String name = null;
	private boolean soonToExpire = false;
	private boolean passwordExpired = false;
	private int expireDays = -1;

	private long loginTime = 0;

	private String username;
	private String password;

	private static ArrayList<IErrorHandler> errorHandlers;

	/**
	 * Creates a new instance of the main TTU website auth.
	 * 
	 * @param username
	 *            The username for the account. Can start with or without ttu\\username.
	 * @param password
	 *            The password for the account.
	 */
	public TTUAuth(String username, String password) {		
		this.username = username.toLowerCase();
		this.password = password;

		if (errorHandlers == null)
			errorHandlers = new ArrayList<IErrorHandler>();
	}

	/**
	 * Determines if the user has been logged in yet or not. Will return false
	 * if the main website login has expired.
	 * 
	 * @return true if the user is logged in.
	 */
	public boolean isLoggedIn() {
		return isLoggedIn
				&& (System.currentTimeMillis() - loginTime) < MAX_LOGIN_TIME;
	}

	/**
	 * Gets the URL for the profile image. This is the image on the student ID.
	 * Notice this function is <b>sync</b>. Do NOT expect the value to be returned
	 * immediately (or even quickly for that matter).
	 * 
	 * DANGER: May invalidate the blackboard session (if the blackboard session has
	 * already been acquired)!
	 * @return A url including the http or https portion or null.
	 */
	public String retrieveProfileImageURL() {
		if (!isLoggedIn())
			return null;
		
		String html = "";
		Cookie gradeFirstSession = null;
		
		try {
			//HttpURLConnection conn = Utility.getGetConn(GF_COOKIE_PAGE);
			//conn.setInstanceFollowRedirects(false);
			
			// https://ttumsc.gradesfirst.com/session/new -> 
			// https://webapps.itsd.ttu.edu/shim/gradesfirst/index.php/login?service=https%3A%2F%2Fttumsc.gradesfirst.com%2Fcas%2Fschools%2F163-texas_tech_university%2Fsession%2Fnew
			HttpURLConnection conn = null;
			String loc = "https://webapps.itsd.ttu.edu/shim/gradesfirst/index.php/login?service=https%3A%2F%2Fttumsc.gradesfirst.com%2Fcas%2Fschools%2F163-texas_tech_university%2Fsession%2Fnew";
			
			// https://webapps.itsd.ttu.edu/shim/gradesfirst/index.php/login?service=https%3A%2F%2Fttumsc.gradesfirst.com%2Fcas%2Fschools%2F163-texas_tech_university%2Fsession%2Fnew
			int cycles = 0;
			while (loc.indexOf("/home/") == -1 && cycles++ < 6) {
				conn = Utility.getGetConn(loc);
				if (loc.indexOf("signin.asp") != -1) {
					conn.setRequestProperty("Cookie", Cookie.chain(getERaiderCookies()));
				} else {
					if (loc.indexOf("ttumsc.gradesfirst.com/") == -1) {
						if (phpCookie == null)
							conn.setRequestProperty("Cookie", Cookie.chain(cookieELC));
						else
							conn.setRequestProperty("Cookie", Cookie.chain(cookieELC, phpCookie));
					} else if (gradeFirstSession != null) {
						conn.setRequestProperty("Cookie", Cookie.chain(gradeFirstSession));
					}
				}
				conn.setInstanceFollowRedirects(false);
				
				ArrayList<Cookie> cookies2 = Cookie.getCookies(conn);
				if (phpCookie == null && loc.indexOf("webapps.itsd.ttu.edu") != -1) {
					phpCookie = Cookie.getCookie(cookies2, "PHPSESSID");
				}
				
				loc = Utility.getLocation(conn);
				if (!loc.startsWith("http"))
					loc = "https://eraider.ttu.edu/" + loc;
				
				for (int i = 0; i < cookies2.size(); i++) {
					if (cookies2.get(i).getKey().startsWith("PHPSESSI") && phpCookie == null) {
						phpCookie = cookies2.get(i);
					}
					if (cookies2.get(i).getKey().startsWith("_gradesfirst_sess") && gradeFirstSession == null) {
						gradeFirstSession = cookies2.get(i);
					}
				}
			}
			
			if (loc.indexOf("/home/") != -1) {
				loc = Utility.getLocation(conn);
				if (loc == null || loc.equals("")) {
					TTUAuth.logError(null, "profileimagegradefirstlocation9null", ErrorType.Severe);
					return null;
				}
				conn = Utility.getGetConn(loc);
				conn.setRequestProperty("Cookie", Cookie.chain(gradeFirstSession));
				conn.setInstanceFollowRedirects(false);
				html = Utility.read(conn);
				Document doc = Jsoup.parse(html);
				String url = null;
				for (Element element : doc.select(".profile_picture")) {
					for (Element el1 : element.children()) {
						url = el1.attr("src");
					}
					if (url != null)
						break;
				}
				return url;
			} else {
				// abnormal termination
				TTUAuth.logError(null, "profileimageabnormalterm", ErrorType.Severe);
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "profileimage", ErrorType.Severe);
		} catch (Throwable t) {
			TTUAuth.logError(t, "profileimageother", ErrorType.Severe, html);
		}
		
		return null;
	}

	public Object getFinalGrades() {
		String html = "";
		if (!isLoggedIn())
			return null;
		try {
			if (cookieMobile == null) {
				cookieMobile = doMobileLogin();
			}
			
			HttpURLConnection conn = Utility.getGetConn(FINAL_GRADE_NEW + getRaiderID().toUpperCase());
	    	conn.setInstanceFollowRedirects(false);
	    	conn.setRequestProperty("Cookie", Cookie.chain(cookieMobile));
	    	
	    	html = Utility.read(conn);
	    	
	    	JsonElement jelement = new JsonParser().parse(html);
		    JsonObject jobject = jelement.getAsJsonObject();
		    JsonArray terms = jobject.getAsJsonArray("terms");
			for (JsonElement termEl : terms) {
				JsonObject term = termEl.getAsJsonObject();
				/*"id": "201527",
      "name": "Fall 2014 TTU",
      "startDate": "2014-08-11",
      "endDate": "2014-12-10",
      "sections": [*/
				//ScheduleKey key = new ScheduleKey(term.get("name").getAsString(), term.get("startDate").getAsString(), term.get("endDate").getAsString());
				//key.setTermID(term.get("id").getAsString());
				FinalGradeTerm fgt = new FinalGradeTerm();
				fgt.EndDate = term.get("endDate").getAsString();
				fgt.ID = term.get("id").getAsString();
				fgt.Name = term.get("name").getAsString();
				fgt.StartDate = term.get("startDate").getAsString();
				System.out.println(fgt);
				
				ArrayList<FinalGradeNode> sections = new ArrayList<FinalGradeNode>();
				for (JsonElement courseEl : term.get("sections").getAsJsonArray()) {
					JsonObject section = courseEl.getAsJsonObject();
					
					/*String title = section.get("courseTitle").getAsString();
					String course = section.get("courseName").getAsString();
					String courseID = section.get("sectionId").getAsString();*/
				}
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "getfgnew", ErrorType.Fatal);
		}
		/*} catch (Throwable t) {
			TTUAuth.logError(t, "getfgnewgeneral", ErrorType.APIChange, html);
		}*/

		return null;
	}
	
	/***
	 * Performs a mobile API login. This allows piggybacking onto the Mobile
	 * app's API.
	 * @return A valid mobile.texastech.edu cookie or null.
	 * @throws IOException
	 */
	private Cookie doMobileLogin() throws IOException {
		HttpURLConnection conn = Utility.getGetConn(SCH_AUTH);
		conn.setInstanceFollowRedirects(false);
		Cookie mobile = Cookie.getCookie(Cookie.getCookies(conn), "JSESSIONID");
		String location = conn.getHeaderFields().get("Location").get(0);
		int counter = 0;
		HttpURLConnection getNext = null;
		while (counter++ < 10) {
			if (location.startsWith("signin.aspx"))
				location = "https://eraider.ttu.edu/" + location;
			getNext = Utility.getGetConn(location);
			getNext.setInstanceFollowRedirects(false);
			//System.out.println(casCookie);
			getNext.setRequestProperty("Cookie", Cookie.chain(casCookie));//, cookieELC, cookieESI, cookieMSISAuth));
			String header = Utility.getLocation(getNext);
			if (header != null) {
				location = header;
				if (location.endsWith("ProdTTU-banner-mobileserver/")) {
					break;
				}
			} else {
				break;
			}
		}
		if (getNext == null)
			return null;
		
		mobile = Cookie.getCookie(Cookie.getCookies(getNext), "JSESSIONID");
		
		return mobile;
	}
	
	/**
	 * Gets Cookies for using things on eraider.ttu.edu. Note: Only use
	 * once login() completes successfully.
	*/
	public Cookie[] getERaiderCookies() {
		return new Cookie[] { cookieFedAuth, cookieFedAuth1, cookieESI, cookieELC, cookieASPNET_SESSIONID, casERaider };
	}
	
	/**
	 * Gets a mapping of terms and the corresponding courses for
	 * each term.
	 * 
	 * @throws IOException
	 */
	public HashMap<ScheduleKey, ArrayList<ScheduleNode>> getSchedule() {
		HashMap<ScheduleKey, ArrayList<ScheduleNode>> nodes = new HashMap<ScheduleKey, ArrayList<ScheduleNode>>();
		String html = "";
		if (!isLoggedIn())
			return nodes;
		try {
			if (cookieMobile == null) {
				cookieMobile = doMobileLogin();
			}
			
			
			HttpURLConnection conn = Utility.getGetConn(SCH_PAGE + getRaiderID().toUpperCase());
	    	conn.setInstanceFollowRedirects(false);
	    	conn.setRequestProperty("Cookie", Cookie.chain(cookieMobile));
	    	
	    	html = Utility.read(conn);
	    	
	    	JsonElement jelement = new JsonParser().parse(html);
		    JsonObject jobject = jelement.getAsJsonObject();
		    JsonArray terms = jobject.getAsJsonArray("terms");
			for (JsonElement termEl : terms) {
				JsonObject term = termEl.getAsJsonObject();
				ScheduleKey key = new ScheduleKey(term.get("name").getAsString(), term.get("startDate").getAsString(), term.get("endDate").getAsString());
				key.setTermID(term.get("id").getAsString());
				
				ArrayList<ScheduleNode> sections = new ArrayList<ScheduleNode>();
				for (JsonElement courseEl : term.get("sections").getAsJsonArray()) {
					JsonObject section = courseEl.getAsJsonObject();
					
					String title = section.get("courseDescription").getAsString();
					String course = section.get("courseName").getAsString();
					String courseID = section.get("sectionId").getAsString();
					double creditHours = 0.0;
					if (section.has("credits") && !section.get("credits").isJsonNull())
						creditHours = section.get("credits").getAsDouble();
					
					String instructorName = "";
					for (JsonElement instructorEl : section.get("instructors").getAsJsonArray()) {
						JsonObject instructor = instructorEl.getAsJsonObject();
						if (instructor.has("primary")) {
							if (instructor.get("primary").getAsBoolean()) {
								instructorName = instructor.get("firstName").getAsString();
								if (instructor.has("middleInitial") && !instructor.get("middleInitial").isJsonNull()) {
									String middle = instructor.get("middleInitial").getAsString();
									instructorName += " " + middle + (middle.length() == 1 ? "." : "");
								}
								instructorName += " " + instructor.get("lastName").getAsString();
								break;
							}
						}
					}
					
					String room = "";
					String location = "";
					int startHour = 0, startMinute = 0, endHour = 0, endMinute = 0;
					DayOfWeek[] dow = null;
					
					JsonArray meetings = section.get("meetingPatterns").getAsJsonArray();
					if (meetings.size() > 0) {
						JsonObject meeting = meetings.get(0).getAsJsonObject();
						if (meeting.has("buildingId") && !meeting.get("buildingId").isJsonNull()) {
							location = meeting.get("buildingId").getAsString();
						}
						if (meeting.has("room") && !meeting.get("room").isJsonNull()) {
							room = meeting.get("room").getAsString();
						}
						if (meeting.has("sisStartTimeWTz") && !meeting.get("sisStartTimeWTz").isJsonNull()) {
							String timeStart = meeting.get("sisStartTimeWTz").getAsString();
							timeStart = timeStart.substring(0, timeStart.indexOf(" "));
						    String hour = timeStart.substring(0, timeStart.indexOf(":"));
						    String minute = timeStart.substring(timeStart.indexOf(":") + 1);
						    try {
						    	startHour = Integer.parseInt(hour);
						    	startMinute = Integer.parseInt(minute);
						    } catch (Throwable t) { }
						}
						if (meeting.has("sisEndTimeWTz") && !meeting.get("sisEndTimeWTz").isJsonNull()) {
							String timeEnd = meeting.get("sisEndTimeWTz").getAsString();
							timeEnd = timeEnd.substring(0, timeEnd.indexOf(" "));
						    String hour = timeEnd.substring(0, timeEnd.indexOf(":"));
						    String minute = timeEnd.substring(timeEnd.indexOf(":") + 1);
						    try {
						    	endHour = Integer.parseInt(hour);
						    	endMinute = Integer.parseInt(minute);
						    } catch (Throwable t) { }
						}
						if (meeting.has("daysOfWeek") && !meeting.get("daysOfWeek").isJsonNull()) {
							JsonArray meetingDays = meeting.get("daysOfWeek").getAsJsonArray();
							dow = new DayOfWeek[meetingDays.size()];
							int iter = 0;
							for (JsonElement meetingDayEl : meetingDays) {
								dow[iter++] = DayOfWeek.fromInt(meetingDayEl.getAsInt());
							}
						}
					}
					
					ScheduleNode node = new ScheduleNode();
					node.setCourse(course);
					node.setCourseID(courseID);
					node.setCourseTitle(title);
					node.setCreditHours(creditHours);
					node.setInstructor(instructorName);
					node.setLocation(location);
					node.setStartHour(startHour);
					node.setStartMin(startMinute);
					node.setEndHour(endHour);
					node.setEndMin(endMinute);
					node.setDaysOfWeek(dow);
					node.setRoomNumber(room);
					
					sections.add(node);
				}
				nodes.put(key, sections);
			}
			
			// sort the terms.
			Comparator<ScheduleKey> comparator = new Comparator<ScheduleKey>() {
				@Override
				public int compare(ScheduleKey arg0, ScheduleKey arg1) {
					return arg0.getTermID().compareTo(arg1.getTermID());
				}
			};
			
			TreeMap<ScheduleKey, ArrayList<ScheduleNode>> tmp = new TreeMap<ScheduleKey, ArrayList<ScheduleNode>>(comparator);
			tmp.putAll(nodes);
			HashMap<ScheduleKey, ArrayList<ScheduleNode>> nodes2 = new HashMap<ScheduleKey, ArrayList<ScheduleNode>>();
			nodes2.putAll(tmp);
			nodes = nodes2;
		} catch (IOException t) {
			TTUAuth.logError(t, "getsch", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getschgeneral", ErrorType.APIChange, html);
		}

		return nodes;
	}

	/**
	 * Performs an eRaider login.
	 * 
	 * @return A value indicating the state of the login.
	 */
	public LoginResult login() {
		try {
			casCookie = null;
			
			HttpURLConnection conn = Utility.getGetConn(PORTAL_CAS_LOGIN);
			conn.setRequestProperty("Referer", "https://portal.texastech.edu/");
			conn.setInstanceFollowRedirects(false);
			casInitialCookie = Cookie.getCookie(Cookie.getCookies(conn), "JSESSIONID");
			
			
			conn = Utility.getGetConn(ERAIDER_CAS_LOGIN);
			conn.setRequestProperty("Referer", PORTAL_CAS_LOGIN);
			conn.setInstanceFollowRedirects(false);
			casERaider = Cookie.getCookieStartsWith(Cookie.getCookies(conn), "ASPSESS");
			
			
			conn = Utility.getGetConn(ERAIDER_CAS_LOGIN_FEDERATE);
			conn.setRequestProperty("Referer", ERAIDER_CAS_LOGIN);
			conn.setRequestProperty("Cookie", Cookie.chain(casERaider));
			conn.setInstanceFollowRedirects(false);
			String federatedSignOn = Utility.getLocation(conn);
			
			
			// push the username and password:
			conn = Utility.getPostConn(federatedSignOn);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Referer", ERAIDER_CAS_LOGIN_FEDERATE);
			Utility.writeQuery(conn, new KeyValue("UserName", username.startsWith("ttu\\") ? username : "ttu\\" + username),
					new KeyValue("Password", password), new KeyValue("AuthMethod", "FormsAuthentication"));
			// and let's grab the MSIS cookie:
			cookieMSISAuth = Cookie.getCookie(Cookie.getCookies(conn), "MSISAuth");
			String federatedRedirect = Utility.getLocation(conn);
			if (federatedRedirect == null)
				return LoginResult.BAD_AUTH;
			
			
			conn = Utility.getGetConn(federatedRedirect);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cookieMSISAuth));
			// we have to grab form data :(
			String formData = Utility.read(conn);
			Document doc = Jsoup.parse(formData);
			KeyValue wa = null, wresult = null, wctx = null;
			for (Element element : doc.select("input")) {
				String name = element.attr("name").toLowerCase();
				if (name.equals("wa"))
					wa = new KeyValue("wa", element.attr("value"));
				if (name.equals("wresult"))
					wresult = new KeyValue("wresult", element.attr("value"));
				if (name.equals("wctx"))
					wctx = new KeyValue("wctx", element.attr("value"));
			}
			
			
			conn = Utility.getPostConn("https://eraider.ttu.edu/");
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Referer", federatedRedirect);
			conn.setRequestProperty("Cookie", Cookie.chain(casERaider));
			Utility.writeQuery(conn, wa, wresult, wctx);
			ArrayList<Cookie> cookies = Cookie.getCookies(conn);
			loginTime = System.currentTimeMillis();
			cookieFedAuth = Cookie.getCookie(cookies, "FedAuth");
			cookieFedAuth1 = Cookie.getCookie(cookies, "FedAuth1");
			
			
			conn = Utility.getGetConn(ERAIDER_CAS_LOGIN_FEDERATE);
			conn.setRequestProperty("Cookie", Cookie.chain(casERaider, cookieFedAuth, cookieFedAuth1));
			conn.setInstanceFollowRedirects(false);
			cookies = Cookie.getCookies(conn);
			cookieELC = Cookie.getCookie(cookies, "elc");
			cookieESI = Cookie.getCookie(cookies, "esi");
			cookieASPNET_SESSIONID = Cookie.getCookie(cookies, "ASP.NET_SessionId");
			String next = Utility.getLocation(conn);
			
			
			while (next != null && next.indexOf("cas.texastech.edu/sso") != -1) {
				HttpURLConnection tmp = Utility.getGetConn(next);
				tmp.setInstanceFollowRedirects(false);
				tmp.setRequestProperty("Cookie", Cookie.chain(casCookie == null ? casInitialCookie : casCookie));
				
				cookies = Cookie.getCookies(tmp);
				if (cookies.size() > 0) {
					if (Cookie.getCookie(cookies, "JSESSIONID") != null) {
						casCookie = Cookie.getCookie(cookies, "JSESSIONID");
					}
				}
				next = Utility.getLocation(tmp);
			}
			
			
			conn = Utility.getGetConn(next);
			conn.setInstanceFollowRedirects(false);
			cookiePortal = Cookie.getCookie(Cookie.getCookies(conn), "JSESSIONID");
			
			
			conn = Utility.getGetConn(PORTAL_PAGE);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cookiePortal));
			String html = Utility.read(conn);
			doc = Jsoup.parse(html);
			String strRaiderID = doc.select("#dockbarRNumber").text();
			String strName = doc.select("#dockbarDisplayName").text();
			raiderID = strRaiderID;
			name = strName;
		} catch (IOException e) {
			TTUAuth.logError(e, "login", ErrorType.Fatal);
			return LoginResult.OTHER;
		} catch (Throwable t) {
			TTUAuth.logError(t, "logingeneral", ErrorType.APIChange);
			return LoginResult.OTHER;
		}

		isLoggedIn = true;
		
		return LoginResult.SUCCESS;
	}

	/**
	 * Gets the full name of the user. Note: must be logged in to query. This
	 * may not be available immediately after login. Add a full login handler to
	 * see if it is ready.
	 * 
	 * @return
	 */
	public String getFullName() {
		return name;
	}

	/**
	 * Gets the RaiderID of the user. Note: must be logged in to query. This may
	 * not be available immediately after login. Add a full login handler to see
	 * if it is ready.
	 * 
	 * @return
	 */
	public String getRaiderID() {
		return raiderID;
	}
	
	/**
	 * Gets the number of days before the user's password expires.
	 * @return -1 if the password has already expired or if it doesn't expire soon.
	 */
	public int getPasswordExpirationDays() {
		return expireDays;
	}

	/**
	 * Forces the server to log out the user account.
	 */
	public void logout() {
		if (!isLoggedIn())
			return;

		/*try {
			HttpURLConnection conn = Utility.getGetConn(LOGOUT_PAGE);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cookie_sessionID));

			conn = Utility.getGetConn(conn.getHeaderFields().get("Location")
					.get(0));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie",
					Cookie.chain(casCookie, cas2Cookie));
			Utility.readByte(conn);

			conn = Utility.getGetConn(conn.getHeaderFields().get("Location")
					.get(0));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cookie_aspSessionID,
					new Cookie("ctest", "TRUE"), elcCookie, esiCookie));
			Utility.readByte(conn);
		} catch (IOException t) {
			TTUAuth.logError(t, "logout", ErrorType.Severe);
		} catch (Throwable t) {
			TTUAuth.logError(t, "logoutgeneral", ErrorType.Severe);
		}*/
	}

	/**
	 * Forces the status of the login to be refreshed.
	 */
	public void forceCheckLoginStatus() {
		try {
			HttpURLConnection conn = Utility.getGetConn(PORTAL_PAGE);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cookiePortal));

			String location = Utility.getLocation(conn);
			if (location != null && location.indexOf("cas.texastech.edu") != -1) {
				isLoggedIn = false;
			}
		} catch (IOException e) {
			TTUAuth.logError(e, "forcechecklogin", ErrorType.Fatal);
		}
	}

	/**
	 * Adds a new error handler to the library.
	 * 
	 * @param err
	 *            The error handler.
	 */
	public static void attachErrorHandler(IErrorHandler err) {
		errorHandlers.add(err);
	}
	
	/**
	 * Logs an error and fires the error handlers.
	 * 
	 * @param t
	 *            The exception.
	 * @param localSource
	 *            The source of the error. This is manually inputted.
	 * @param etype
	 *            How severe the error is.
	 * @param additionalInfo
	 *            Any other information about the error.
	 */
	public static void logError(Throwable t, String localSource, ErrorType etype) {
		logError(t, localSource, etype, "none");
	}

	/**
	 * Logs an error and fires the error handlers.
	 * 
	 * @param t
	 *            The exception.
	 * @param localSource
	 *            The source of the error. This is manually inputted.
	 * @param etype
	 *            How severe the error is.
	 * @param additionalInfo
	 *            Any other information about the error.
	 */
	public static void logError(Throwable t, String localSource,
			ErrorType etype, String additionalInfo) {
		
		String occurance = new Date().toString();
		
		String stack = "";
		if (t != null) {
			stack = "{";
			StackTraceElement[] els = t.getStackTrace();
			for (int i = 0; i < els.length; i++) {
				StackTraceElement ste = els[i];
				stack += ste.toString();
				if (i != els.length - 1) {
					stack += "-> ";
				}
			}
			stack += "}";
		}
		
		System.err.println("An error has occured"
				+ (t != null ? " \"" + t.getCause() + "\". " : ". ")
				+ (t != null ? " \"" + t.getMessage() + "\". " : ". ")
				+ (localSource != null ? "Source: " + localSource + ". " : "")
				+ (etype != ErrorType.None ? "Severity: " + etype.name() + ". "
						: "") + "Additional info: \"" + additionalInfo + "\". "
				+ (!stack.equals("") ? "Stack: " + stack + ". " : "")
				+ "Occurred: " + occurance + ".");
		
		for (int i = 0; i < errorHandlers.size(); i++) {
			errorHandlers.get(i).error(t, localSource, etype, additionalInfo);
		}
	}
}