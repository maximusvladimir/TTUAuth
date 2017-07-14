package com.maximusvladimir.ttuauth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.maximusvladimir.ttuauth.data.GradeNode;
import com.maximusvladimir.ttuauth.data.LoginResult;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.Utility;

/**
 * A general purpose class for accessing the blackboard website.
 */
public class BlackboardAuth implements IAuth {
	private static String LOGIN_PAGE = "https://ttu.blackboard.com/";
	private static String LOGIN2_PAGE = "https://webapps.itsd.ttu.edu/shim/bblearn9/index.php/login?service=https%3A%2F%2Fttu.blackboard.com%2Fwebapps%2Fbb-auth-provider-cas-BBLEARN%2Fexecute%2FcasLogin%3Fcmd%3Dlogin%26authProviderId%3D_103_1%26redirectUrl%3Dhttps%253A%252F%252Fttu.blackboard.com%252Fwebapps%252Fportal%252Fexecute%252FdefaultTab%26globalLogoutEnabled%3Dtrue&renew=true";
	private static String STREAM = "https://ttu.blackboard.com/webapps/Bb-mobile-BBLEARN/enrollments?course_type=ALL&include_grades=false&language=en_US&v=1&ver=4.1.4";
	private static String GRADE_PAGE = "https://ttu.blackboard.com/webapps/bb-mygrades-BBLEARN/myGrades?stream_name=mygrades";

	private boolean isLoggedIn = false;
	private Cookie jSessionID, sessionID, sSessionID, webClientCache, otherCookie;

	private TTUAuth auth;

	/**
	 * Creates a new instance of the black board website auth.
	 * 
	 * @param auth
	 *            The main TTU website auth.
	 */
	public BlackboardAuth(TTUAuth auth) {
		this.auth = auth;
	}

	/**
	 * Gets a list of grades from the given course.
	 * 
	 * @param classID
	 *            The id of the course.
	 * @return A list of the grades for the given course.
	 * @throws IOException
	 */
	public ArrayList<GradeNode> getClassGrades(String classID) {
		String html = "";
		ArrayList<GradeNode> gns = new ArrayList<GradeNode>();
		/*try {
			
		} catch (IOException t) {
			TTUAuth.logError(t, "getclassgrades", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getclassgradesgeneral", ErrorType.APIChange,
					html);
		}*/

		return gns;
	}

	/**
	 * Gets a list of courses displayed in black board.
	 * 
	 * @return A dictionary of course ids and course names.
	 * @throws IOException
	 */
	public HashMap<String, String> getCurrentClasses() {
		String xml = "";
		Map<String, String> map = new HashMap<String, String>();
		try {
			HttpURLConnection conn = Utility.getGetConn(STREAM);
			Cookie.setCookies(conn, jSessionID, sessionID, sSessionID, webClientCache, otherCookie);
			xml = Utility.read(conn);
			
			Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
			for (Element e : doc.select("course")) {
				String blackboardID = e.attr("bbid");
				String courseID = e.attr("courseid");
				Date enrollDate = Utility.parseUTCDate(e.attr("enrollmentdate"));
				Date startDate = Utility.parseUTCDate(e.attr("startdateduration"));
				Date endDate = Utility.parseUTCDate(e.attr("enddateduration"));
			    System.out.println(e);
			    String url = "https://ttu.blackboard.com/webapps/Bb-mobile-BBLEARN/courseData?course_section=GRADES&course_id=" + blackboardID + "&rich_content_level=BASIC&language=en_US&v=1&ver=4.1.4";
			    conn = Utility.getGetConn(url);
				Cookie.setCookies(conn, jSessionID, sessionID, sSessionID, webClientCache, otherCookie);
				System.out.println(Utility.read(conn));
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "getcurrclasses", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getcurrclassesgeneral", ErrorType.APIChange,
					xml);
		}

		return (HashMap<String, String>) map;
	}

	/**
	 * Determines if the user is logged in to black board.
	 * 
	 * @return true if logged into black board.
	 */
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * Logs the user into blackboard. Note that user must be logged into TTUAuth
	 * before they can log into blackboard.
	 * 
	 * @return A value indicating whether the login was successful.
	 */
	public LoginResult login() {
		if (!auth.isLoggedIn())
			return LoginResult.MAIN_LOGOUT;

		try {
			String location = "";
			HttpURLConnection conn = Utility.getGetConn(LOGIN_PAGE);
			conn.setInstanceFollowRedirects(false);
			ArrayList<Cookie> bbInitial = Cookie.getCookies(conn);
			for (int i = 0; i < bbInitial.size(); i++) {
				Cookie tmp = bbInitial.get(i);
				if (tmp.getKey().equals("JSESSIONID"))
					jSessionID = tmp;
				else if (tmp.getKey().equals("session_id"))
					sessionID = tmp;
				else if (tmp.getKey().equals("s_session_id"))
					sSessionID = tmp;
				else if (tmp.getKey().equals("web_client_cache_guid"))
					webClientCache = tmp;
				else
					otherCookie = tmp;
			}
			
			// connect to the CAS
			conn = Utility.getGetConn(LOGIN2_PAGE);
			conn.setInstanceFollowRedirects(false);
			if (auth.getPHPCookie() == null) {
				Cookie.setCookies(conn, auth.getELCCookie());
				Cookie phpCookie = Cookie.getCookie(Cookie.getCookies(conn), "PHPSESSID");
				// saves time for Blackboard and retrieveProfileImage().
				auth.setPHPCookie(phpCookie);
				
				conn = Utility.getGetConn(Utility.getLocation(conn));
				Cookie.setCookies(conn, auth.getERaiderCookies());
				conn.setInstanceFollowRedirects(false);
				
				location = Utility.getLocation(conn);
				if (location.startsWith("signin.aspx"))
					location = "https://eraider.ttu.edu/" + location;
				conn = Utility.getGetConn(location);
				conn.setInstanceFollowRedirects(false);
				Cookie.setCookies(conn, auth.getERaiderCookies());
				// might need to set ESI and ELC here. If other areas are bugged, this is why.
			} else {
				/// TODO Find out what happens if we already have the PHP cookie!
			}
			
			// https://webapps.itsd.ttu.edu/shim/bblearn9/index.php?elu=XXXXXXXXXX&elk=XXXXXXXXXXXXXXXX
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			Cookie.setCookies(conn, auth.getPHPCookie(), auth.getELCCookie());
			
			// https://webapps.itsd.ttu.edu/shim/bblearn9/index.php/login?service=https%3A%2F%2Fttu.blackboard.com%2Fwebapps%2Fbb-auth-prov
			// ider-cas-BBLEARN%2Fexecute%2FcasLogin%3Fcmd%3Dlogin%26authProviderId%3D_103_1%26redirectUrl%3Dhttps%253A%252F%252Fttu.blackboard
			// .com%252Fwebapps%252Fportal%252Fexecute%252FdefaultTab%26globalLogoutEnabled%3Dtrue&renew=true
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			Cookie.setCookies(conn, auth.getPHPCookie(), auth.getELCCookie());
			
			// https://ttu.blackboard.com/webapps/bb-auth-provider-cas-BBLEARN/execute/casLogin?cmd=login&authProviderId=_103_1&redirectUrl=https
			// %3A%2F%2Fttu.blackboard.com%2Fwebapps%2Fportal%2Fexecute%2FdefaultTab&globalLogoutEnabled=true&ticket=ST-...
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			Cookie.setCookies(conn, jSessionID, sessionID, sSessionID, webClientCache, otherCookie);
			// sSessionID, sessionID, webClientCache are all reset here, so we need to get those again, then get those again:
			ArrayList<Cookie> bbNext = Cookie.getCookies(conn);
			for (int i = 0; i < bbNext.size(); i++) {
				Cookie tmp = bbNext.get(i);
				if (tmp.getKey().equals("session_id"))
					sessionID = tmp;
				else if (tmp.getKey().equals("s_session_id"))
					sSessionID = tmp;
				else if (tmp.getKey().equals("web_client_cache_guid"))
					webClientCache = tmp;
			}
		} catch (IOException e) {
			TTUAuth.logError(e, "blackboardlogin", ErrorType.Fatal);
			return LoginResult.OTHER;
		} catch (Throwable t) {
			TTUAuth.logError(t, "blackboardlogingeneral", ErrorType.Fatal, Cookie.chain(jSessionID, sessionID, sSessionID, webClientCache, otherCookie));
			return LoginResult.OTHER;
		}

		isLoggedIn = true;
		return LoginResult.SUCCESS;
	}
}
