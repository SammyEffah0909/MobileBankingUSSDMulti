package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class UserLogin extends UssdActionClassInterface {
	static Logger l = new UserLogin().doLog();

	private Logger doLog() {
		return getLogger("MG2.0");
	}

	@Override
	public TreeMap<String, String> processIntermediateAction(String arg0) {
		// TODO Auto-generated method stub
		l.info("Json received: " + arg0);

		TreeMap<String, String> f = new TreeMap();
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(arg0, Map.class);
			String userDetails = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";
			// userDetails = "NIB:-240----29501==DENNIS
			// AKOMEAH==00500188859DAF6D3EA577,NIB:-240----29502==DENNIS
			// AKOMEAH==00500188859DAF6D3EA578";

			l.info("Action class called. userDetails found:: " + userDetails);
			/*
			 * String[] accounts = userDetails.split(","); if(accounts.length > 1) { String
			 * name = ""; //User has multiple accounts String[] prefices = new String[]
			 * {"A", "B", "C", "D", "E", "F", "G", "H", "I"}; for(int i=0;
			 * i<accounts.length; i++) { String accountDetails[] = accounts[i].split("==");
			 * String cardNumber = GeneralUtils.cryptPan(accountDetails[2], 2); String
			 * maskedCardNumber = GeneralUtils.maskCardNumber(cardNumber, 4, 4); name =
			 * accountDetails[1]; String id = accountDetails[0];
			 * f.put(prefices[i]+"|"+Integer.parseInt(id)+"_FIDJ_13", maskedCardNumber); }
			 * f.put("TMSG", "Welcome "+name+", ~select an account~"); }else {
			 * l.info("Action class called. Single user found."); //User has a single
			 * account String[] detailsParts = userDetails.split("=="); String name =
			 * detailsParts[1]; String alias = detailsParts[0];
			 * 
			 * f.put("TMSG", "Welcome "+name+", Enter PIN to continue:"); //alias+
			 * f.put("A|"+alias, ""); }
			 */
			// User has a single account
			String[] detailsParts = userDetails.split("==");
			String id = detailsParts[0];
			String name = detailsParts[1];
			String cardNum = detailsParts[2];

			f.put("TMSG", "Welcome " + name + ", Enter PIN to continue:");

			f.put(String.format("A|%s|%s", id, cardNum), "");

		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}
}
