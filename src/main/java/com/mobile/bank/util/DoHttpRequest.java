package com.mobile.bank.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mobile.bank.ussd.model.MobileProfile;
import com.mobile.bank.ussd.model.Response;

public class DoHttpRequest {

	public static String doGet(String url) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");
		con.setReadTimeout(30000);

		// add request header
		// con.setRequestProperty("Content-Type", "application/json");

		int responseCode = con.getResponseCode();
		BufferedReader in = null;
		if (responseCode == 200) {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	public static String sendJSONPost(String url, String postData) {
		StringBuffer response = null;

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setConnectTimeout(9000);
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postData);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			BufferedReader in = null;

			if (responseCode == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			} else if (responseCode == 400) {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String inputLine;
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} catch (SocketTimeoutException e) {
			return "-1";// Socket Timeout
		} catch (Exception ex) {
			ex.printStackTrace();
			return "06";// Error Occurred
		}
	}

	public static Response postToWS(String url, String json) {
		Response response = new Response();

		try {
			String resp = sendJSONPost(url, json);

			if (!resp.equals("-1") || !resp.equals("06")) {
				JSONObject obj = new JSONObject(resp);
				response.setError(obj.getInt("error"));
				response.setMessage(obj.getString("message"));
				response.setMobile(obj.getString("mobile"));
				response.setReference(obj.getString("reference"));
				response.setToken(obj.getString("token"));

				if (response.getError() == 0) {
					// successful
					JSONArray jArray = obj.optJSONArray("mProfileList");

					if (jArray != null) {
						if (jArray.length() > 0) {
							List<MobileProfile> mProfileList = new ArrayList<MobileProfile>();

							for (int i = 0; i < jArray.length(); i++) {
								MobileProfile mProfile = new MobileProfile();
								JSONObject jObj = jArray.getJSONObject(i);
								mProfile.setId(jObj.getInt("id"));
								mProfile.setAppId(jObj.getInt("appId"));
								mProfile.setMobileNo(jObj.getString("mobileNo"));
								mProfile.setAppName(jObj.getString("appName"));
								mProfile.setuSess(jObj.getString("uSess"));
								mProfile.setCardNumber(jObj.getString("cardNumber"));
								mProfile.setMaskedAccount(jObj.getString("maskedAccount"));
								mProfile.setCustomerAcc(jObj.getString("customerAcc"));
								mProfile.setActive(jObj.getBoolean("active"));
								mProfile.setPinChanged(jObj.getBoolean("pinChanged"));
								mProfileList.add(mProfile);
							}
							response.setmProfileList(mProfileList);
						}
					}
				}
			} else {
				response.setError(6);
				response.setMessage("Unable to retrieve user profile");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
}
