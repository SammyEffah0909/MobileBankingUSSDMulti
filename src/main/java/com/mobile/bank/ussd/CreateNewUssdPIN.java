package com.mobile.bank.ussd;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.mobile.bank.processor.SendMGRequest;
import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdSessionTerminatorInterface;
import com.google.gson.Gson;

public class CreateNewUssdPIN extends UssdSessionTerminatorInterface {
	static Logger l = new SendMGRequest().getLogger("MG2.0");

	public String processUssdRequest(String jsonReq) {
		String rMsg = "";
		HashMap<String, String> reply = new HashMap();
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonReq, Map.class);
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
			String acct = (String) m.get("NEWUSERACCT");
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

			String newPIN1 = (String) m.get("NEWPIN1");
			String newPIN2 = (String) m.get("NEWPIN2");
			String onboardingMsgSuffix = PropsCache.getInstance().getProperty(sc + "ONBOARD_SUFFIX");
			// String appName =
			// GeneralUtils.getAppName(m.get("SHORTCODE").toString()).split(":")[1];
			String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];
			String bankName = PropsCache.getInstance().getProperty(sc + "NAME");

			String[] acctRcd = ((String) m.get("MATCH")).split("::");
			String firstName = acctRcd[5];

			String msg = "";
			if (newPIN1.equals(newPIN2)) {
				if (isNumeric(newPIN2)) {
					if (createAccount(appName, mn, acct, firstName, newPIN1) == 0) {
						msg = String.format("Hi %s Congrats!,~You have successfully registered for %s. %s", firstName,
								bankName, onboardingMsgSuffix);
						reply.put("error", "00");
						reply.put("msg", msg);
					} else {
						msg = String.format("Hi, %s~Your Registration Failed.~Kindly Contact your bank branchEND",
								firstName);
						reply.put("error", "00");
						reply.put("msg", msg);
					}
				} else {
					reply.put("error", "00");
					reply.put("msg", "Please ensure you enter a number as your PIN..");
				}
			} else {
				msg = String.format("Hi, %s~Ensure your inputs are correctly matchedEND", firstName);
				reply.put("error", "00");
				reply.put("msg", msg);
			}
		} catch (Exception e) {
			l.error(e, e);
		}
		rMsg = new Gson().toJson(reply, Map.class);
		return rMsg;
	}

	public String processUssdResponse(String responseMessageFormatter, String messageToFormat) {
		Gson j = new Gson();
		Map<String, String> m = (Map) j.fromJson(messageToFormat, Map.class);

		String sc = (String) m.get("msg");
		return sc;
	}

	private boolean isNumeric(String newPIN2) {
		boolean numeric = true;
		try {
			Integer.parseInt(newPIN2);
		} catch (Exception e) {
			numeric = false;
			l.error(e, e);
		}
		return numeric;
	}

	private int createAccount(String appName, String msisdn, String accountNumber, String firstName, String pin) {
		final String KEY = PropsCache.getInstance().getProperty("KEY");
		final String IV = PropsCache.getInstance().getProperty("IV");
		final String PRODUCTION = PropsCache.getInstance().getProperty("PRODUCTION");
		String URL = PRODUCTION.equals("1") ? PropsCache.getInstance().getProperty("MG_URL")
				: PropsCache.getInstance().getProperty("MG2URLDEMO");
		URL += "/process";

		String pCode = generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setToken("ACCSYNCP");
		trnx.setMsisdn(msisdn);
		trnx.setAppName(appName);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);
		trnx.setuSess(firstName);
		trnx.setNewUserAccount(accountNumber);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("ACCSYNCP REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(msisdn);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("ACCSYNCP REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("ACCSYNCP RESP:: " + gson.toJson(resp));
		l.info("ACCSYNCP TAT:: " + (b - a) + "ms");

		return resp.getError();
	}

	private String generateRandomString(int length) {
		Random RANDOM = new SecureRandom();
		String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder returnValue = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}

		return new String(returnValue);
	}

	public static void main(String[] args) {
		String json = "{\"MSISDN\":\"233548933270***383847\",\"NEWUSERACCT\":\"1030306600166942\",\"NEWPIN1\":\"1111\",\"NEWPIN2\":\"1111\", \"SHORTCODE\":\"*389*810#\",\"MATCH\":\"1::card_num::default_pin::change_pin::card_pin::GA RURAL TEST\"}";
		// System.out.println(new CreateNewUssdPIN().processUssdRequest(json));
		// System.out.println("*5912*710#".substring(9,
		// "*5912*710#".length()).replace("#", ""));
		// int success = new CreateNewUssdPIN().createAccount("GARMobile",
		// "233548933270", "3031020000229201",
		// "GA RURAL TEST", "1111");
		// System.out.println(success);
	}
}
