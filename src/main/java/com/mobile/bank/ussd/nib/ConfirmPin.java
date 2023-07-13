package com.mobile.bank.ussd.nib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.util.GeneralUtils;
import com.mobile.bank.util.PropsCache;
import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

/*
 * Action Class usually used for Verification
 */
public class ConfirmPin extends UssdActionClassInterface {
	static Logger l = new ConfirmPin().getLogger("MG2.0");
	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG_URL") + "/process";
	private static final String AUTOSWITCH_IP = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
	private static final String AUTOSWITCH_PORT = PropsCache.getInstance().getProperty("AUTOSWITCH_PORT");

	@Override
	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();

		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			// get inputholders from USSD Menu Here
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
			String pin = m.containsKey("PROJECTEDINPUT") ? m.get("PROJECTEDINPUT") : "";
			String login = m.get("LOGIN");
			String id = login.split("\\|")[1];
			String cardNum = login.split("\\|")[2];
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

			// String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];
			l.info("json received:: " + jsonData);

			int pinValid = isPinValid(GeneralUtils.cryptPan(cardNum, 2), pin);

			if (pinValid == 0) {
				// pin verified successfully
				f.put("_FIDJ_2", "");
			} else if (pinValid == 3) {
				// invalid pin
				// f.put("TMSG", "Pin verification failed. Invalid PIN.");
				// f.put("", "");
				f.put("3_FIDJ_40", "");
			} else if (pinValid == 9) {
				// account hotlisted
				// f.put("TMSG", "Your profile has been locked. Kindly contact your bank.");
				// f.put("", "");
				f.put("9_FIDJ_40", "");
			} else {
				// f.put("TMSG", "Could not verify profile. Please try again later.");
				// f.put("", "");
				f.put("_FIDJ_40", "");
			}
		} catch (Exception e) {
			l.error(e, e);
		}

		return f;
	}

	public static void main(String[] args) {
		String json = "{\"LOGIN\":\"A|NIB-1402053768001\",\"MSISDN\":\"233542023469***104271632\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-ZK-3390889-B90I-619\",\"SHORTCODE\":\"*389*389*710\",\"WHOAREU\":\"NIB-1402053768001\\u003d\\u003dAGBANYO KAFUI KOBLA (STAFF)\\u003d\\u003d005001151DD2D3004BAB88,NIB-1402038329501\\u003d\\u003dnull\\u003d\\u003d00500125AB24EFBECF20F1,\"};";

		// System.out.println(new ConfirmPin().doBalance(1, "1111", "NIBMobile",

		long a = System.currentTimeMillis();
		System.out.println(isPinValid("0050010000029619", "1111"));// 0050010000029619//0050010000030634
		long b = System.currentTimeMillis();
		long c = b - a;
		System.out.println("TAT:" + c);
	}

	/*
	 * private String doBalance(int id, String appName, String mobile, String pin) {
	 * String pCode = GeneralUtils.generateRandomString(16); String encryptedPin =
	 * AESUtil.AESCBCEncrypt(pin, KEY, pCode);
	 * 
	 * Transaction trnx = new Transaction(); trnx.setId(id); trnx.setToken("B");
	 * trnx.setMsisdn(mobile); trnx.setAppName(appName); trnx.setPin(encryptedPin);
	 * trnx.setpCode(pCode);
	 * 
	 * Gson gson = new Gson(); String json = gson.toJson(trnx);
	 * 
	 * l.info("BALANCE REQ:: " + json);
	 * 
	 * String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);
	 * 
	 * Request req = new Request(); req.setId(mobile); req.setMsg(encryptedMsg);
	 * 
	 * String reqJson = gson.toJson(req);
	 * 
	 * l.info("BALANCE REQ ENC:: " + reqJson);
	 * 
	 * long a = System.currentTimeMillis(); Response resp =
	 * DoHttpRequest.postToWS(URL, reqJson); long b = System.currentTimeMillis();
	 * 
	 * l.info("BALANCE RESP:: " + gson.toJson(resp)); l.info("BAL TAT:: " + (b -
	 * a));
	 * 
	 * return String.format("%s#%s", resp.getError(), resp.getMessage()); }
	 */

	public static String getUniqueId() {
		String message = "";
		try {
			String tt = "";
			for (int s = 0; s < 3; s++) {
				tt = tt + new Random().nextInt(5);
			}
			Date d = new Date();
			DateFormat df = new SimpleDateFormat("ddHHmmss");
			String dt = df.format(d);
			message = "02_PINV" + dt + tt;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return message;
	}

	public static int isPinValid(String cardNumber, String pin) {
		int valid = -1;
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "022020";
		int port = Integer.parseInt(AUTOSWITCH_PORT);
		XProcessor processor = new XProcessor();
		String reference = getUniqueId();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		card.setCardExpiration(expiryDate);
		card.setCardNumber(cardNumber);
		card.setCardPin(pin);
		card.setAccountType("CA");

		XResponse xResponse = null;

		try {
			XRequest request = new XRequest();
			request.setCard(card);
			request.setTransCode(TransCode.BALANCE);
			request.setReference(reference);
			request.setChannelId("02");

			xResponse = processor.process(host, request);
			String r = String.format("PinVerify Reference: %s Card Number: %s Response Code: %s", reference, cardNumber,
					xResponse.getResponse());
			l.info(r);

			valid = xResponse.getResponse();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return valid;
	}

}
