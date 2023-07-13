package com.mobile.bank.ussd.nib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VerifyGIP extends UssdActionClassInterface {
	
	static final Logger log = Logger.getLogger(VerifyGIP.class);
	
	public static void main(String [] args) {
		String json = "{\"MSISDN\":\"233548933270***9515\",\"MMNOCODE\":\"686\", \"MM_NETWORK\":\"686\",\"DESTACCT\":\"233548933270\"}";
		
		System.out.println(new VerifyGIP().processIntermediateAction(json));
//		System.out.println(new VerifyGIP().verifyMomoNumber("233548933270", "686"));
	}
	
	@Override
	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();
		
		try {
			Gson j = new Gson();
		    Map<String, String> m = (Map)j.fromJson(jsonData, Map.class);
		    
		    String mn = m.get("MSISDN").split(java.util.regex.Pattern.quote("***"))[0];
		    String vastype = m.get("MM_NETWORK").startsWith("999") ? m.get("MM_NETWORK").substring(3) : m.get("MM_NETWORK").toString();
		    String destAcct = m.get("DESTACCT");
		    
		    log.info("json received:: " + jsonData);
		    
		    System.out.println(destAcct + "~" + vastype);
		    String response = verifyAccountNumber(destAcct, vastype);
		    
		    if(!response.equals("NORESULT")) {
		    	f.put("TMSG", response);
		    	f.put(response,"");
		    }else {
		    	f.put("TMSG", "N/A");
		    	f.put("","");
		    }
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return f;
	}
	
	private String verifyAccountNumber(String accountNumber, String bankCode) {
		  String result = "";
		  
		  try {
			  String request = "{" + 
			  		"\"apiId\":\"xportal\"," + 
			  		"\"apiSecret\":\"EAE87AA45B443279747E158C6FA5FD2C9DDD49B8BCB2726FEE89F76D679B88BD5599E3E59643EA233454C66\"," + 
			  		"\"reference\": \"gipverify999\"," + 
			  		"\"product\": \"GIP\"," + 
			  		"\"action\": \"query\"," + 
			  		"\"account\" : \""+ String.format("%s~%s", accountNumber, bankCode) +"\"" + 
			  		"}";
			  		
			  
			  String url = "http://172.16.30.8:7777/vasApp/webapi/vas/pay";
			  result = doPost(url, request);
			  
			  JsonParser jsonParser = new JsonParser();
			  JsonObject jsonObject = jsonParser.parse(result).getAsJsonObject();
			  String status = jsonObject.get("status").getAsString();
			  String data = jsonObject.get("otherInfo").getAsString();
			  
			  if(status.equals("00")) {
				  return data;
			  }else {
				  result = "NORESULT";
			  }
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
		  
		  return result;
	  }
	
	private String doPost(String url, String postData) throws Exception {
		String result = "";

      try {
      	  URL obj = new URL(url);
          HttpURLConnection con = (HttpURLConnection) obj.openConnection();

          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/json");
          con.setConnectTimeout(5000);
          // Send post request
          con.setDoOutput(true);
          DataOutputStream wr = new DataOutputStream(con.getOutputStream());
          wr.writeBytes(postData);
          wr.flush();
          wr.close();

          int responseCode = con.getResponseCode();
          BufferedReader in = null;
          
          if(responseCode == 200) {
          	in = new BufferedReader(
                      new InputStreamReader(con.getInputStream()));
              
          }else{
          	in = new BufferedReader(
                      new InputStreamReader(con.getErrorStream()));
          }
          
          String inputLine;
          StringBuffer response = new StringBuffer();

          while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
          }
          in.close();
          
          result = response.toString();
          
          return result;
      }catch(Exception e) {
      	e.printStackTrace();
      }
      
      return result;
  }

}
