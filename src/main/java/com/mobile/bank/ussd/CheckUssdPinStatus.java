package com.mobile.bank.ussd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.etz.mobile.security.Base64Encoder;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class CheckUssdPinStatus extends UssdActionClassInterface {

    static Logger l = new CheckUssdPinStatus().getLogger("MG2.0");
    private static final String KEY = PropsCache.getInstance().getProperty("KEY");
    private static final String IV = PropsCache.getInstance().getProperty("IV");
    final String URL = PropsCache.getInstance().getProperty("MG_URL");
    // final String URL = PropsCache.getInstance().getProperty("MG2URLDEMO");

    public static void main(String[] args) {
        String json = "{\"SHORTCODE\":\"*389*389*277\",\"MSISDN\":\"233558287508\"}";
        System.out.println(new CheckUssdPinStatus().processIntermediateAction(json));
        // System.out.println(maskCardNumber("0068860077650007", 4, 4));

        // String mobile = "233240000000";
        // String account = "233540003400";
        // String cardNumber = "0068860000000000";
        // String firstName = "Ralph";
        // String bankCode = "686";
        // String targetBankCode = "021";
        // String uSess = "Ralph";
        // String appName = "NIBMobile";
        // String biller = "DSTV";
        // String amount = "1";
        // String oldPin = "0000";
        // String newPin = "0000";
        // String pin = "0000";
        // int id = 2938;
        //
        // String pCode = GeneralUtils.generateRandomString(16);
        // String encryptedPin = AESUtil.AESCBCEncrypt(oldPin, KEY, pCode);
        // String encryptedOldPin = AESUtil.AESCBCEncrypt(oldPin, KEY, pCode);
        // String encryptedNewPin = AESUtil.AESCBCEncrypt(newPin, KEY, pCode);
        //
        // Transaction trnx = new Transaction();
        // trnx.setId(id);
        // trnx.setToken("TAMMD");
        // trnx.setMsisdn(mobile);
        // trnx.setAppName(appName);
        // trnx.setAmount(amount);
        // trnx.setSource(mobile);
        // trnx.setSourceBankCode(bankCode);
        // trnx.setTarget(account);
        // trnx.setTargetBankCode(targetBankCode);
        // trnx.setPin(encryptedPin);
        // trnx.setpCode(pCode);
        //
        // Gson gson = new Gson();
        // String json = gson.toJson(trnx);
        //
        // System.out.println(json);
        //
        // String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);
        //
        // Request req = new Request();
        // req.setId(mobile);
        // req.setMsg(encryptedMsg);
        //
        // String reqJson = gson.toJson(req);
        //
        // System.out.println(reqJson);
    }

    public TreeMap<String, String> processIntermediateAction(String jsonData) {
        TreeMap<String, String> f = new TreeMap();
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
            String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
            int x = Integer.parseInt(new SimpleDateFormat("hhmmss").format(new Date()).substring(4));
            long q = System.currentTimeMillis();
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
            System.out.println(sc);
            String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];
            String bankCode = PropsCache.getInstance().getProperty(sc + "BC");
            System.out.println(appName);
            Response rcd = getUserProfile(appName, mn, bankCode);
            long a = System.currentTimeMillis();
            l.info("->Time To Query MobileDB Central:" + (a - q));

            if (rcd.getError() == 0) {
                if (rcd.getmProfileList() != null && rcd.getmProfileList().size() > 0) {
                    for (int i = 0; i < rcd.getmProfileList().size(); i++) {
                        int id = rcd.getmProfileList().get(i).getId();
                        String uSess = rcd.getmProfileList().get(i).getuSess();
                        String cardNum = rcd.getmProfileList().get(i).getCardNumber();
                        f.put(String.format("%s==%s==%s_FIDJ_2", id, uSess, cardNum),
                                rcd.getmProfileList().get(i).getMaskedAccount());
                    }
                }
                // else {
                // f.put("_FIDJ_20", "NewUser");
                // }
            } else if (rcd.getError() == 6) {
                // fetched profile from mprofile but could not fetch from bank. Kindly try again
                f.put("_FIDJ_20", "NewUser");
            } else {
                l.info("Error Message:: " + rcd.getMessage());
//                l.info("Error Message:: " + encodeMsg(rcd.getMessage()));
//                String text = encodeMsg(rcd.getMessage());
                f.put("TMSG", rcd.getMessage() + "~");
                f.put("_FIDJ_100", "");
            }
        } catch (Exception e) {
            l.error(e, e);
        }
        return f;
    }

    public static String encodeMsg(String message) {
        return Base64Encoder.encode(message);
    }

    private Response getUserProfile(String appName, String msisdn, String bankCode) {
        Transaction trnx = new Transaction();
        trnx.setToken("GETPROFILE");
        trnx.setMsisdn(msisdn);
        trnx.setAppName(appName);
        trnx.setSourceBankCode(bankCode);

        Gson gson = new Gson();
        String json = gson.toJson(trnx);
        l.info("GetProfile REQ:: " + json);

        String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

        Request req = new Request();
        req.setId(msisdn);
        req.setMsg(encryptedMsg);

        String reqJson = gson.toJson(req);
        l.info("GetProfile REQ ENC:: " + reqJson);

        String url = URL + "/query";
        Response resp = DoHttpRequest.postToWS(url, reqJson);

        l.info("GetProfile resp: " + gson.toJson(resp));

        return resp;
    }
}
