package com.mobile.bank.ussd;

import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import com.etz.mobile.security.Base64Encoder;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class FailedUssdPinStatusCheck extends UssdActionClassInterface {
    
    static Logger l = new FailedUssdPinStatusCheck().doLog();
    
    private Logger doLog() {
        return getLogger("MBUSSDMULTI");
    }
    
    @Override
    public TreeMap<String, String> processIntermediateAction(String arg) {
        l.info("Json received:: " + arg);
        // TODO Auto-generated method stub

        TreeMap<String, String> f = new TreeMap<>();
        // String message = "No request received for processing. Terminating Session.";
        try {
            Gson j = new Gson();
            Map<String, String> m = (Map) j.fromJson(arg, Map.class);
            String failedMsg = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";
            
            String message = decodeMsg(failedMsg);
            l.error("MESSAGE::: " + message);
            System.out.println("MESSAGE::: " + message);
            f.put("TMSG", message);
            f.put("", "");
        } catch (JsonSyntaxException e) {
            l.error(e, e);
        }
        return f;
    }
    
    public static String decodeMsg(String message) {
        return Base64Encoder.decode(message);
    }
    
}
