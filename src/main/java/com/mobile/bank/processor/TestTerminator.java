package com.mobile.bank.processor;

import java.util.Map;

import org.apache.log4j.Logger;

import com.fnm.ussd.engine.util.UssdSessionTerminatorInterface;
import com.google.gson.Gson;

public class TestTerminator extends UssdSessionTerminatorInterface {
	static Logger l = new TestTerminator().getLogger("UseTerminatorClass");

	@Override
	public String processUssdRequest(String jsonReq) {
		String error = "06";
		String resp = "";

		try {
			System.out.println("Json Received: " + jsonReq);
			l.info("Json Received: " + jsonReq);
			Gson j = new Gson();
			Map<String, String> m = (Map<String, String>) j.fromJson(jsonReq, (Class) Map.class);

			final String msisdn = m.get("MSISDN").split(java.util.regex.Pattern.quote("**"))[0];
			String vastype = (String) m.get("VASTYPE");
			String mainoptions = (String) m.get("MAINOPTIONS");
			String amount = (String) m.get("AMOUNT");
			String reference = (String) m.get("REFERENCE");
			String shortcode = m.containsKey("SHORTCODE") ? m.get("SHORTCODE") : "";

			if (true) {
				error = "00";
				resp = "Successful";
			} else {
				resp = "Failed";
			}
		} catch (Exception ex) {
			resp = "An error occurred";
			l.error("An error occurred ", ex);
			l.error(ex.getMessage());
			ex.printStackTrace();
		}

		return "{\"error\":\"" + error + "\",\"msg\":\"" + resp + "\"}";
	}

	@Override
	public String processUssdResponse(String formatter, String msg2format) {
		String w = "";
		try {
			Gson gson = new Gson();
			Map<String, String> m = (Map<String, String>) gson.fromJson(msg2format, (Class) Map.class);
			w = m.get("msg");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return w;
	}

}
