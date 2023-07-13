/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.processor;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class ValidateAmount extends UssdActionClassInterface {
  static Logger l = (new ValidateAmount()).doLog();
  
  private Logger doLog() {
    return getLogger("justpay");
  }
  
  public TreeMap<String, String> processIntermediateAction(String jsonData) {
    TreeMap<String, String> f = new TreeMap<>();
    try {
      Gson j = new Gson();
      Map<String, String> m = (Map<String, String>)j.fromJson(jsonData, Map.class);
      String mn = ((String)m.get("MSISDN")).split(Pattern.quote("***"))[0];
      String amount = m.get("AMOUNT");
      if (Double.parseDouble(amount) <= 50.0D) {
        f.put("TMSG", "Press 1 to proceed");
        f.put("", "");
      } else {
        f.put("TMSG", "Invalid Amount Entered.END");
        f.put("", "");
      } 
    } catch (Exception e) {
      l.error(e, e);
    } 
    return f;
  }
}
