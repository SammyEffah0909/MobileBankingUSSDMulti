package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.etz.mobile.security.Base64Encoder;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class FailedUssdPinStatusCheck extends UssdActionClassInterface {

	static Logger l = new FailedUssdPinStatusCheck().doLog();

	private Logger doLog() {
		return getLogger("MG2.0");
	}

	@Override
	public TreeMap<String, String> processIntermediateAction(String arg) {
		l.info("Json received:: " + arg);
		// TODO Auto-generated method stub

		TreeMap<String, String> f = new TreeMap<>();
		// String message = "No request received for processing. Terminating Session.";
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(arg, Map.class);
			String failedMsg = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";

			String message = decodeMsg(failedMsg);

			f.put("TMSG", message);
			f.put("", "");
		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	public static String decodeMsg(String message) {
		return Base64Encoder.decode(message);
	}

}
