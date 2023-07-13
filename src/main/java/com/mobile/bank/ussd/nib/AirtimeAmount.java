package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class AirtimeAmount extends UssdActionClassInterface {

	// public static void main(String...args) {
	//
	// }

	static Logger l = new AirtimeAmount().doLog();

	private Logger doLog() {
		return getLogger("AIRTIME AMOUNT LOGIN");
	}

	@Override
	public TreeMap<String, String> processIntermediateAction(String arg0) {
		// TODO Auto-generated method stub
		l.info("Json received:: " + arg0);

		// {"LOGIN":"A|29154|0050012AE6E646668EF20A","MOBILE":"0201231267","MSISDN":"233542023469***103816179","NETWORK":"Vodafone","PINVERIF":"","PROJECTEDINPUT":"1111","PROVIDER":"USSDBRIDGE","REFERENCE":"ussdx-QL-0521239-H21D-372","SHORTCODE":"*389*389*710","TRANS_TYPE":"VL","VTUOPTION":"OTHERS","WHOAREU":"29154\u003d\u003dE-TRANZACT
		// USSD
		// TEST\u003d\u003d0050012AE6E646668EF20A\u003d\u003d203*****201\u003d\u003d2035079796201,"}
		// MTN|Vodafone|Tigo|Glo

		TreeMap<String, String> f = new TreeMap<>();
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(arg0, Map.class);
			String networkOther = m.containsKey("NETWORK") ? m.get("NETWORK") : "";
			boolean hasLimit = false;
			String message = "Enter Amount";
			if (networkOther.isEmpty()) {
				hasLimit = m.containsKey("PROVIDER") ? getProviderName(m.get("PROVIDER")) : false;
			} else {
				if (networkOther.toLowerCase().equals("vodafone")) {
					hasLimit = true;
				}
			}
			// boolean

			if (hasLimit) {
				message += " (should not exceed GHS 50.00)~";
			} else {
				message += "~";
			}

			f.put("TMSG", message);
			f.put("", "");

		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	public boolean getProviderName(String provider) {
		// String networkCode = "";

		switch (provider) {
		case "MTNSDP710":
			// networkCode = "MTN";
			break;
		case "VODAFONEGH":
			// networkCode = "VODAFONE";
			return true;
		// break;
		case "TIGO":
			// networkCode = "TIGO";
			break;
		default:
			// networkCode = "MTN";
		}

		return false;
		// return networkCode;
	}

}
