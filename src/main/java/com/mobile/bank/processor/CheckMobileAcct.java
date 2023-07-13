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
import com.mobile.bank.util.PropsCache;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CheckMobileAcct extends UssdActionClassInterface {

    static Logger l = Logger.getLogger(CheckMobileAcct.class);

    public static void main(String[] args) {
        String s = "{    \"MSISDN\": \"233548933270***3074766813344848\",    \"PIN\": \"3834\",    \"PROVIDER\": \"TIGO\",    \"REFERENCE\": \"ussdx-WK-1180198-J80J-898\",    \"SHORTCODE\": \"*389*277#\",    \"NEWUSERACCT\": \"1030301601387901\",    \"WHOAREU\": \"ABII-123-----00016\\u003d\\u003dSOLOMON FOSU\\u003d\\u003d9200019A2040DB492B13CC\"}";
        System.out.println((new CheckMobileAcct()).processIntermediateAction(s));
    }

    public TreeMap<String, String> processIntermediateAction(String jsonData) {
        TreeMap<String, String> f = new TreeMap<>();
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map<String, String>) j.fromJson(jsonData, Map.class);
            String mn = ((String) m.get("MSISDN")).split(Pattern.quote("***"))[0];
            String nwusracct = m.get("NEWUSERACCT");
            String shortcode = ((String) m.get("SHORTCODE")).replaceAll("#", "").replaceAll("\\*", "").substring(6);
            l.info("SHOTCODE::: " + shortcode);
            String bankName = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "NAME");
            long q = System.currentTimeMillis();
            String[] rst = checkCustMatch(shortcode, mn, nwusracct);
            long a = System.currentTimeMillis();
            l.info("->Time To Match Phone-Account Bank WS:" + (a - q));
            if (rst[0].equals("true")) {
                f.put("1::" + rst[1] + "::" + rst[2] + "::" + rst[3] + "::" + rst[4] + "::" + rst[5],
                        "Register Me");
            } else {
                f.put("TMSG", "Your details do not match our record,~ Please visit the nearest " + bankName + " branch to regularise. Thank youEND");
                f.put("1", "");
            }
        } catch (Exception e) {
            l.error(e, e);
        }
        return f;
    }

    private String[] checkCustMatch(String shortcode, String phoneNo, String acct) {
        String[] exist = {"false", " ", " ", " ", " ", " "};
        try {
            String d = "";
            d = getBankDetailsViaUrl(shortcode, acct);
            System.out.println("d:" + d);
            l.info("VALUE D::: " + d);
            if (!d.equals("06")) {
                String[] s = d.split("\\|");
                String bankPhone = !s[0].startsWith("233") ? s[0].substring(1).trim() : s[0].substring(3).trim();
                phoneNo = phoneNo.substring(3).trim();
                if (phoneNo.equals(bankPhone)) {
                    exist[0] = "true";
                    exist[1] = "card_num";
                    exist[2] = "default_pin";
                    exist[3] = "change_pin";
                    exist[4] = "card_pin";
                    exist[5] = s[1];
                }
            }
        } catch (Exception e) {
            l.error(e, e);
        }
        return exist;
    }

//    public static String getBankDetails(String shortcode, String account) {
//        String returnValue = "06";
//        String ipAddress = "172.16.30.6";
//        String key = "123456";
//        String expiryDate = "022020";
//        int port = 8181;
//        String bankCode = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "BC");
//        XProcessor processor = new XProcessor();
//        HttpHost host = new HttpHost();
//        host.setServerAddress(ipAddress);
//        host.setPort(port);
//        host.setSecureKey(key);
//        Card card = new Card();
//        card.setCardNumber(String.format("%s%s", new Object[]{bankCode, account}));
//        card.setCardExpiration(expiryDate);
//        XRequest request = new XRequest();
//        request.setXmlString("<CBARequest>AL</CBARequest>");
//        request.setCard(card);
//        request.setTransCode(TransCode.BANKSERVICE);
//        XResponse response = null;
//        try {
//            response = processor.process(host, request);
//            System.out.println("Response: " + response.getResponse());
//            System.out.println("Message: " + response.getMessage());
//            System.out.println("Custom XML: " + response.getCustomXml());
//            if (response.getResponse() == 0) {
//                returnValue = response.getCustomXml();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return returnValue;
//    }
    public static String getBankDetails(String shortcode, String account) {
        String returnValue = "06";
        String ipAddress = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
        String key = "123456";
        String expiryDate = "022020";
        String sport = PropsCache.getInstance().getProperty("AUTOSWITCH_PORT");
        int port = Integer.parseInt(sport);
//        String context = PropsCache.getInstance().getProperty("AUTOSWITCH_CONTEXT");
        String bankCode = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "BC");
        XProcessor processor = new XProcessor();
        HttpHost host = new HttpHost();
        host.setServerAddress(ipAddress);
        host.setPort(port);
        host.setSecureKey(key);
//        if (onCloud.equals("1")) {
//            host.setContext(context);
//        }
        Card card = new Card();
        card.setCardNumber(String.format("%s%s", new Object[]{bankCode, account}));
        card.setCardExpiration(expiryDate);
        System.out.println("NB" + String.format("%s%s", new Object[]{bankCode, account}));
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
        l.info(("URL::: " + url));
        try {
            String response = DoHttpRequest.doGet(url.replace("#account#", account));
            l.info("Account Lookup via URL Full Response:: " + response);
            JSONObject json = new JSONObject(response);
            String error = json.getString("responseCode");
            if (error.equals("00")) {
                str = output = json.optString("lookup");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        l.info("Account Lookup via URL Response:: " + output);
        return output;
    }
}
