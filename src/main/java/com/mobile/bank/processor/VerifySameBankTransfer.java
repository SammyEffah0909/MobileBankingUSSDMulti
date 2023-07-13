/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.processor;

import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.mobile.bank.util.DoHttpRequest;
import java.util.Map;
import java.util.TreeMap;
import com.mobile.bank.util.PropsCache;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class VerifySameBankTransfer extends UssdActionClassInterface {

    static final Logger log = Logger.getLogger(VerifySameBankTransfer.class);
    
    public static void main(String[] args) {
        String json = "{\"MSISDN\":\"233209157113***1499303250\",\"PROVIDER\":\"VODAFONEGH\",\"REFERENCE\":\"ussdx-WR-2397775-C97H-429\",\"SHORTCODE\":\"*389*710#\",\"TRANS_TYPE\":\"005\",\"WALLET_NUMBER1\":\"1402078178101\",\"WHOAREU\":\"NIB-240-----29501\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500188859DAF6D3EA577\"}";
        System.out.println((new VerifySameBankTransfer()).processIntermediateAction(json));
    }
    
    public TreeMap<String, String> processIntermediateAction(String jsonData) {
        TreeMap<String, String> f = new TreeMap<>();
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map<String, String>) j.fromJson(jsonData, Map.class);
            String mn = ((String) m.get("MSISDN")).split(Pattern.quote("***"))[0];
            String bankCode = ((String) m.get("TRANS_TYPE")).toString();
            String destAcct = ((String) m.get("WALLET_NUMBER1")).toString();
            log.info("json received:: " + jsonData);
            
            String response = getBankDetailsViaUrl(bankCode, destAcct).split("\\|")[1];   
//             String response = verifySameBankTransfer(bankCode, destAcct).split("\\|")[1];
            log.info("VERIFY ACCOUNT RESPONSE::: " + response);
            
            if (!response.equals("06")) {
                f.put("TMSG", response);
                f.put(response, "");
            } else {
                f.put("TMSG", "N/A");
                f.put("", "");
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
            f.put("TMSG", "N/A");
            f.put("", "");
        }        
        return f;
    }
    
    public static String verifySameBankTransfer(String bankcode, String acct) {
        String returnValue = "06";
        String ipAddress = "172.16.30.4";
        String key = "123456";
        String expiryDate = "0000";
        int port = 8082;
        XProcessor processor = new XProcessor();
        HttpHost host = new HttpHost();
        host.setServerAddress(ipAddress);
        host.setPort(port);
        host.setSecureKey(key);
        Card card = new Card();
        card.setCardNumber(String.format("%s%s", new Object[]{bankcode, acct}));
        card.setCardExpiration(expiryDate);
        XRequest request = new XRequest();
        request.setXmlString("<CBARequest>AL</CBARequest>");
        request.setCard(card);
        request.setTransCode(TransCode.BANKSERVICE);
        XResponse response = null;
        try {
            response = processor.process(host, request);
            System.out.println("Response: " + response.getResponse());
            System.out.println("Message: " + response.getMessage());
            System.out.println("Custom XML: " + response.getCustomXml());
            System.out.println("Reference: " + response.getReference());
            if (response.getResponse() == 0) {
                returnValue = response.getCustomXml();
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return returnValue;
    }
    
        public String getBankDetailsViaUrl(String shortcode, String account) {
        String output = "06";
        String str = "";
        String bankCode = PropsCache.getInstance().getProperty(String.valueOf(String.valueOf(shortcode)) + "BC");
        String url = "";
        if (bankCode.equals("914")) {
            url = PropsCache.getInstance().getProperty("ACCOUNT_LOOKUP_URL_GA_RURAL");
        } else if (bankCode.equals("913")) {
            url = PropsCache.getInstance().getProperty("ACCOUNT_LOOKUP_URL_TALENT_RURAL");
        }
        
        System.out.println("URL::: " + url);
        log.info(("URL::: " + url));
        try {
            String response = DoHttpRequest.doGet(url.replace("#account#", account));
            log.info("Account Lookup via URL Full Response:: " + response);
            JSONObject json = new JSONObject(response);
            String error = json.getString("responseCode");
            if (error.equals("00")) {
                str = output = json.optString("lookup");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Account Lookup via URL Response:: " + output);
        return output;
    }

}
