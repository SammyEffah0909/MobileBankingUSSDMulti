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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class VerifyTAMMDTransfer extends UssdActionClassInterface {

    static final Logger log = Logger.getLogger(VerifyTAMMDTransfer.class);

    public static void main(String[] args) {
        String json = "{\"MSISDN\":\"233209157113***1499303250\",\"PROVIDER\":\"VODAFONEGH\",\"REFERENCE\":\"ussdx-WR-2397775-C97H-429\",\"SHORTCODE\":\"*389*710#\",\"TRANS_TYPE\":\"021\",\"WALLET_NUMBER1\":\"01626960006\",\"WHOAREU\":\"NIB-240-----29501\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500188859DAF6D3EA577\"}";
        System.out.println((new VerifyTAMMDTransfer()).processIntermediateAction(json));
    }

    public TreeMap<String, String> processIntermediateAction(String jsonData) {
        TreeMap<String, String> f = new TreeMap<>();
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map<String, String>) j.fromJson(jsonData, Map.class);
            String mn = ((String) m.get("MSISDN")).split(Pattern.quote("***"))[0];
            String mmNetwork = ((String) m.get("MM_NETWORK")).toString();
            String bankCode = mmNetwork.split("-")[1];
            String destAcct = ((String) m.get("WALLET_NUMBER1")).toString();
            log.info("json received:: " + jsonData);
            String response = verifySameBankTransfer(bankCode, destAcct).split("\\|")[1];
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
        String ipAddress = "172.16.30.6";
        String key = "123456";
        String expiryDate = "0000";
        int port = 8080;
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
}
