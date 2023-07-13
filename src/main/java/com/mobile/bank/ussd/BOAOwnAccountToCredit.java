package com.mobile.bank.ussd;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class BOAOwnAccountToCredit extends UssdActionClassInterface {
	static Logger l = new CheckUssdPinStatus().getLogger("MG2.0");

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();

		l.info("json received:: " + jsonData);
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = m.get("MSISDN").split(java.util.regex.Pattern.quote("***"))[0];

			String shortcode = (String) m.get("SHORTCODE");
			int howMany = shortcode.split("\\*").length - 1;
			String sc = "";

			switch (howMany) {
			case 1:
				sc = shortcode.replaceAll("#", "").replaceAll("\\*", "");
				break;
			case 2:
				sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(3);
				break;
			case 3:
				sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(6);
				break;
			}

			String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];
			String bankCode = PropsCache.getInstance().getProperty(sc + "BC");
			f.put("TMSG", "Select account to credit~");

			// String cardNum = "";
			String maskedAcc = "";
			String plainAcc = "";

			String accounts = getAccounts(bankCode, mn);

			if (!accounts.isEmpty()) {
				if (accounts.contains("|")) {
					String[] list = accounts.split("[|]");
					for (int i = 0; i < list.length; i++) {
						f.put(list[i].split("~")[2], list[i].split("~")[1]);
					}
				} else {
					// cardNum = accounts.split("~")[0];
					maskedAcc = accounts.split("~")[1];
					plainAcc = accounts.split("~")[2];
					f.put(plainAcc, maskedAcc);
				}
			}
		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	private String getAccounts(String bankCode, String phone) {
		String url = PropsCache.getInstance().getProperty("GET_ACCOUNTS_URL");
		String finalUrl = String.format(url, bankCode, phone);
		System.out.println("url:" + finalUrl);
		String accounts = "";

		try {
			String resp = DoHttpRequest.doGet(finalUrl);

			JSONObject r = new JSONObject(resp);
			String responseCode = r.getString("responseCode");
			if (responseCode.equals("00")) {
				accounts = r.getString("lookup");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return accounts;
	}

	public static void main(String[] args) {
		String json = "{\"SHORTCODE\":\"*389*389*021\", \"MSISDN\":\"233548933270\"}";
		System.out.println(new BOAOwnAccountToCredit().processIntermediateAction(json));
	}

}
