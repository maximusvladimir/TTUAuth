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
	private static String FUND_INDEX = "https://get.cbord.com/raidercard/full/index.php";
	private static String FUND_HOME = "https://get.cbord.com/raidercard/full/funds_home.php";
	private static String FUND_LOGIN = "https://get.cbord.com/raidercard/full/login.php";
	private static String FUND_OVERVIEW = "https://get.cbord.com/raidercard/full/funds_overview_partial.php";

	private Cookie aws;
	private Cookie php;
	private Cookie dummy = new Cookie("no_login_guest_user", "");

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
			conn.setRequestProperty("Cookie", Cookie.chain(aws, php, dummy));
			html = Utility.read(conn);
			//System.out.println(html);
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
			conn.setRequestProperty("Cookie", Cookie.chain(aws, php, dummy));
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
			HttpURLConnection conn = Utility.getGetConn(FUND_INDEX);
			conn.setInstanceFollowRedirects(false);
			ArrayList<Cookie> tmpCookies = Cookie.getCookies(conn);
			aws = Cookie.getCookie(tmpCookies, "AWSELB");
			php = Cookie.getCookie(tmpCookies, "PHPSESSID");
			
			conn = Utility.getGetConn(FUND_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(aws, php));
			String location = Utility.getLocation(conn);
			
			// https://webapps.itsd.ttu.edu/shim/getfunds/index.php/login?service=https%3A%2F%2Fget.cbord.com%2Fraidercard%2Ffull%2Flogin.php
			conn = Utility.getGetConn(location);
			conn.setInstanceFollowRedirects(false);
			// TODO: future speed optimization
			//if (auth.getPHPCookie() == null) {
				conn.setRequestProperty("Cookie", Cookie.chain(auth.getELCCookie()));
				Cookie phpCookie = Cookie.getCookie(Cookie.getCookies(conn), "PHPSESSID");
				// saves time for Blackboard and retrieveProfileImage().
				auth.setPHPCookie(phpCookie);
				
				conn = Utility.getGetConn(Utility.getLocation(conn));
				conn.setRequestProperty("Cookie", Cookie.chain(auth.getERaiderCookies()));
				conn.setInstanceFollowRedirects(false);
				
				location = Utility.getLocation(conn);
				if (location.startsWith("signin.aspx"))
					location = "https://eraider.ttu.edu/" + location;
				conn = Utility.getGetConn(location);
				conn.setInstanceFollowRedirects(false);
				conn.setRequestProperty("Cookie", Cookie.chain(auth.getERaiderCookies()));
				// might need to set ESI and ELC here. If other areas are bugged, this is why.
			/*} else {
				conn.setRequestProperty("Cookie", Cookie.chain(auth.getELCCookie(), auth.getPHPCookie()));
				/// TODO This is in retirevProfileImage, maybe Mobile Login, and Blackboard!!!
				throw new NullPointerException("Needs implementation!");
			}*/
			
			// https://webapps.itsd.ttu.edu/shim/getfunds/index.php?elu=XXXXXXXXXX&elk=XXXXXXXXXXXXXXXX
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(auth.getPHPCookie(), auth.getELCCookie()));
			
			// https://webapps.itsd.ttu.edu/shim/getfunds/index.php/login?service=https%3A%2F%2Fget.cbord.com%2Fraidercard%2Ffull%2Flogin.php
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(auth.getPHPCookie(), auth.getELCCookie()));
			
			// https://get.cbord.com/raidercard/full/login.php?ticket=ST-...
			conn = Utility.getGetConn(Utility.getLocation(conn));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(aws, php));
			
			// https://get.cbord.com/raidercard/full/funds_home.php
			location = Utility.getLocation(conn);
			if (location.startsWith("index.")) {
				location = "https://get.cbord.com/raidercard/full/" + location;
			}
			conn = Utility.getGetConn(location);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(aws, php));
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
