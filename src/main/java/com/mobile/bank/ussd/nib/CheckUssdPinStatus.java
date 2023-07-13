package com.mobile.bank.ussd.nib;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.etz.mobile.security.Base64Encoder;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class CheckUssdPinStatus extends UssdActionClassInterface {
	static Logger l = new CheckUssdPinStatus().getLogger("MG2.0");
	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG_URL");
	static HashMap<String, Boolean> whitelistMap;

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();

		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
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
			f = getCustRcd(f, mn, appName);

		} catch (Exception e) {
			l.error(e, e);
		}

		return f;
	}

	private TreeMap<String, String> returnCustRcd(TreeMap<String, String> f, Response rcd) {
		if (rcd.getError() == 0) {
			if (rcd.getmProfileList() != null && rcd.getmProfileList().size() > 0) {
				int id = rcd.getmProfileList().get(0).getId();
				String uSess = rcd.getmProfileList().get(0).getuSess();
				String cardNum = rcd.getmProfileList().get(0).getCardNumber();
				String maskedAccount = rcd.getmProfileList().get(0).getMaskedAccount();
				boolean pinChanged = rcd.getmProfileList().get(0).isPinChanged();
				String customerAcc = rcd.getmProfileList().get(0).getCustomerAcc() + ",";

				if (!pinChanged) {
					f.put(String.format("%s==%s==%s==%s==%s_FIDJ_10", id, uSess, cardNum, maskedAccount, customerAcc),
							rcd.getmProfileList().get(0).getMaskedAccount());
				} else {
					f.put(String.format("%s==%s==%s==%s==%s==%s_FIDJ_30", id, uSess, cardNum, maskedAccount,
							customerAcc, "CHANGEPIN"), rcd.getmProfileList().get(0).getMaskedAccount());
				}
			}
		} else if (rcd.getError() == 6) {
			f.put("_FIDJ_19", "");
		} else {
			String text = encodeMsg(rcd.getMessage());
			f.put(text + "_FIDJ_100", "");
		}

		return f;
	}

	private TreeMap<String, String> getCustRcd(TreeMap<String, String> f, String phoneNo, String appName) {
		Response rcd = getUserProfile(appName, phoneNo);
		long a = System.currentTimeMillis();

		String maintenance = PropsCache.getInstance().getProperty("NIB_MAINTENANCE");

		if (maintenance.equals("0") || maintenance.equals("2")) {
			// maintenance off - 0
			if (maintenance.equals("0")) {
				returnCustRcd(f, rcd);
			}
			// maintenance on for whitelisted numbers - 2
			if (maintenance.equals("2")) {
				String[] whitelist = PropsCache.getInstance().getProperty("NIB_WHITELIST").split(";");
				whitelistMap = new HashMap<>();
				for (String list : whitelist) {
					whitelistMap.put(list, true);
				}

				boolean valid = whitelistMap.containsKey(phoneNo);

				if (valid) {
					returnCustRcd(f, rcd);
				} else {
					String text = PropsCache.getInstance().getProperty("NIB_MAINTENANCE_MSG");
					f.put(encodeMsg(text) + "_FIDJ_100", "");
				}
			}
		} else if (maintenance.equals("1")) {
			// maintenance on
			String text = PropsCache.getInstance().getProperty("NIB_MAINTENANCE_MSG");
			f.put(encodeMsg(text) + "_FIDJ_100", "");
		}

		// if (rcd.getmProfileList() != null && rcd.getmProfileList().size() > 0) {
		// int id = rcd.getmProfileList().get(0).getId();
		// String uSess = rcd.getmProfileList().get(0).getuSess();
		// String cardNum = rcd.getmProfileList().get(0).getCardNumber();
		// String maskedAccount = rcd.getmProfileList().get(0).getMaskedAccount();
		// boolean pinChanged = rcd.getmProfileList().get(0).isPinChanged();
		// String customerAcc = rcd.getmProfileList().get(0).getCustomerAcc() + ",";
		//
		// if (!pinChanged) {
		// f.put(String.format("%s==%s==%s==%s==%s_FIDJ_10", id, uSess, cardNum,
		// maskedAccount, customerAcc),
		// rcd.getmProfileList().get(0).getMaskedAccount());
		// } else {
		// f.put(String.format("%s==%s==%s==%s==%s==%s_FIDJ_30", id, uSess, cardNum,
		// maskedAccount, customerAcc,
		// "CHANGEPIN"), rcd.getmProfileList().get(0).getMaskedAccount());
		// }
		// } else {
		// f.put("_FIDJ_19", "");
		// }

		return f;
	}

	public static String encodeMsg(String message) {
		return Base64Encoder.encode(message);
	}

	public static void main(String[] args) {
		String json = "{\"SHORTCODE\":\"*389*710#\", \"MSISDN\":\"233542023469\"}";
		// System.out.println(new CheckUssdPinStatus().processIntermediateAction(json));
		System.out.println(encodeMsg("profile inactive. Kindly contact your bank"));
	}

	private Response getUserProfile(String appName, String msisdn) {
		Transaction trnx = new Transaction();
		trnx.setToken("GETPROFILE");
		trnx.setMsisdn(msisdn);
		trnx.setAppName(appName);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);
		l.info("GetProfile REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(msisdn);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("GetProfile REQ ENC:: " + reqJson);

		String url = URL + "/query";
		Response resp = DoHttpRequest.postToWS(url, reqJson);

		System.out.println("resp: " + gson.toJson(resp));

		return resp;
	}
}
