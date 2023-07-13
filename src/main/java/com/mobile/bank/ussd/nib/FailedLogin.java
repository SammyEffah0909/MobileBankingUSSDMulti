package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class FailedLogin extends UssdActionClassInterface {

	static Logger l = new FailedLogin().doLog();

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
			String pinValid = m.containsKey("PINVERIF") ? m.get("PINVERIF") : "";

			if (pinValid.equals("3")) {
				// invalid pin
				f.put("TMSG", "Pin verification failed. Invalid PIN.");
				f.put("", "");
			} else if (pinValid.equals("9")) {
				// account hotlisted
				f.put("TMSG", "Your profile has been locked. Kindly contact your bank.");
				f.put("", "");
			} else {
				f.put("TMSG", "Could not verify profile. Please try again later.");
				f.put("", "");
			}

		} catch (Exception e) {
			l.error(e, e);
		}
		return f;

	}

}
