package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.GeneralUtils;
import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class OwnAccountToDebit extends UssdActionClassInterface {
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
//			String whoAreU = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";
			
			String accountToCredit = m.get("CUSTACCS");
			
			String shortcode = (String) m.get("SHORTCODE");
			int howMany = shortcode.split("\\*").length-1;
			String sc="";
			
			switch(howMany) {
				case 1:
					sc = shortcode.replaceAll("#","").replaceAll("\\*", "");
					break;
				case 2:
					sc = shortcode.replaceAll("#","").replaceAll("\\*", "").substring(3);
					break;
				case 3:
					sc = shortcode.replaceAll("#","").replaceAll("\\*", "").substring(6);
					break;
			}
			
			String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];
			String bankCode = PropsCache.getInstance().getProperty(sc+"BC");
			
			f = getCustRcd(f, mn, appName, bankCode, accountToCredit);
//			String [] accts = accountToCredit.split(",");
//			String acctNo = null;
			
//			if(accts.length > 1) {
//				for (int i = 0; i < accts.length; i++) {
//					String [] who = accts[i].split("==");
//					String cardNum = who[2];
//					String whoRu = String.format("%s==%s==%s==%s", who[0], who[1], who[2], who[3]);
//					
//					if(cardNum.equals(accountToCredit)) {
//						continue;
//					}
//					
//					f.put(whoRu, who[3]);
//				}
//			}else {
//				f.put("_FIDJ_10", "");
//			}
		} catch (Exception e) {
			l.error(e, e);
		}
		
		return f;
	}
	
	private TreeMap<String, String> getCustRcd(TreeMap<String, String> f, String phoneNo, String appName, String bankCode, String compare) {
		Response rcd = getUserProfile(appName, phoneNo, bankCode);
		long a = System.currentTimeMillis();
		
		if(rcd.getmProfileList() != null && rcd.getmProfileList().size() > 0) {
			for(int i=0;i<rcd.getmProfileList().size();i++) {
				int id = rcd.getmProfileList().get(i).getId();
				String uSess = rcd.getmProfileList().get(i).getuSess();
				String cardNum = rcd.getmProfileList().get(i).getCardNumber();
				String maskedCardNum = rcd.getmProfileList().get(i).getMaskedAccount() + ",";
				
				String [] acct = compare.split(",");
				
				for(int j=0;j<acct.length;j++) {
					String [] who = acct[j].split("==");
					if(who[2].equals(cardNum)) {
						continue;
					}
					f.put(String.format("%s==%s==%s==%s", id, uSess, cardNum, maskedCardNum), rcd.getmProfileList().get(i).getMaskedAccount());
				}
			}
		}else {
			f.put("_FIDJ_10", "");
		}
		
		return f;
	}

	public static void main(String[] args) {//2402078178101,1402078178101
		//String json = "{\"LOGIN\":\"A|NIB-140-----29501\",\"MM_NETWORK\":\"SELF\",\"MSISDN\":\"233542023469***104729359\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-WO-0344804-D44A-034\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"005\",\"WHOAREU\":\"NIB-140-----29501==DENNIS AKOMEAH==005001058D91E68A7E7202,NIB-1402053768001==null==005001F9B0CBB35BE11D4E,\"}";
//		String json = "{\"AMOUNT\":\"1\",\"CUSTACCS\":\"2402078178101\",\"LOGIN\":\"A|NIB-1402078178101\",\"MM_NETWORK\":\"SELF\",\"MSISDN\":\"233542023469***104288067\",\"PIN\":\"1111\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-VS-4328214-F28B-352\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"005\",\"WHOAREU\":\"NIB-1402078178101==ADU-OKYERE GRACE FOSUAH==005001660A1C444A5CAE62,NIB-2402078178101==null==005001FD6B148338C9C01F,\"}";
//		System.out.println(new OwnAccountToDebit().processIntermediateAction(json));
		String json = "{\"CUSTACCS\":\"22\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500113D2900B3C4905C8\\u003d\\u003d140*****501,\",\"LOGIN\":\"A|22\",\"MM_NETWORK\":\"SELF\",\"MSISDN\":\"233548933270***105492741\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-QL-4441919-B41B-114\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"005\",\"WHOAREU\":\"22\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500113D2900B3C4905C8\\u003d\\u003d140*****501,\"}";
		System.out.println(new OwnAccountToDebit().processIntermediateAction(json));
	}
	
	private Response getUserProfile(String appName, String msisdn, String bankCode) {
		Transaction trnx = new Transaction();
		trnx.setToken("GETPROFILE");
		trnx.setMsisdn(msisdn);
		trnx.setAppName(appName);
		trnx.setSourceBankCode(bankCode);
		
        Gson gson = new Gson();    
        String json = gson.toJson(trnx);
        l.info("GetProfile REQ:: "+ json);
        
        String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);
		
		Request req = new Request();
		req.setId(msisdn);
		req.setMsg(encryptedMsg);
		
		String reqJson = gson.toJson(req);
		l.info("GetProfile REQ ENC:: "+ reqJson);
		
		String url = URL + "/query";
		Response resp = DoHttpRequest.postToWS(url, reqJson);
		
		System.out.println("resp: "+ gson.toJson(resp));
		
		return resp;
	}

}
