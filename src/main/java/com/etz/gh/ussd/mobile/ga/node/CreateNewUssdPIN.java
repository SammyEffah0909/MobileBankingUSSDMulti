package com.etz.gh.ussd.mobile.ga.node;

import com.etz.gh.ussd.mobile.util.PropsCache;
import com.etz.mobile.security.AccessEncoder;
import com.etz.mobile.security.Base64Encoder;
import com.fnm.ussd.engine.util.UssdSessionTerminatorInterface;
import com.google.gson.Gson;
import com.test.gcb.util.DataPipe;
import com.test.gcb.util.HttpPost;
import com.test.gcb.util.Utils;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class CreateNewUssdPIN extends UssdSessionTerminatorInterface {
  static Logger l = Logger.getLogger(CreateNewUssdPIN.class);
  
  public String processUssdRequest(String jsonReq) {
    String rMsg = "";
    HashMap<String, String> reply = new HashMap<>();
    try {
      Gson j = new Gson();
      Map<String, String> m = (Map<String, String>)j.fromJson(jsonReq, Map.class);
      String mn = ((String)m.get("MSISDN")).split(Pattern.quote("***"))[0];
      String acct = m.get("NEWUSERACCT");
      String shortcode = ((String)m.get("SHORTCODE")).replaceAll("#", "").replaceAll("\\*", "").substring(6);
      String newPIN1 = m.get("NEWPIN1");
      String newPIN2 = m.get("NEWPIN2");
      String onboardingMsgSuffix = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "ONBOARD_SUFFIX");
      String appName = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "NAME");
      String[] acctRcd = ((String)m.get("MATCH")).split("::");
      String firstname = acctRcd[5];
      String cardNum = "";
      String maskedAlias = "";
      String msg = "";
      if (newPIN1.equals(newPIN2)) {
        if (isNumeric(newPIN2)) {
          if (doMGPINCraetion(shortcode, newPIN2, mn, acct)) {
            long a = System.currentTimeMillis();
            if (updateNewUssdUserRcd(shortcode, mn, acct, firstname)) {
              long q = System.currentTimeMillis();
              l.info("->Time To Update MobileDB SX Reg Central:" + (q - a));
              msg = String.format("Hi %s Congrats!,~You have successfully registered for %s. %s", new Object[] { firstname, appName, onboardingMsgSuffix });
              reply.put("error", "00");
              reply.put("msg", msg);
            } else {
              msg = String.format("Hi, %s~Your Registration Failed.~Kindly Contact your bank branchEND", new Object[] { firstname });
              reply.put("error", "00");
              reply.put("msg", msg);
            } 
          } else {
            msg = String.format("Hi, %s~Your Registration Failed.~Kindly Contact your bank branchEND", new Object[] { firstname });
            reply.put("error", "00");
            reply.put("msg", msg);
          } 
        } else {
          reply.put("error", "00");
          reply.put("msg", "Please ensure you enter a number as your PIN..");
        } 
      } else {
        msg = String.format("Hi, %s~Ensure your inputs are correctly matchedEND", new Object[] { firstname });
        reply.put("error", "00");
        reply.put("msg", msg);
      } 
    } catch (Exception e) {
      l.error(e, e);
    } 
    rMsg = (new Gson()).toJson(reply, Map.class);
    return rMsg;
  }
  
  public String processUssdResponse(String responseMessageFormatter, String messageToFormat) {
    Gson j = new Gson();
    Map<String, String> m = (Map<String, String>)j.fromJson(messageToFormat, Map.class);
    String sc = m.get("msg");
    return sc;
  }
  
  private boolean isNumeric(String newPIN2) {
    boolean numeric = true;
    try {
      Integer.parseInt(newPIN2);
    } catch (Exception e) {
      numeric = false;
      l.error(e, e);
    } 
    return numeric;
  }
  
  private boolean doMGPINCraetion(String shortcode, String newPIN, String mobile, String maskedAlias) {
    boolean pinDone = false;
    try {
      String minss = (new SimpleDateFormat("MMddhhmm")).format(new Date());
      String appName = PropsCache.getInstance().getProperty(shortcode).split(":")[1];
      String newPin = newPIN;
      String otp = encrytData(mobile);
      String acctno = encrytData(maskedAlias);
      String enewpin = scramblePin(newPIN, mobile);
      String email = encrytData("info@bestpoint.com");
      String payload = String.format("?=ACCSYNCP*%s %s %s %s %s %s %s %s", 
          new Object[] { appName, otp, acctno, enewpin, "000000", appName, minss, email });
      String encode = URLEncoder.encode(payload, "UTF-8");
      String url = "http://172.16.30.9:8501/MG/1.0?id=" + mobile + "&msg=" + encode;
      String pinRst = HttpPost.doRequest(url);
      l.info("doMGPINCreation() request:: " + url);
      l.info("doMGPINCreation() mobile:: " + mobile + ":: response ::" + pinRst);
      if (pinRst.toLowerCase().indexOf("success") > -1) {
        pinDone = true;
        l.info("doMGPINCraetion()::" + mobile + "::[SUCCEEDED]");
      } else {
        pinDone = false;
        l.info("doMGPINCraetion()::" + mobile + "::[FAILED]");
      } 
    } catch (Exception e) {
      l.error(e, e);
    } 
    return pinDone;
  }
  
  public String scramblePin(String initpin, String phoneNo) {
    String keygen = (new AccessEncoder()).getESACode(phoneNo, initpin);
    String pinn = encrytData(keygen);
    return pinn;
  }
  
  public String encrytData(String keygen) {
    String enc = "";
    enc = Base64Encoder.encode(keygen);
    return enc;
  }
  
  private boolean updateNewUssdUserRcd(String shortcode, String mn, String acctNo, String firstname) {
    String prefix = PropsCache.getInstance().getProperty(String.valueOf(shortcode) + "PREFIX");
    Connection con = null;
    PreparedStatement ps = null;
    boolean updated = false;
    try {
      DataSource ds = DataPipe.setupDataSource("DEMO");
      con = ds.getConnection();
      String appId = PropsCache.getInstance().getProperty(shortcode).split(":")[0];
      String sql = String.format("update m_mobile_subscriber_card set alias='%s%s', auth_by='V', request_byip= '%s', modified = now(), active = 0  where subscriber_id = (select id from m_mobile_subscriber where mobile_no = '%s' and appid=%s)", new Object[] { prefix, Utils.mask(acctNo, 2), firstname, mn, appId });
      ps = con.prepareStatement(sql);
      int z = ps.executeUpdate();
      if (z > 0) {
        updated = true;
        l.info("updateNewUssdUserRcd()::" + mn + " Upgraded");
      } else {
        l.info("updateNewUssdUserRcd()::" + mn + " Not Upgraded");
      } 
      return updated;
    } catch (Exception e) {
      l.error(e, e);
    } finally {
      try {
        ps.close();
        con.close();
      } catch (Exception exception) {}
    } 
    return updated;
  }
}
