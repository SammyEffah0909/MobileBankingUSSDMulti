package com.mobile.bank.ussd.nib;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class AccountToDebit extends UssdActionClassInterface {
	static Logger l = new CheckUssdPinStatus().getLogger("MG2.0");
	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG_URL");

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();
		
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
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
			f = getCustRcd(f, mn, appName, bankCode);
			
		} catch (Exception e) {
			l.error(e, e);
		}
		
		return f;
	}
	
	private TreeMap<String, String> getCustRcd(TreeMap<String, String> f, String phoneNo, String appName, String bankCode) {
		Response rcd = getUserProfile(appName, phoneNo, bankCode);
		long a = System.currentTimeMillis();
		
		if(rcd.getmProfileList() != null && rcd.getmProfileList().size() > 0) {
			for(int i=0;i<rcd.getmProfileList().size();i++) {
				int id = rcd.getmProfileList().get(i).getId();
				String uSess = rcd.getmProfileList().get(i).getuSess();
				String cardNum = rcd.getmProfileList().get(i).getCardNumber();
				String maskedCardNum = rcd.getmProfileList().get(i).getMaskedAccount();
				String customerAcc = rcd.getmProfileList().get(i).getCustomerAcc() + ",";
				f.put(String.format("%s==%s==%s==%s==%s", id, uSess, cardNum, maskedCardNum, customerAcc), rcd.getmProfileList().get(i).getMaskedAccount());
			}
		}else {
			f.put("_FIDJ_19", "");
		}
		
		return f;
	}
	
	public static void main(String [] args) {
		String json = "{\"SHORTCODE\":\"*389*389\", \"MSISDN\":\"233209157113\"}";
		System.out.println(new AccountToDebit().processIntermediateAction(json));
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
