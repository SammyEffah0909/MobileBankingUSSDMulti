package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.util.GeneralUtils;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class _OwnAccountToCredit extends UssdActionClassInterface {
	static Logger l = Logger.getLogger(_OwnAccountToCredit.class);

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();
		
		l.info("json received:: " + jsonData);
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = m.get("MSISDN").split(java.util.regex.Pattern.quote("***"))[0];
			String whoAreU = m.containsKey("WHOAREU") ? m.get("WHOAREU") : ""; 
			String [] accts = whoAreU.split(",");
			String acctNo = null;
			
			if(accts.length > 1) {
				f.put("TMSG", "Transfers~~Select account to credit~~");
				for (int i = 0; i < accts.length; i++) {
					String [] who = accts[i].split("==");
					String id = who[0];
					String cardNumber = who[2];
					String maskedCardNumber = who[2];
					
					acctNo = GeneralUtils.cryptPan(cardNumber, 2);
					f.put(acctNo, maskedCardNumber);
				}
			}else {
				f.put("_FIDJ_10", "");
			}
		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	public static void main(String[] args) {
		String json = "{\"LOGIN\":\"A|NIB-140-----29501\",\"MM_NETWORK\":\"SELF\",\"MSISDN\":\"233542023469***104729359\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-WO-0344804-D44A-034\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"005\",\"WHOAREU\":\"NIB-140-----29501==DENNIS AKOMEAH==005001058D91E68A7E7202,NIB-1402053768001==null==005001F9B0CBB35BE11D4E,\"}";
		
		System.out.println(new _OwnAccountToCredit().processIntermediateAction(json));
	}

}
