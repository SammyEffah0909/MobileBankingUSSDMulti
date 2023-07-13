package com.etz.gh.ussd.mobile.ga.node;

import com.etz.gh.ussd.mobile.util.PropsCache;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.test.gcb.util.DataPipe;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class CheckUssdPinStatus extends UssdActionClassInterface {
  static Logger l = Logger.getLogger(CheckUssdPinStatus.class);
  
  public TreeMap<String, String> processIntermediateAction(String jsonData) {
    TreeMap<String, String> f = new TreeMap<>();
    try {
      Gson j = new Gson();
      Map<String, String> m = (Map<String, String>)j.fromJson(jsonData, Map.class);
      String mn = ((String)m.get("MSISDN")).split(Pattern.quote("***"))[0];
      int x = Integer.parseInt((new SimpleDateFormat("hhmmss")).format(new Date()).substring(4));
      long q = System.currentTimeMillis();
      String shortcode = ((String)m.get("SHORTCODE")).replaceAll("#", "").replaceAll("\\*", "").substring(6);
      ArrayList<String[]> u = getCustRcd(shortcode, mn);
      long a = System.currentTimeMillis();
      l.info("->Time To Query MobileDB Central:" + (a - q));
      String[] verifiedArry = null;
      if (u != null && u.size() > 0) {
        verifiedArry = new String[u.size()];
        int i;
        for (i = 0; i < u.size(); i++) {
          String[] sa = u.get(i);
          verifiedArry[i] = "true";
        } 
        for (i = 0; i < u.size(); i++) {
          String[] sa = u.get(i);
          if (verifiedArry[i].equals("true"))
            f.put(String.valueOf(sa[0]) + "==" + sa[3] + "==" + sa[2] + "_FIDJ_2", sa[0]); 
        } 
      } else {
        f.put("_FIDJ_20", "NewUser");
      } 
    } catch (Exception e) {
      l.error(e, e);
    } 
    return f;
  }
  
  private ArrayList<String[]> getCustRcd(String shortcode, String phoneNo) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList<String[]> hh = (ArrayList)new ArrayList<>();
    try {
      DataSource ds = DataPipe.setupDataSource("DEMO");
      con = ds.getConnection();
      String appId = PropsCache.getInstance().getProperty(shortcode).split(":")[0];
      String sql = "select alias, auth_by, card_number, request_byip from m_mobile_subscriber_card where subscriber_id in (select id from m_mobile_subscriber where mobile_no = '" + phoneNo + "' and appid=" + appId + ")";
      ps = con.prepareStatement(sql);
      rs = ps.executeQuery();
      while (rs.next()) {
        String[] rst = { " ", " ", " ", " " };
        rst[0] = rs.getString(1);
        rst[1] = rs.getString(2);
        rst[2] = rs.getString(3);
        rst[3] = (rs.getString(4) == null || rs.getString(4).trim().equals("")) ? " " : rs.getString(4);
        hh.add(rst);
        l.info("getCustRcd()::" + phoneNo + "::[" + rst[0] + "|" + rst[1] + "|" + rst[3] + "]");
      } 
    } catch (Exception e) {
      l.error(e, e);
    } finally {
      try {
        if (rs != null)
          rs.close(); 
        if (ps != null)
          ps.close(); 
        if (con != null)
          con.close(); 
      } catch (Exception e) {
        l.error(e, e);
      } 
    } 
    return hh;
  }
}
