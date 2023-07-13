package com.mobile.bank.ussd.nib;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class CheckUssdPinStatusTest extends UssdActionClassInterface {
	static Logger l = new CheckUssdPinStatusTest().getLogger("MG2.0");
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
			f.put("29154==E-TRANZACT USSD TEST==9050017DDFC3516AFE9B85==203*****96201==2035079796201", "203*****96201");

		} catch (Exception e) {
			l.error(e, e);
		}

		return f;
	}

	public static void main(String[] args) {
		String json = "{\"SHORTCODE\":\"*389*710#\", \"MSISDN\":\"233542023469\"}";
		// System.out.println(new CheckUssdPinStatus().processIntermediateAction(json));
	}
}
