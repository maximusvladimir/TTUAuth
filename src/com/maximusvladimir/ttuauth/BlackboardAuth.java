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
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.maximusvladimir.ttuauth.data.GradeNode;
import com.maximusvladimir.ttuauth.data.LoginResult;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.Utility;

/**
 * A general purpose class for accessing the blackboard website.
 */
public class BlackboardAuth implements IAuth {
	private static String LOGIN_PAGE = "https://ttu.blackboard.com/";
	private static String LOGIN2_PAGE = "https://ttu.blackboard.com/webapps/bb-auth-provider-cas-BBLEARN/execute/casLogin?cmd=login&authProviderId=_103_1&redirectUrl=https%3A%2F%2Fttu.blackboard.com%2Fwebapps%2Fportal%2Fexecute%2FdefaultTab";
	private static String STREAM = "https://ttu.blackboard.com/webapps/streamViewer/streamViewer";
	private static String GRADE_PAGE = "https://ttu.blackboard.com/webapps/bb-mygrades-BBLEARN/myGrades?stream_name=mygrades";

	private boolean isLoggedIn = false;
	private ArrayList<Cookie> currCookies;

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
		try {
			HttpURLConnection conn = Utility.getGetConn(GRADE_PAGE
					+ "&course_id=" + classID);
			conn.setRequestProperty("Cookie", Cookie.chain(currCookies));

			html = Utility.read(conn);
			Document doc = Jsoup.parse(html);
			for (Element element : doc.select(".sortable_item_row")) {
				String assignment = element.select(".gradable").text();
				String actDate = element.select(".lastActivityDate").text();
				String act = element.select(".activityType").text();
				String grade = element.select(".cell .grade").text()
						+ element.select(".pointsPossible").text();
				String comments = null;
				Elements els = element.select(".grade-feedback");
				if (els.size() == 1) {
					try {
						Element cel = els.first();
						String val = cel.attr("onClick");
						String id = cel.attr("id");
						val = val
								.replace("mygrades.showInLightBox( '", "")
								.replace(
										"', '<div class=\\\"vtbegenerated\\\">",
										"").replace("</div>', '", "")
								.replace("' );", "").replace(id, "");
						comments = val;
					} catch (Throwable t) {

					}
				}

				Date dt = null;
				SimpleDateFormat parserSDF = new SimpleDateFormat(
						"MMM dd, yyyy HH:mm aa");
				try {
					dt = parserSDF.parse(actDate);
				} catch (ParseException e) {
				}

				GradeNode gn = new GradeNode();
				gn.setActivityDate(dt);
				gn.setActivityType(act);
				gn.setAssignment(assignment);
				gn.setComments(comments);
				gn.setGrade(grade);
				gns.add(gn);
			}
		} catch (IOException t) {
			TTUAuth.logError(t, "getclassgrades", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getclassgradesgeneral", ErrorType.APIChange,
					html);
		}

		return gns;
	}

	/**
	 * Gets a list of courses displayed in black board.
	 * 
	 * @return A dictionary of course ids and course descriptions.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getCurrentClasses() {
		String jsonraw = "";
		Map<String, String> map = new HashMap<String, String>();
		try {
			HttpURLConnection conn = Utility.getPostConn(STREAM);
			conn.setRequestProperty("Cookie", Cookie.chain(currCookies));
			String query = "cmd=loadStream&streamName=mygrades&providers=%7B%7D&forOverview=false";
			conn.setRequestProperty("Content-Length", query.length() + "");
			conn.setInstanceFollowRedirects(false);
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(query);
			dos.close();

			jsonraw = Utility.read(conn);
			String json = jsonraw + "";
			String s = "\"choices\"";
			json = json.substring(json.indexOf(s) + s.length() + 1);
			json = json.substring(0, json.indexOf("}") + 1);
			Gson gson = new Gson();
			map = (Map<String, String>) gson.fromJson(json, map.getClass());
		} catch (IOException t) {
			TTUAuth.logError(t, "getcurrclasses", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "getcurrclassesgeneral", ErrorType.APIChange,
					jsonraw);
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
			Cookie[] loginCookies = auth.getSharedLoginCookies();
			Cookie[] newArray = new Cookie[loginCookies.length + 1];
			System.arraycopy(loginCookies, 0, newArray, 0, loginCookies.length);
			loginCookies = newArray;

			int follows = 0;
			HttpURLConnection conn;
			conn = Utility.getGetConn(LOGIN_PAGE);

			ArrayList<Cookie> cookies = Cookie.getCookies(conn);
			currCookies = cookies;

			conn = Utility.getGetConn(LOGIN2_PAGE);
			conn.setRequestProperty("Cookie", Cookie.chain(cookies));
			conn.setInstanceFollowRedirects(false);
			ArrayList<Cookie> cookies2 = Cookie.getCookies(conn);
			for (int i = 0; i < cookies2.size(); i++) {
				Cookie cookie = cookies2.get(i);
				if (cookie.getKey().startsWith("JSESSION")) {
					cookies.add(cookie);
				}
			}

			String location = "";
			while (Utility.getLocation(conn) != null
					&& follows++ < 15) {
				location = Utility.getLocation(conn);
				HttpURLConnection conn2 = Utility.getGetConn(location);
				conn2.setInstanceFollowRedirects(false);
				if (location.indexOf("ttu.blackboard.com/web") == -1) {
					conn2.setRequestProperty("Cookie",
							Cookie.chain(loginCookies));
				} else {
					conn2.setRequestProperty("Cookie", Cookie.chain(cookies));
				}

				ArrayList<Cookie> cookies3 = Cookie.getCookies(conn2);
				for (int i = 0; i < cookies3.size(); i++) {
					Cookie cookie = cookies3.get(i);
					if (cookie.getKey().startsWith("PHPSESSID")) {
						loginCookies[loginCookies.length - 1] = cookie;
					}
				}
				conn = conn2;
			}
			if (follows >= 13) {
				TTUAuth.logError(new Exception("redirect loop"), "blackboardlogin", ErrorType.Fatal, location);
				return LoginResult.OTHER;
			}
		} catch (IOException e) {
			TTUAuth.logError(e, "blackboardlogin", ErrorType.Fatal);
			return LoginResult.OTHER;
		} catch (Throwable t) {
			TTUAuth.logError(t, "blackboardlogingeneral", ErrorType.Fatal, Cookie.chain(currCookies));
			return LoginResult.OTHER;
		}

		isLoggedIn = true;
		return LoginResult.SUCCESS;
	}
}