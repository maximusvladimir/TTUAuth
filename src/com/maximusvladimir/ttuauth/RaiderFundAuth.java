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

public class RaiderFundAuth {
	private static String FUND_LOGIN = "https://webapps.itsd.ttu.edu/shim/getfunds/index.php/login?service=https%3A%2F%2Fget.cbord.com%2Fraidercard%2Ffull%2Flogin.php";
	private static String FUND_COOKIE = "https://get.cbord.com/raidercard/full/login.php";
	private static String CAS1_LOGIN = "https://eraider.ttu.edu/signin.asp?redirect=https%3A%2F%2Fwebapps.itsd.ttu.edu%2Fshim%2Fgetfunds%2Findex.php";
	private static String FUND_INDEX = "https://get.cbord.com/raidercard/full/index.php";
	private static String FUND_HOME = "https://get.cbord.com/raidercard/full/funds_home.php";
	private static String FUND_OVERVIEW = "https://get.cbord.com/raidercard/full/funds_overview_partial.php";
	
	private Cookie awseCookie;
	private Cookie phpCookie;
	
	/**
	 * Gets a list of account balances for meal plans.
	 * @return A list of balances and names.
	 * @throws IOException
	 */
	public ArrayList<RaiderFund> getRaiderFunds() throws IOException {
		HttpURLConnection  conn = Utility.getGetConn(FUND_HOME);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestProperty("Cookie", Cookie.chain(awseCookie, phpCookie, new Cookie("no_login_guest_user","")));
		String html = Utility.read(conn);
		int index = html.indexOf("getOverview");
		String userID = html.substring(index);
		userID = userID.substring(userID.indexOf("(")+2,userID.indexOf(")")-1);
		String token = "formToken\"";
		index = html.indexOf(token);
		index += token.length() + 1;
		String form = html.substring(index);
		form = form.substring(form.indexOf("\"")+1);
		form = form.substring(0, form.indexOf("\""));
		
		conn = Utility.getPostConn(FUND_OVERVIEW);
		String query = "userId=" + userID + "&formToken=" + form;
		conn.setRequestProperty("Content-Length", query.length() + "");
		conn.setInstanceFollowRedirects(false);
		conn.setRequestProperty("Referer","https://get.cbord.com/raidercard/full/funds_home.php");
		conn.setRequestProperty("Cookie", Cookie.chain(awseCookie, phpCookie, new Cookie("no_login_guest_user","")));
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeBytes(query);
		dos.close();
		
		html = "<html><body>";
		html += Utility.read(conn);
		html += "</body></html>";
		Document doc = Jsoup.parse(html);
		ArrayList<RaiderFund> funds = new ArrayList<RaiderFund>();
		for (Element el : doc.select("tbody tr")) {
			RaiderFund fund = new RaiderFund();
			fund.setAccountName(el.select(".account_name").text());
			fund.setAmount(el.select(".balance").text());
			funds.add(fund);
		}
		
		return funds;
	}
	
	/**
	 * Logs into the meal plan balance checking website.
	 * @param auth The TTU main website auth token.
	 * @return A value indicating whether the login succeeded.
	 */
	public LoginResult login(TTUAuth auth) {
		if (!auth.isLoggedIn())
			return LoginResult.BAD_AUTH;
		
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
			
			conn = Utility.getGetConn(FUND_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(auth.getCbordLoginCookies()));
			cookies = Cookie.getCookies(conn);
			Cookie php2 = null;
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getKey().startsWith("PHPS")) {
					php2 = cookie;
				}
			}
			
			Cookie[] cs = auth.getCbordLoginCookies();
			ArrayList<Cookie> cs2 = new ArrayList<Cookie>();
			for (int i = 0; i < cs.length; i++) {
				cs2.add(cs[i]);
			}
			cs2.add(php2);

			conn = Utility.getGetConn(CAS1_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);
			
			conn = Utility.getGetConn(conn.getHeaderFields().get("Location").get(0));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);
			
			conn = Utility.getGetConn(CAS1_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(awseCookie, phpCookie));
			Utility.readByte(conn);
			
			conn = Utility.getGetConn(FUND_LOGIN);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(cs2));
			Utility.readByte(conn);
			
			conn = Utility.getGetConn(conn.getHeaderFields().get("Location").get(0));
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie",  Cookie.chain(awseCookie, phpCookie));
			Utility.readByte(conn);
			
			conn = Utility.getGetConn(FUND_INDEX);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", Cookie.chain(awseCookie, phpCookie, new Cookie("no_login_guest_user","")));
			Utility.readByte(conn);
		} catch (IOException e) {
			return LoginResult.OTHER;
		}
		
		return LoginResult.SUCCESS;
	}
}
