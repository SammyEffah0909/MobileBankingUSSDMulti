package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.util.PropsCache;
import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class VerifyMerchantId extends UssdActionClassInterface {
	static Logger l = Logger.getLogger(VerifyMerchantId.class);
	private static final String AUTOSWITCH_IP = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
	private static final String AUTOSWITCH_PORT = PropsCache.getInstance().getProperty("AUTOSWITCH_PORT");

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
			String terminalId = (String) m.get("TERMINAL");

			String verified = verifyMerchant(terminalId);

			l.info("Verified: " + verified);

			if (!verified.isEmpty() || verified != null) {
				if (Double.parseDouble(verified.split("#")[1]) > 0) {
					f.put("TMSG", "Merchant: " + verified.split("#")[0] + "~Amount:GHS" + verified.split("#")[1]
							+ "~Enter 1 to proceed");
					f.put(String.format("%s_FIDJ_5", verified), "");
				} else {
					f.put("TMSG", "Merchant: " + verified.split("#")[0] + "~Enter 1 to proceed");
					f.put(verified, "");
				}
			}
		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	public static void main(String[] args) {
		// System.out.println(getNibDetails("2402038329501"));
		String s = "{" + "    \"MSISDN\": \"233547320775***3074766813344848\"," + "    \"TERMINAL\": \"3050044019\","
				+ "    \"PROVIDER\": \"TIGO\"," + "    \"REFERENCE\": \"ussdx-WK-1180198-J80J-898\","
				+ "    \"SHORTCODE\": \"*389*277#\"," + "    \"NEWUSERACCT\": \"1402078178101\","
				+ "    \"WHOAREU\": \"ABII-123-----00016\\u003d\\u003dSOLOMON FOSU\\u003d\\u003d9200019A2040DB492B13CC\""
				+ "}";

		System.out.println(new VerifyMerchantId().processIntermediateAction(s));
	}

	public static String verifyMerchant(String terminalId) {
		String returnValue = "";
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "022020";
		int port = Integer.parseInt(AUTOSWITCH_PORT);
		XProcessor processor = new XProcessor();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		card.setCardNumber("0052402038329501");
		card.setCardExpiration(expiryDate);

		XRequest request = new XRequest();
		request.setXmlString(String.format("<CBARequest>QRE:%s</CBARequest>", terminalId));
		request.setCard(card);
		request.setTransCode(TransCode.BANKSERVICE);

		XResponse response = null;

		try {
			response = processor.process(host, request);

			System.out.println("Response: " + response.getResponse());
			System.out.println("Message: " + response.getMessage());
			System.out.println("Custom XML: " + response.getCustomXml());

			if (response.getResponse() == 0) {
				returnValue = response.getCustomXml();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}

}
