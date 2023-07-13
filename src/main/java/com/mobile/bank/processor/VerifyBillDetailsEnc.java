/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.processor;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class VerifyBillDetailsEnc extends UssdActionClassInterface {
  static Logger l = (new VerifyBillDetailsEnc()).doLog();
  
  static Properties props = new Properties();
  
  static String VerifyUrl = "";
  
  static {
    try {
      props.load(new FileInputStream(new File("cfg/verifybill.properties")));
      VerifyUrl = props.getProperty("VERIFYURL");
    } catch (Exception e) {
      l.error(e, e);
    } 
  }
  
  private Logger doLog() {
    return getLogger("422STAR1");
  }
  
  public TreeMap<String, String> processIntermediateAction(String jsonData) {
    VerifyBillDetailsEnc.l.info("VerifyBillDetails:::Received:" + jsonData);
    TreeMap<String, String> f = new TreeMap<>();
    try {
      Gson j = new Gson();
      Map<String, String> m = (Map<String, String>)j.fromJson(jsonData, Map.class);
      String mn = ((String)m.get("MSISDN")).split(Pattern.quote("***"))[0];
      String ref = m.get("REFERENCE");
      String otherinfo = m.containsKey("OTHERINFO") ? m.get("OTHERINFO") : "";
      String shortcode = ((String)m.get("SHORTCODE")).replaceAll("#", "");
      String[] sc = shortcode.split(Pattern.quote("*"));
      String mc = m.get("VASTYPE"), VASTYPE = mc;
      if (VASTYPE == null) {
        int l = sc.length;
        mc = sc[l - 1];
      } 
      VerifyBillDetailsEnc.l.info("GCB.MM.BILL Merchant -> " + mc);
      VASTYPE = mc;
      String DESTACCT = m.get("DESTACCT");
      String verifyData = m.containsKey("VERIFYDATA") ? ((String)m.get("VERIFYDATA")).toString() : "";
      String rst = "";
      if (VASTYPE.equals("TELESOL") || VASTYPE.equals("SURF") || VASTYPE.equals("BOLT") || 
        VASTYPE.equals("BUSY")) {
        VerifyBillDetailsEnc.l.info("Verify Data:: " + verifyData);
        if (verifyData.equals("")) {
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, "1", otherinfo);
        } else {
          int page = verifyData.charAt(1);
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, (
              new StringBuilder(String.valueOf(Character.getNumericValue(page)))).toString(), otherinfo);
        } 
        VerifyBillDetailsEnc.l.info("Formatted Response:: " + rst);
        if (!rst.equals("NORESULT")) {
          String am = "";
          if (rst.contains("#")) {
            if (rst.contains("^")) {
              String display = rst.split(Pattern.quote("^"))[0];
              String[] a = display.split("#");
              for (int i = 0; i < a.length; i++) {
                String x = a[i];
                am = String.valueOf(String.valueOf(am)) + x + "\n";
              } 
            } 
            f.put("TMSG", String.valueOf(String.valueOf(am)) + "\n");
            f.put(rst, "");
          } else if (rst.contains("|")) {
            String[] a2 = rst.split("[|]");
            am = a2[0];
            for (int k = 1; k < a2.length; k++)
              f.put(a2[k].split("~")[0], a2[k].split("~")[1].replaceAll("~", " - ")); 
            f.put("TMSG", "Select An Option No:");
          } 
        } else {
          f.put("TMSG", "Sorry, there's a problem verifying your details.\nEND");
          f.put("", "");
        } 
      } else {
        if (VASTYPE.equals("6FFA")) {
          double amount = Integer.parseInt(DESTACCT) * 0.5D;
          f.put("Face of Faith Africa#0255034026#Enter 1 to proceed^6FFA#2#0#1#NA|" + amount, "");
          f.put("TMSG", "Amount: GHS " + amount + "\n Press 1 to Continue\n");
          return f;
        } 
        if (VASTYPE.equals("6AKA")) {
          double amount = Integer.parseInt(DESTACCT) * 0.5D;
          f.put("AKATSICO SRC#12345678908#Enter 1 to proceed^6AKA#2#0#1#NA|" + amount, "");
          f.put("TMSG", "Amount: GHS " + amount + "\n Press 1 to Continue\n");
          return f;
        } 
        if (VASTYPE.equals("STARTIMES")) {
          String am = "";
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, "", otherinfo);
          if (!rst.equals("NORESULT")) {
            if (rst.contains("#")) {
              String[] a2 = rst.split("#");
              for (int k = 0; k < a2.length; k++) {
                String x2 = a2[k];
                am = String.valueOf(String.valueOf(am)) + x2 + "~";
              } 
              String res = "Name: " + a2[0] + "~Smart Card:" + a2[1] + "~Current Package:" + a2[2];
              f.put("TMSG", String.valueOf(String.valueOf(res)) + "~Enter 1 to proceed~");
              f.put("", "");
            } 
          } else {
            f.put("TMSG", "Sorry, there's a problem verifying your details.\nEND");
            f.put("", "");
          } 
          return f;
        } 
        if (VASTYPE.equalsIgnoreCase("ArkFund") || VASTYPE.equalsIgnoreCase("BFund") || 
          VASTYPE.equalsIgnoreCase("EdiFundT1") || VASTYPE.equalsIgnoreCase("EdiFundT2") || 
          VASTYPE.equalsIgnoreCase("Epack") || VASTYPE.equalsIgnoreCase("MFund") || 
          VASTYPE.equalsIgnoreCase("DhaFund")) {
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, "", otherinfo);
          if (!rst.equals("NORESULT") && rst.contains("#")) {
            String[] a3 = rst.split("#", -1);
            if (!a3[2].isEmpty() || !a3[3].isEmpty()) {
              String res2 = String.format("%s~%s~%s~%s", new Object[] { a3[0], a3[3], a3[2], 
                    "Enter Amount to Proceed to Pay" });
              f.put("TMSG", res2);
              f.put(rst, "");
            } 
          } 
          return f;
        } 
        if (VASTYPE.equals("ADSL")) {
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, "", otherinfo);
          if (!rst.equals("NORESULT")) {
            f.put("TMSG", rst.split("#")[6]);
            f.put(rst.split("#")[6], "");
          } else {
            f.put("TMSG", "Sorry, there's a problem verifying your details.~END");
          } 
        } else if (VASTYPE.equals("100")) {
          rst = checkCustDetails(mn, "6TOTAL", DESTACCT, ref, "", "", "");
          if (!rst.equals("NORESULT")) {
            String am = "";
            if (rst.contains("^")) {
              String display = rst.split(Pattern.quote("^"))[0];
              String[] a = display.split("#");
              for (int i = 0; i < a.length; i++) {
                String x = a[i];
                am = String.valueOf(String.valueOf(am)) + x + "~";
              } 
            } 
            f.put("TMSG", String.valueOf(String.valueOf(am)) + "~");
            f.put(rst, "");
          } else {
            f.put("TMSG", "Sorry, there's a problem verifying your details.~END");
          } 
        } else {
          rst = checkCustDetails(mn, VASTYPE, DESTACCT, ref, shortcode, "", otherinfo);
          if (!rst.equals("NORESULT")) {
            String am = "";
            if (rst.contains("#")) {
              if (rst.contains("^")) {
                String display = rst.split(Pattern.quote("^"))[0];
                String[] a = display.split("#");
                for (int i = 0; i < a.length; i++) {
                  String x = a[i];
                  am = String.valueOf(String.valueOf(am)) + x + "\n";
                } 
              } 
              if (VASTYPE.equals("6ATUG")) {
                String stdInfo = rst.split(Pattern.quote("^"))[0];
                String[] stdInfoParts = stdInfo.split("#");
                String name = stdInfoParts[2];
                String instruction = stdInfoParts[3];
                String output = "";
                char lastLetter = DESTACCT.toLowerCase().charAt(DESTACCT.length() - 1);
                VerifyBillDetailsEnc.l.info("Last Letter:: " + lastLetter);
                if (lastLetter != 'b') {
                  output = "Sorry you cannot make payment.";
                  f.put("TMSG", output);
                  f.put("", "");
                } else {
                  int amount2 = calcAmount(lastLetter);
                  String amountOut = "~Graduation Fee: GHS " + amount2 + 
                    ".00~Transaction Fee: GHS 5.00";
                  output = String.valueOf(String.valueOf(name)) + amountOut + instruction;
                  VerifyBillDetailsEnc.l.info("Output: " + output);
                  VerifyBillDetailsEnc.l
                    .info("To pass as Amount:: " + rst + "|" + amount2);
                  f.put(String.valueOf(String.valueOf(rst)) + "|" + amount2, "");
                  f.put("TMSG", output);
                } 
                VerifyBillDetailsEnc.l.info("output:: " + output);
              } else {
                f.put("TMSG", String.valueOf(String.valueOf(am)) + "\n");
                VerifyBillDetailsEnc.l.info("am = " + am);
                f.put(rst, "");
              } 
            } else if (rst.contains("|")) {
              String[] a2 = rst.split("[|]");
              am = a2[0];
              for (int k = 1; k < a2.length; k++)
                f.put(a2[k].split("~")[0], a2[k].split("~")[1].replaceAll("~", " - ")); 
              f.put("TMSG", "Select An Option No:");
            } 
          } else {
            f.put("TMSG", "Sorry, there's a problem verifying your details.\nEND");
            f.put("", "");
          } 
        } 
      } 
    } catch (Exception e) {
      VerifyBillDetailsEnc.l.error(e, e);
    } 
    return f;
  }
  
  private int calcAmount(char lastLetter) {
    if (lastLetter == 'd')
      return 220; 
    return 350;
  }
  
  private String checkCustDetails(String phoneNo, String biller, String subscId, String ref, String shortCode, String pageNumber, String otherinfo) {
    String name = "NORESULT";
    try {
      String url = String.valueOf(String.valueOf(VerifyUrl)) + "/VerifyBill/?alias=" + biller + 
        "&account=" + URLEncoder.encode(subscId, "UTF-8") + "&phone=" + phoneNo + "&otherinfo=" + 
        otherinfo + "&shortcode=" + shortCode + "&page=" + pageNumber;
      String rslt = doPOSTRequest(url, "");
      l.info("This is the result from Verification: " + rslt);
      if (rslt != null && !rslt.trim().equals("") && rslt.contains("#")) {
        name = rslt;
      } else if (rslt != null && !rslt.trim().equals("") && rslt.contains("|")) {
        name = rslt;
      } else {
        name = "NORESULT";
      } 
    } catch (Exception e) {
      name = "NORESULT";
      l.error("An error occurred ", e);
    } 
    return name;
  }
  
  public String doPOSTRequest(String url, String data) {
    String resp = "-1";
    HttpURLConnection conn = null;
    try {
      URL uri = new URL(url);
      conn = (HttpURLConnection)uri.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Charset", "utf-8");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setAllowUserInteraction(true);
      conn.setReadTimeout(9000);
      l.info(" - Request Url: " + url);
      l.info(" - Request Data: " + data);
      DataOutputStream output = new DataOutputStream(conn.getOutputStream());
      output.writeBytes(data);
      output.flush();
      output.close();
      StringBuffer sb = new StringBuffer();
      int responseCode = conn.getResponseCode();
      l.info(" - HttpCode: " + responseCode);
      if (responseCode >= 200 && responseCode < 400) {
        l.info(" - Reading from Server: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((resp = in.readLine()) != null)
          sb.append(resp); 
        in.close();
        l.info(" - response: " + sb.toString());
      } else {
        l.info(" - Reading from Server: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        while ((resp = in.readLine()) != null)
          sb.append(resp); 
        in.close();
        l.info(" - response: " + sb.toString());
      } 
      return sb.toString();
    } catch (Exception ex) {
      l.error("An error occurred ", ex);
      try {
        if (conn != null)
          conn.disconnect(); 
      } catch (Exception exConn) {
        resp = "-2";
        l.error("An error occurred ", exConn);
      } 
    } finally {
      try {
        if (conn != null)
          conn.disconnect(); 
      } catch (Exception exConn) {
        resp = "-2";
        l.error("An error occurred ", exConn);
      } 
    } 
    return resp;
  }
  
  public static void main(String[] args) {
    String l = "{\"DESTACCT\":\"P0203227\",\"LOGIN\":\"A|NIB-1402078178101\",\"MSISDN\":\"233542023469***104212996\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-ZT-1429802-A29A-502\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"BUNDLE\",\"VASTYPE\":\"ADSL\",\"WHOAREU\":\"NIB-1402078178101\\u003d\\u003dADU-OKYERE GRACE FOSUAH\\u003d\\u003d005001660A1C444A5CAE62,NIB-2402078178101\\u003d\\u003dnull\\u003d\\u003d005001FD6B148338C9C01F,\"}";
    String re = "{\"MSISDN\":\"233209157113\",\"DESTACCT\":\"264\",\"SHORTCODE\":\"*252*100#\"}";
    String v = "{\"DESTACCT\":\"7124\",\"MSISDN\":\"233548933270***105085880\",\"PROVIDER\":\"MTNSDP252\",\"REFERENCE\":\"ussdx-AT-4619162-H19G-071\",\"SHORTCODE\":\"*422*400\",\"VASTYPE\":\"EPACK\"}";
    System.out.println((new VerifyBillDetailsEnc()).processIntermediateAction("{\"DESTACCT\":\"7124\",\"MSISDN\":\"233548933270***105085880\",\"PROVIDER\":\"MTNSDP252\",\"REFERENCE\":\"ussdx-AT-4619162-H19G-071\",\"SHORTCODE\":\"*422*400\",\"VASTYPE\":\"EPACK\"}"));
  }
}
