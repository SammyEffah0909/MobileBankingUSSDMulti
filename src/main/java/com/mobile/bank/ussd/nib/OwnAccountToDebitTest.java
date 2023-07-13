package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class OwnAccountToDebitTest extends UssdActionClassInterface {
	static Logger l = new CheckUssdPinStatus().getLogger("MG2.0");
	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG_URL");

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();

		l.info("json received:: " + jsonData);
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = m.get("MSISDN").split(java.util.regex.Pattern.quote("***"))[0];
			// String whoAreU = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";

			String accountToCredit = m.get("CUSTACCS");

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

			f.put("29154==E-TRANZACT USSD TEST==0050012AE6E646668EF20A==203*****96201==2035079796201", "203*****96201");

		} catch (Exception e) {
			l.error(e, e);
		}

		return f;
	}

}
