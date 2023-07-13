package com.etz.gh.ussd.mobile.ga.node;

import com.etz.mobile.security.AccessEncoder;
import com.etz.mobile.security.Base64Encoder;
import com.test.gcb.util.DataPipe;
import com.test.gcb.util.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sql.DataSource;

public class Test {
  public static void main(String[] args) {
    String newPIN = "1111";
    String mobile = "233548933270";
    String maskedAlias = "1030306600166942";
    String firstName = "Eugene";
    if ((new Test()).doMGPINCraetion(newPIN, mobile, maskedAlias) && (
      new Test()).updateNewUssdUserRcd(mobile, maskedAlias, firstName))
      System.out.println("mission accomplished"); 
  }
  
  private boolean doMGPINCraetion(String newPIN, String mobile, String maskedAlias) {
    boolean pinDone = false;
    try {
      String minss = (new SimpleDateFormat("MMddhhmm")).format(new Date());
      String appName = "AmaMobile";
      String newPin = newPIN;
      String otp = encrytData(mobile);
      String acctno = encrytData(maskedAlias);
      String enewpin = scramblePin(newPIN, mobile);
      String email = encrytData("info@bestpoint.com");
      String payload = String.format("?=ACCSYNCP*%s %s %s %s %s %s %s %s", 
          new Object[] { appName, otp, acctno, enewpin, "000000", appName, minss, email });
      String encode = URLEncoder.encode(payload, "UTF-8");
      String url = "http://172.16.30.9:8501/MG/1.0?id=" + mobile + "&msg=" + encode;
      String pinRst = sendGet(url);
      System.out.println("doMGPINCreation() request:: " + url);
      System.out.println("doMGPINCreation() mobile:: " + mobile + ":: response ::" + pinRst);
      if (pinRst.toLowerCase().indexOf("success") > -1) {
        pinDone = true;
        System.out.println("doMGPINCraetion()::" + mobile + "::[SUCCEEDED]");
      } else {
        pinDone = false;
        System.out.println("doMGPINCraetion()::" + mobile + "::[FAILED]");
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return pinDone;
  }
  
  private boolean updateNewUssdUserRcd(String mn, String acctNo, String firstname) {
    String prefix = "NIB-";
    Connection con = null;
    PreparedStatement ps = null;
    boolean updated = false;
    try {
      DataSource ds = DataPipe.setupDataSource("DEMO");
      con = ds.getConnection();
      String appId = "91";
      String sql = String.format("update m_mobile_subscriber_card set alias='%s%s', auth_by='V', request_byip= '%s', modified = now(), active = 0  where subscriber_id = (select id from m_mobile_subscriber where mobile_no = '%s' and appid=%s)", new Object[] { prefix, Utils.mask(acctNo, 2), firstname, mn, appId });
      ps = con.prepareStatement(sql);
      int z = ps.executeUpdate();
      if (z > 0) {
        updated = true;
        System.out.println("updateNewUssdUserRcd()::" + mn + " Upgraded");
      } else {
        System.out.println("updateNewUssdUserRcd()::" + mn + " Not Upgraded");
      } 
      return updated;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        ps.close();
        con.close();
      } catch (Exception localException3) {
        localException3.printStackTrace();
      } 
    } 
    return updated;
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
  
  public static String sendGet(String url) {
    URL obj = null;
    HttpURLConnection con = null;
    StringBuffer response = null;
    try {
      obj = new URL(url);
      con = (HttpURLConnection)obj.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(9000);
      con.setDoOutput(true);
      int responseCode = con.getResponseCode();
      BufferedReader in = null;
      if (responseCode == 200) {
        in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
      } else if (responseCode == 400) {
        in = new BufferedReader(
            new InputStreamReader(con.getErrorStream()));
      } 
      response = new StringBuffer();
      String inputLine;
      while ((inputLine = in.readLine()) != null)
        response.append(inputLine); 
      in.close();
    } catch (SocketTimeoutException e) {
      return "06-->Timeout";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return response.toString();
  }
}
