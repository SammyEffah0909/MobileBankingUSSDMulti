/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.processor;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class VerifyGIP extends UssdActionClassInterface {
  static final Logger log = Logger.getLogger(VerifyGIP.class);
  
  public static void main(String[] args) {
    String json = "{\"MSISDN\":\"233548933270***9515\",\"MMNOCODE\":\"686\", \"VASTYPE\":\"686\",\"DESTACCT\":\"233548933270\"}";
    System.out.println((new VerifyGIP()).processIntermediateAction(json));
  }
  
  public TreeMap<String, String> processIntermediateAction(String jsonData) {
    TreeMap<String, String> f = new TreeMap<>();
    try {
      Gson j = new Gson();
      Map<String, String> m = (Map<String, String>)j.fromJson(jsonData, Map.class);
      String mn = ((String)m.get("MSISDN")).split(Pattern.quote("***"))[0];
      String vastype = ((String)m.get("VASTYPE")).startsWith("999") ? ((String)m.get("VASTYPE")).substring(3) : ((String)m.get("VASTYPE")).toString();
      String mmnoCode = m.get("MMNOCODE");
      String destAcct = m.get("DESTACCT");
      log.info("json received:: " + jsonData);
      System.out.println(String.valueOf(destAcct) + "\n" + mmnoCode);
      String response = verifyAccountNumber(destAcct, vastype);
      if (!response.equals("NORESULT")) {
        f.put("TMSG", response);
        f.put("", "");
      } else {
        f.put("TMSG", "N/A");
        f.put("", "");
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return f;
  }
  
  private String verifyAccountNumber(String accountNumber, String bankCode) {
    String result = "";
    try {
      String request = "{\"apiId\":\"xportal\",\"apiSecret\":\"EAE87AA45B443279747E158C6FA5FD2C9DDD49B8BCB2726FEE89F76D679B88BD5599E3E59643EA233454C66\",\"reference\": \"gipverify999\",\"product\": \"GIP\",\"action\": \"query\",\"account\" : \"" + 
        
        String.format("%s~%s", new Object[] { accountNumber, bankCode }) + "\"" + 
        "}";
      String url = "http://172.16.30.8:7777/vasApp/webapi/vas/pay";
      result = doPost(url, request);
      JsonParser jsonParser = new JsonParser();
      JsonObject jsonObject = jsonParser.parse(result).getAsJsonObject();
      String status = jsonObject.get("status").getAsString();
      String data = jsonObject.get("otherInfo").getAsString();
      if (status.equals("00"))
        return data; 
      result = "NORESULT";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }
  
  private String doPost(String url, String postData) throws Exception {
    String result = "";
    try {
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection)obj.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setConnectTimeout(5000);
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(postData);
      wr.flush();
      wr.close();
      int responseCode = con.getResponseCode();
      BufferedReader in = null;
      if (responseCode == 200) {
        in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
      } else {
        in = new BufferedReader(
            new InputStreamReader(con.getErrorStream()));
      } 
      StringBuffer response = new StringBuffer();
      String inputLine;
      while ((inputLine = in.readLine()) != null)
        response.append(inputLine); 
      in.close();
      result = response.toString();
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return result;
    } 
  }
}
