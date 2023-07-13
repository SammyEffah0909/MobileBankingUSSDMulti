/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.processor;

import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class BankBillsVerify extends UssdActionClassInterface {

    static Logger l = (new BankBillsVerify()).doLog();

    private Logger doLog() {
        return getLogger("justpay");
    }

    public static void main(String[] args) {
        String json = "{\"MSISDN\":\"233548933270***9515\",\"MMNOCODE\":\"686\", \"VASTYPE\":\"GOTV\",\"DESTACCT\":\"1021091238\"}";
        System.out.println((new BankBillsVerify()).processIntermediateAction(json));
    }

    private String checkCustDetails(String phoneNo, String biller, String subscId) {
        String name = "NORESULT";
        try {
            String url = "http://172.16.30.19:8787/VerifyBill/?alias=" + biller + "&account=" + subscId + "&phone=" + phoneNo;
            String rslt = "";
            try {
                rslt = doRequest(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (rslt != null && !rslt.trim().equals("") && rslt.contains("#")) {
                name = rslt;
            } else if (rslt != null && !rslt.trim().equals("") && rslt.contains("|")) {
                name = rslt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    private String verifyBankAcct(String bankCode, String account) {
        String name = "";
        String bankName = "";
        try {
            if (bankCode.equalsIgnoreCase("006")) {
                bankName = "UMobile";
            }
            String url = "https://webpay.etranzactgh.com/3DApp/invoke.jsp?clientid=ACCT&providerid=ACCT&xmlinfo=<Request><clientID>ACCT</clientID><providerID>ACCT</providerID><providerName>ACCT</providerName><accountNumber>" + account + "</accountNumber><otherInfo>" + bankName + "~%20</otherInfo></Request>";
            String rslt = "";
            try {
                name = doRequest(url);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public TreeMap<String, String> processIntermediateAction(String jsonData) {
        TreeMap<String, String> f = new TreeMap<>();
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map<String, String>) j.fromJson(jsonData, Map.class);
            String mn = ((String) m.get("MSISDN")).split(Pattern.quote("***"))[0];
            String biller = m.get("BILLER");
            String subscriberId = m.get("BILL ACCT_1");
            String extras = m.containsKey("OTHERINFO") ? m.get("OTHERINFO") : "";
            if (biller.equalsIgnoreCase("006") || biller.equalsIgnoreCase("017")) {
                String vBank = verifyBankAcct(biller, subscriberId);
                String respCode = "";
                String fullName = "";
                try {
                    int a = vBank.indexOf("<otherInfo>") + 11;
                    int b = vBank.indexOf("</otherInfo>");
                    String otherInfo = vBank.substring(a, b);
                    String fName = otherInfo.split("~")[3];
                    String lName = otherInfo.split("~")[4];
                    fullName = String.valueOf(fName) + " " + lName;
                    int c = vBank.indexOf("<responseCode>") + 14;
                    int d = vBank.indexOf("</responseCode>");
                    respCode = vBank.substring(c, d);
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                if (respCode.equalsIgnoreCase("00")) {
                    f.put("TMSG", String.valueOf(fullName) + "\nEnter Amount(GHS):\n");
                    f.put("", "");
                } else {
                    f.put("TMSG", "Invalid account number entered. Please check entry and try again\n");
                }
                return f;
            }
            if (biller.equalsIgnoreCase("ATU")) {
                String se_type = m.get("SE_TYPE");
                String de_type = m.get("DE_TYPE");
                String hash = URLEncoder.encode("#", "UTF-8");
                subscriberId = String.valueOf(mn) + hash + se_type + hash + de_type;
                l.info("Subscriber ID:: " + subscriberId);
            }
            String rst = checkCustDetails(mn, biller, subscriberId);
            if (!rst.equals("NORESULT")) {
                String am = "";
                if (biller.equalsIgnoreCase("DSTV") || biller.equalsIgnoreCase("GOTV")
                        || biller.equalsIgnoreCase("BO")) {
                    if (Pattern.compile("[A-Z]+").matcher(rst).find()) {
                        String[] a = rst.split("\\^")[0].split("#");
                        for (int i = 0; i < a.length; i++) {
                            String x = a[i];
                            am = String.valueOf(am) + x + "~";
                        }
                        f.put("TMSG", String.valueOf(am) + "~Enter 1 to proceed~");
                        f.put("amountdue", "");
                    } else {
                        f.put("TMSG", "Bill Details Not Available\nEND");
                        f.put("", "");
                        return f;
                    }
                } else if (biller.equalsIgnoreCase("STARTIMES")) {
                    if (rst.contains("#")) {
                        String[] a = rst.split("#");
                        for (int i = 0; i < a.length; i++) {
                            String x = a[i];
                            am = String.valueOf(am) + x + "~";
                        }
                        String res = "Name: " + a[0] + "~Smart Card:" + a[1] + "~Current Package:" + a[2];
                        f.put("TMSG", String.valueOf(res) + "~Enter 1 to proceed~");
                        f.put("", "");
                    }
                } else if (rst.contains("#")) {
                    String[] a = rst.split("#");
                    for (int i = 0; i < a.length; i++) {
                        String x = a[i];
                        am = String.valueOf(am) + x + "\n";
                    }
                    f.put("TMSG", am);
                    f.put("", "");
                } else if (rst.contains("|")) {
                    String[] a = rst.split("[|]");
                    am = a[0];
                    for (int i = 1; i < a.length - 1; i++) {
                        f.put(a[i].split("@")[0], a[i].replaceAll("@", " - "));
                    }
                    f.put("TMSG", "Select An Option No:");
                }
            } else {
                f.put("TMSG", "Verification Failed\nEND");
                f.put("", "");
            }
        } catch (Exception e) {
            l.error(e, e);
        }
        return f;
    }

    public static String doRequest(String url) {
        URL urlObject = null;
        HttpURLConnection connection = null;
        String resp = "";
        try {
            urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer buildStr = new StringBuffer();
                while ((resp = in.readLine()) != null) {
                    buildStr.append(resp);
                }
                System.out.println("HttpPost::proceed() - response: " + buildStr.toString());
                resp = buildStr.toString();
            } else {
                resp = "";
            }
        } catch (Exception eee) {
            System.out.println("HttpPost::proceed()::" + eee);
        }
        return resp;
    }
}
