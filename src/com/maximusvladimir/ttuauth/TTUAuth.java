package com.maximusvladimir.ttuauth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.maximusvladimir.ttuauth.data.DayOfWeek;
import com.maximusvladimir.ttuauth.data.FinalGradeNode;
import com.maximusvladimir.ttuauth.data.LoginResult;
import com.maximusvladimir.ttuauth.data.ScheduleNode;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.HTMLParser;
import com.maximusvladimir.ttuauth.helpers.HTMLTag;
import com.maximusvladimir.ttuauth.helpers.Utility;

/**
 * A general purpose class for accessing the main TTU website.
 */
public class TTUAuth implements IAuth {
	private static String COOKIE0_PAGE = "https://cas.texastech.edu/sso3.4/login?service=https%3A%2F%2Fportal.texastech.edu%2Fc%2Fportal%2Flogin";
	private static String COOKIE1_PAGE = "https://eraider.ttu.edu/signin.asp?redirect=https%3A%2F%2Fcas.texastech.edu%2Fsso3.4%3FredirectUrl%3D%252Fsso3.4%252Flogin%253Fservice%253Dhttps%25253A%25252F%25252Fportal.texastech.edu%25252Fc%25252Fportal%25252Flogin";
	private static String COOKIE2_PAGE = "https://eraider.ttu.edu/signin.asp?redirect=https%3A%2F%2Fcas.texastech.edu%2Fsso3.4%3FredirectUrl%3D%252Fsso3.4%252Flogin%253Fservice%253Dhttps%25253A%25252F%25252Fportal.texastech.edu%25252Fc%25252Fportal%25252Flogin&jsct=1";
	private static String COOKIE_FG_PAGE = "https://cas.texastech.edu/sso3.4/login?service=https%3A%2F%2Foraapps.texastech.edu%3A443%2Fssomanager%2Fc%2FSSB%3Fpkg%3Dbwskogrd.P_ViewTermGrde";
	private static String COOKIE_ORA_PAGE = "https://oraapps.texastech.edu/ssomanager/c/SSB?pkg=bwskogrd.P_ViewTermGrde";
	private static String LOGIN_PAGE = "https://eraider.ttu.edu/authenticate.asp";
	private static String PORTAL_PAGE = "https://portal.texastech.edu/";
	private static String LOGOUT_PAGE = "https://portal.texastech.edu/c/portal/logout";
	private static String FINAL_GRADE_PAGE = "https://ssb.texastech.edu/TTUSPRD/bwskogrd.P_ViewTermGrde";
	private static String FINAL_GRADE_POST_PAGE = "https://ssb.texastech.edu/TTUSPRD/bwskogrd.P_ViewGrde";
	private static String SCH_PAGE = "https://ssb.texastech.edu/TTUSPRD/bwskfshd.P_CrseSchd";
	private static String GF_LOGIN_REQUEST = "https://ttumsc.gradesfirst.com/home/";
	private static String GF_COOKIE_PAGE = "https://ttumsc.gradesfirst.com/cas/schools/163-texas_tech_university/session/new";
	private static long MAX_LOGIN_TIME = 1000 * 60 * 20;

	private Cookie cookie_aspSessionID;
	private Cookie cookie_sessionID;
	private String redirectTo;
	private String hiddenKey;
	private String hiddenVal;
	private String signInText;

	private Cookie casCookie;
	private Cookie cas2Cookie;
	private Cookie idmCookie;
	private Cookie ssbCookie;
	private Cookie elcCookie;
	private Cookie esiCookie;
	// Warning: only set when the profile image function is
	// queried.
	private Cookie phpCookie;

	private boolean isLoggedIn = false;
	private boolean isSSBLoggedIn = false;
	private String raiderID = null;
	private String name = null;
	private boolean soonToExpire = false;
	private boolean passwordExpired = false;

	private long loginTime = 0;

	private String username;
	private String password;

	private static ArrayList<IErrorHandler> errorHandlers;

	/**
	 * Creates a new instance of the main TTU website auth.
	 * 
	 * @param username
	 *            The username for the account.
	 * @param password
	 *            The password for the account.
	 */
	public TTUAuth(String username, String password) {
		this.username = username;
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
	 * Gets a list of grades for each course for a specific term. Note: you must
	 * call getFinalGradeList() for this to work.
	 * 
	 * @param semesterID
	 *            The term to get the grades for.
	 * @return A list of all the courses.
	 * @throws IOException
	 */
	public ArrayList<FinalGradeNode> getFinalGrade(int semesterID) {
		ArrayList<FinalGradeNode> nodes = new ArrayList<FinalGradeNode>();

		if (!isLoggedIn() || !isSSBLoggedIn)
			return nodes;

		String html = "";

		try {
			HttpURLConnection conn = Utility.getPostConn(FINAL_GRADE_POST_PAGE);
			conn.setRequestProperty("Cookie", ssbCookie.getKey() + "="
					+ ssbCookie.getValue() + "; " + idmCookie.getKey() + "="
					+ idmCookie.getValue());
			conn.setRequestProperty("Origin", "https://ssb.texastech.edu");
			String query = "term_in=" + semesterID;
			conn.setRequestProperty("Content-Length", query.length() + "");

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(query);
			dos.close();

			ArrayList<Cookie> cookies = Cookie.getCookies(conn);
			for (int i = 0; i < cookies.size(); i++) {
				if (cookies.get(i).getKey().startsWith("SESS")) {
					ssbCookie = cookies.get(i);
				}
			}

			html = Utility.read(conn);
			Document doc = Jsoup.parse(html);
			Element dataTable = null;
			for (Element element : doc.select(".datadisplaytable")) {
				Element first = element.select("caption").first();
				if (first != null && first.text().indexOf("Course work") != -1) {
					dataTable = element;
					break;
				}
			}

			for (Element element : dataTable.select("tbody").first()
					.select("tr")) {
				if (element.select("th").size() == 0) {
					FinalGradeNode node = new FinalGradeNode();
					node.setCrn(element.child(0).text());
					node.setSubject(element.child(1).text());
					node.setCourse(element.child(2).text());
					node.setCourseTitle(element.child(4).text());
					node.setCampus(element.child(5).text());
					node.setGrade(element.child(6).text());
					node.setHours(element.child(9).text());
					nodes.add(node);
				}
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "finalgrade", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "finalgradegeneral", ErrorType.APIChange, html);
		}

		return nodes;
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
			HttpURLConnection conn = Utility.getGetConn(GF_COOKIE_PAGE);
			conn.setInstanceFollowRedirects(false);
			
			// https://ttumsc.gradesfirst.com/session/new -> 
			// https://webapps.itsd.ttu.edu/shim/gradesfirst/index.php/login?service=https%3A%2F%2Fttumsc.gradesfirst.com%2Fcas%2Fschools%2F163-texas_tech_university%2Fsession%2Fnew
			String loc = Utility.getLocation(conn);
			
			// https://webapps.itsd.ttu.edu/shim/gradesfirst/index.php/login?service=https%3A%2F%2Fttumsc.gradesfirst.com%2Fcas%2Fschools%2F163-texas_tech_university%2Fsession%2Fnew
			int cycles = 0;
			while (loc.indexOf("/home/") == -1 && cycles++ < 6) {
				conn = Utility.getGetConn(loc);
				conn.setRequestProperty("Cookie", Cookie.chain(elcCookie, new Cookie("ctest", "TRUE"), cookie_aspSessionID, esiCookie, phpCookie, gradeFirstSession));
				conn.setInstanceFollowRedirects(false);
				
				loc = Utility.getLocation(conn);
				ArrayList<Cookie> cookies2 = Cookie.getCookies(conn);
				for (int i = 0; i < cookies2.size(); i++) {
					if (cookies2.get(i).getKey().startsWith("PHPSESSI")) {
						phpCookie = cookies2.get(i);
					}
					if (cookies2.get(i).getKey().startsWith("_gradesfirst_sess")) {
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

	/**
	 * Gets a list of all the available terms for which the final grades can be
	 * pulled.
	 * 
	 * @return A dictionary containing the id of the term and the name of the
	 *         term.
	 * @throws IOException
	 */
	public HashMap<Integer, String> getFinalGradeList() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		if (!isLoggedIn())
			return map;

		String html = "";

		try {
			if (!isSSBLoggedIn) {
				doSSBLogin();
			}

			HttpURLConnection conn = Utility.getGetConn(FINAL_GRADE_PAGE);
			conn.setRequestProperty("Cookie", Cookie.chain(idmCookie));
			conn.setInstanceFollowRedirects(false);

			ArrayList<Cookie> cookies = Cookie.getCookies(conn);
			for (int i = 0; i < cookies.size(); i++) {
				if (cookies.get(i).getKey().startsWith("SESSI")) {
					ssbCookie = cookies.get(i);
				}
			}

			html = Utility.read(conn);

			HTMLParser parser = new HTMLParser(html);
			ArrayList<HTMLTag> tags = parser.getAllByTag("OPTION");
			for (int i = 0; i < tags.size(); i += 2) {
				HTMLTag id = tags.get(i);
				HTMLTag val = tags.get(i + 1);
				map.put(Integer.parseInt(id.getAttr("VALUE")), val.toString()
						.replace("<", "").replace(">", ""));
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "getfinallist", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getfinallistgeneral", ErrorType.APIChange,
					html);
		}

		return map;
	}

	/**
	 * Gets a schedule for the current semester (fall/spring). Summer not
	 * supported.
	 * 
	 * @throws IOException
	 */
	public ArrayList<ScheduleNode> getSchedule() {
		ArrayList<ScheduleNode> nodes = new ArrayList<ScheduleNode>();
		String html = "";
		if (!isLoggedIn())
			return nodes;
		try {
			// let's grab the latest date:
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int month = cal.get(Calendar.MONTH);
			String dt = "";
			if (month >= 5) {
				dt = "09/"
						+ String.format("%02d", Utility.getFirstMonday(
								cal.get(Calendar.YEAR), 8)) + "/"
						+ cal.get(Calendar.YEAR);
			} else {
				dt = "02/"
						+ String.format("%02d", Utility.getFirstMonday(
								cal.get(Calendar.YEAR), 1)) + "/"
						+ cal.get(Calendar.YEAR);
			}

			if (!isSSBLoggedIn) {
				doSSBLogin();
			}

			if (ssbCookie == null) {
				HttpURLConnection conn = Utility.getGetConn(SCH_PAGE);
				conn.setRequestProperty("Cookie", Cookie.chain(idmCookie));
				conn.setInstanceFollowRedirects(false);

				ArrayList<Cookie> cookies = Cookie.getCookies(conn);
				for (int i = 0; i < cookies.size(); i++) {
					if (cookies.get(i).getKey().startsWith("SESSI")) {
						ssbCookie = cookies.get(i);
					}
				}
			}

			HttpURLConnection conn = Utility.getGetConn(SCH_PAGE
					+ "?start_date_in=" + dt);
			conn.setRequestProperty("Cookie",
					Cookie.chain(idmCookie, ssbCookie));
			conn.setInstanceFollowRedirects(false);

			html = Utility.read(conn);
			
			Document doc = Jsoup.parse(html);
			HashMap<String, ArrayList<DayOfWeek>> entries = new HashMap<String, ArrayList<DayOfWeek>>();
			for (Element element : doc.select(".datadisplaytable tr")) {
				for (Element td : element.select(".ddlabel")) {
					Element par = td.parent();
					Elements childs = par.children();
					Collections.reverse(childs);
					int counter = 0;
					for (Element el : childs) {
						if (el.equals(td)) {
							// this is a bit odd... basically the table is
							// reversed,
							// so we have to count backwards.
							counter = 7 - (counter + 1);
							break;
						} else {
							counter++;
						}
					}
					if (td.nodeName().equals("td")) {
						String data = td.child(0).html();
						if (data.indexOf("<br>") != -1) {
							DayOfWeek dow = DayOfWeek.fromInt(counter);
							if (entries.containsKey(data)) {
								entries.get(data).add(dow);
							} else {
								entries.put(data, new ArrayList<DayOfWeek>());
								entries.get(data).add(dow);
							}
						} else {
							// report problem here.
						}
					}
				}
			}

			for (String entry : entries.keySet()) {
				ScheduleNode n = new ScheduleNode();
				String[] splits = entry.split("<br>");
				String courseName = splits[0];
				String time = splits[2];
				String location = splits[3];
				n.setCourse(courseName);
				Object[] dowso = entries.get(entry).toArray();
				DayOfWeek[] dows = new DayOfWeek[dowso.length];
				for (int i = 0; i < dowso.length; i++) {
					dows[i] = (DayOfWeek) dowso[i];
				}
				int[] times = Utility.convertToTime(time);
				if (times != null) {
					n.setStartHour(times[0]);
					n.setStartMin(times[1]);
					n.setEndHour(times[2]);
					n.setEndMin(times[3]);
				}
				n.setDaysOfWeek(dows);
				n.setLocation(location);
				nodes.add(n);
			}
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
			loginTime = System.currentTimeMillis();
			getCASCookie();
			if (!getLoginPage()) {
				return LoginResult.OTHER;
			}
			if (!postLogin(username, password)) {
				return LoginResult.BAD_AUTH;
			}
		} catch (IOException e) {
			TTUAuth.logError(e, "login", ErrorType.Fatal);
			return LoginResult.OTHER;
		} catch (Throwable t) {
			TTUAuth.logError(t, "logingeneral", ErrorType.APIChange);
			return LoginResult.OTHER;
		}

		isLoggedIn = true;
		
		if (soonToExpire)
			return LoginResult.PASSWORD_EXPIRING;
		
		if (passwordExpired) {
			isLoggedIn = false;
			return LoginResult.PASSWORD_EXPIRED;
		}

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
	 * Forces the server to log out the user account.
	 */
	public void logout() {
		if (!isLoggedIn())
			return;

		try {
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
		}
	}

	/**
	 * Forces the status of the login to be refreshed.
	 */
	public void forceCheckLoginStatus() {
		try {
			HttpURLConnection conn = Utility.getGetConn(PORTAL_PAGE);
			conn.setRequestProperty("Cookie", Cookie.chain(cookie_sessionID));

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

	private void doSSBLogin() throws IOException {
		Cookie ora = getORACookie();

		HttpURLConnection conn = Utility.getGetConn(COOKIE_FG_PAGE);
		conn.setRequestProperty("Cookie", Cookie.chain(cas2Cookie, casCookie));
		conn.setInstanceFollowRedirects(false);
		String location = Utility.getLocation(conn);
		if (location == null)
			TTUAuth.logError(null, "ssbloginlocation", ErrorType.Fatal);

		conn = Utility.getGetConn(location);
		conn.setRequestProperty("Cookie", ora.getKey() + "=" + ora.getValue());
		conn.setInstanceFollowRedirects(false);
		ArrayList<Cookie> cookies = Cookie.getCookies(conn);
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().startsWith("IDMSESS")) {
				idmCookie = cookies.get(i);
			}
		}
		isSSBLoggedIn = true;
	}

	Cookie[] getSharedLoginCookies() {
		return new Cookie[] { cookie_aspSessionID, elcCookie,
				new Cookie("ctest", "TRUE"), esiCookie };
	}

	private Cookie getORACookie() throws IOException {
		HttpURLConnection conn = Utility.getGetConn(COOKIE_ORA_PAGE);
		conn.setInstanceFollowRedirects(false);
		ArrayList<Cookie> cookies = Cookie.getCookies(conn);
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().equals("JSESSIONID")) {
				return cookies.get(i);
			}
		}

		return null;
	}

	private void getCASCookie() throws IOException {
		HttpURLConnection conn = Utility.getGetConn(COOKIE0_PAGE);
		conn.setInstanceFollowRedirects(false);
		ArrayList<Cookie> cookies = Cookie.getCookies(conn);
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().startsWith("JSESSIONID")) {
				cookie_sessionID = cookies.get(i);
			}
		}
	}

	private boolean getLoginPage() throws IOException {
		HttpURLConnection conn = Utility.getGetConn(COOKIE1_PAGE);
		ArrayList<Cookie> cookies = Cookie.getCookies(conn);
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getKey().startsWith("ASPSESSION")) {
				cookie_aspSessionID = cookies.get(i);
			}
		}
		if (cookie_aspSessionID == null) {
			// throw new out of date
		}

		conn = Utility.getGetConn(COOKIE2_PAGE);
		conn.setRequestProperty("Cookie",
				Cookie.chain(new Cookie("ctest", "TRUE"), cookie_aspSessionID));
		cookies = Cookie.getCookies(conn);

		HTMLParser parser = new HTMLParser(Utility.read(conn));
		ArrayList<HTMLTag> tags = parser.getAllByTag("input");
		for (int i = 0; i < tags.size(); i++) {
			HTMLTag tag = tags.get(i);
			String name = tag.getAttr("name");
			if (name != null) {
				if (name.equals("redirect")) {
					redirectTo = tag.getAttr("value");
				} else if (name.equals("login")) {
					signInText = tag.getAttr("value");
				} else if (!name.equals("username") && !name.equals("password")) {
					hiddenKey = name;
					hiddenVal = tag.getAttr("value");
				}
			}
		}

		return redirectTo != null;
	}

	private boolean postLogin(String username, String password)
			throws IOException {
		HttpURLConnection conn = Utility.getPostConn(LOGIN_PAGE);
		conn.setRequestProperty("Cookie",
				Cookie.chain(new Cookie("ctest", "TRUE"), cookie_aspSessionID));
		conn.setRequestProperty("Referer", COOKIE1_PAGE);
		String query = "redirect=" + URLEncoder.encode(redirectTo, "UTF-8")
				+ "&" + hiddenKey + "=" + hiddenVal + "&" + "username="
				+ username + "&password="
				+ URLEncoder.encode(password, "UTF-8") + "&login="
				+ URLEncoder.encode(signInText, "UTF-8");
		conn.setRequestProperty("Content-Length", query.length() + "");
		conn.setInstanceFollowRedirects(false);
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeBytes(query);
		dos.close();

		ArrayList<Cookie> cookies = Cookie.getCookies(conn);
		for (int i = 0; i < cookies.size(); i++) {
			Cookie cook = cookies.get(i);
			if (cook.getKey().startsWith("elc")) {
				elcCookie = cook;
			}
			if (cook.getKey().startsWith("esi")) {
				esiCookie = cook;
			}
		}

		if (Utility.read(conn).indexOf("signin.asp?redirect=") != -1) {
			return false;
		}

		int follows = 0;
		soonToExpire = false;
		passwordExpired = false;
		while (conn.getHeaderFields().containsKey("Location") && follows++ < 6) {
			String location = conn.getHeaderFields().get("Location").get(0);
			if (location.indexOf("password.asp?pwdStatus=-1") != -1) {
				passwordExpired = true;
				// returns true so we don't think it's a bad auth.
				return true;
			}
			else if (location.indexOf("password.asp?pwdStatus=") != -1) {
				soonToExpire = true;
				String nextUrl = location.substring(location.indexOf("redirect=") + "redirect=".length());
				nextUrl = java.net.URLDecoder.decode(nextUrl, "UTF-8");
				location = nextUrl;
			}
			HttpURLConnection conn2 = Utility.getGetConn(location);
			conn2.setInstanceFollowRedirects(false);
			String regCookie = Cookie.chain(cookie_sessionID);
			if (location.indexOf("https://cas.texastech.edu/") == -1
					&& location.indexOf("/c/portal/login?ticket") != -1) {
				conn2.setRequestProperty("Cookie",
						"LP_TARGET_URL=https%3A%2F%2Fportal.texastech.edu%2F");
			} else {
				conn2.setRequestProperty("Cookie", regCookie);
			}

			if (location.equals("https://portal.texastech.edu/")) {

				String html = Utility.read(conn2);
				int index = html.indexOf("dockbarDisplayName");
				if (index != -1) {
					String nm = html.substring(index);
					nm = nm.substring(nm.indexOf(">") + 1, nm.indexOf("<"));
					name = nm;
				}
				index = html.indexOf("dockbarRNumber");
				if (index != -1) {
					String nm = html.substring(index);
					nm = nm.substring(nm.indexOf(">") + 1, nm.indexOf("<"));
					raiderID = nm;
				}
			}

			cookies = Cookie.getCookies(conn2);
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getKey().startsWith("JSESSION")) {
					cookie_sessionID = cookie;
					if (location.indexOf("/login?redirectUrl=") != -1) {
						casCookie = cookie;
					}
				}
				if (cookie.getKey().indexOf("CASTGC") != -1) {
					cas2Cookie = cookie;
				}
			}

			conn = conn2;
		}

		return true;
	}
}