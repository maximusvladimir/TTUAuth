package com.maximusvladimir.ttuauth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.maximusvladimir.ttuauth.data.LoginResult;
import com.maximusvladimir.ttuauth.data.RaiderFund;
import com.maximusvladimir.ttuauth.helpers.Cookie;
import com.maximusvladimir.ttuauth.helpers.Utility;

public class RaiderFundAuth implements IAuth {
	private static String FUND_LOGIN = "https://webapps.itsd.ttu.edu/shim/getfunds/index.php/login?service=https%3A%2F%2Fget.cbord.com%2Fraidercard%2Ffull%2Flogin.php";
	private static String FUND_COOKIE = "https://get.cbord.com/raidercard/full/login.php";
	private static String CAS1_LOGIN = "https://eraider.ttu.edu/signin.asp?redirect=https%3A%2F%2Fwebapps.itsd.ttu.edu%2Fshim%2Fgetfunds%2Findex.php";
	private static String FUND_INDEX = "https://get.cbord.com/raidercard/full/index.php";
	private static String FUND_HOME = "https://get.cbord.com/raidercard/full/funds_home.php";
	private static String FUND_OVERVIEW = "https://get.cbord.com/raidercard/full/funds_overview_partial.php";

	private Cookie awseCookie;
	private Cookie phpCookie;

	private boolean loggedIn = false;

	private TTUAuth auth;

	/**
	 * Creates a new instance of the RaiderFundAuth.
	 * 
	 * @param auth
	 *            The TTU main website auth token.
	 */
	public RaiderFundAuth(TTUAuth auth) {
		this.auth = auth;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	/**
	 * Gets a list of account balances for meal plans.
	 * 
	 * @return A list of balances and names.
	 * @throws IOException
	 */
	public ArrayList<RaiderFund> getRaiderFunds() {
		ArrayList<RaiderFund> funds = new ArrayList<RaiderFund>();
		
		if (!auth.isLoggedIn() || !isLoggedIn())
			return funds;
		
		String html = "";
		try {
			HttpURLConnection conn = Utility.getGetConn(FUND_HOME);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(awseCookie,
					phpCookie, new Cookie("no_login_guest_user", "")));
			html = Utility.read(conn);
			int index = html.indexOf("getOverview");
			String userID = html.substring(index);
			userID = userID.substring(userID.indexOf("(") + 2,
					userID.indexOf(")") - 1);
			String token = "formToken\"";
			index = html.indexOf(token);
			index += token.length() + 1;
			String form = html.substring(index);
			form = form.substring(form.indexOf("\"") + 1);
			form = form.substring(0, form.indexOf("\""));

			conn = Utility.getPostConn(FUND_OVERVIEW);
			String query = "userId=" + userID + "&formToken=" + form;
			conn.setRequestProperty("Content-Length", query.length() + "");
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Referer",
					"https://get.cbord.com/raidercard/full/funds_home.php");
			conn.setRequestProperty("Cookie", Cookie.chain(awseCookie,
					phpCookie, new Cookie("no_login_guest_user", "")));
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(query);
			dos.close();

			html = "<html><body>";
			html += Utility.read(conn);
			html += "</body></html>";
			Document doc = Jsoup.parse(html);
			for (Element el : doc.select("tbody tr")) {
				RaiderFund fund = new RaiderFund();
				fund.setAccountName(el.select(".account_name").text());
				fund.setAmount(el.select(".balance").text());
				funds.add(fund);
			}
		} catch (IOException e) {
			TTUAuth.logError(e, "raiderfundget", ErrorType.Fatal);
		} catch (Throwable t) {
			TTUAuth.logError(t, "raiderfundgeneral", ErrorType.Fatal, html);
		}

		return funds;
	}

	/**
	 * Logs into the meal plan balance checking website.
	 * 
	 * @return A value indicating whether the login succeeded.
	 */
	public LoginResult login() {
		if (!auth.isLoggedIn())
			return LoginResult.MAIN_LOGOUT;

		try {
			HttpURLConnection conn = Utility.getGetConn(FUND_COOKIE);
			conn.setInstanceFollowRedirects(false);
			ArrayList<Cookie> cookies = Cookie.getCookies(conn);
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getKey().startsWith("AWSE")) {
					awseCookie = cookie;
				}
				if (cookie.getKey().startsWith("PHPS")) {
					phpCookie = cookie;
				}
			}
			if (awseCookie == null || phpCookie == null)
				TTUAuth.logError(new IOException("Cookie values: " + awseCookie
						+ " " + phpCookie), "raiderfundlogincookie",
						ErrorType.APIChange);

			conn = Utility.getGetConn(FUND_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie",
					Cookie.chain(auth.getSharedLoginCookies()));
			cookies = Cookie.getCookies(conn);
			Cookie php2 = null;
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getKey().startsWith("PHPS")) {
					php2 = cookie;
				}
			}
			if (php2 == null)
				TTUAuth.logError(new IOException("Cookie value: " + php2),
						"raiderfundlogincookie", ErrorType.APIChange);

			Cookie[] cs = auth.getSharedLoginCookies();
			ArrayList<Cookie> cs2 = new ArrayList<Cookie>();
			for (int i = 0; i < cs.length; i++) {
				cs2.add(cs[i]);
			}
			cs2.add(php2);

			conn = Utility.getGetConn(CAS1_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);

			String location = Utility.getLocation(conn);
			if (location == null) {
				TTUAuth.logError(new IOException("Bad location response."),
						"raiderfundlocation", ErrorType.Severe);
				return LoginResult.OTHER;
			}
			conn = Utility.getGetConn(location);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);

			conn = Utility.getGetConn(CAS1_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie",
					Cookie.chain(awseCookie, phpCookie));
			Utility.readByte(conn);

			conn = Utility.getGetConn(FUND_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);

			location = Utility.getLocation(conn);
			if (location == null) {
				TTUAuth.logError(new IOException("Bad location response."),
						"raiderfundlocation", ErrorType.Severe);
				return LoginResult.OTHER;
			}
			conn = Utility.getGetConn(location);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie",
					Cookie.chain(awseCookie, phpCookie));
			Utility.readByte(conn);

			conn = Utility.getGetConn(FUND_INDEX);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(awseCookie,
					phpCookie, new Cookie("no_login_guest_user", "")));
			Utility.readByte(conn);
		} catch (IOException e) {
			TTUAuth.logError(e, "raiderfundlogin", ErrorType.Fatal);
			return LoginResult.OTHER;
		} catch (Throwable t) {
			TTUAuth.logError(t, "raiderfundlogingeneral", ErrorType.APIChange);
			return LoginResult.OTHER;
		}

		loggedIn = true;

		return LoginResult.SUCCESS;
	}
}
