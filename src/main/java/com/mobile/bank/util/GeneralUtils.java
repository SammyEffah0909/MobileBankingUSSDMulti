package com.mobile.bank.util;

import java.security.SecureRandom;
import java.util.Random;

import com.etz.security.util.Cryptographer;

public class GeneralUtils {

	public static String cryptPan(String pan, int encType) {
        String cryptedPan = "";
        int subIndex = 6;
        if (pan.length() == 19 || pan.length() == 25) {
            subIndex = 9;
        }
        Cryptographer crypt = new Cryptographer();
        byte[] epinblock = null;
        String mmk = "01010101010101010101010101010101";
        if (encType == 1) {
            String padValue = "FFFFFF" + pan.substring(subIndex);
            try {
                crypt.getClass();
                epinblock = crypt.doCryto(padValue, mmk, 1);
                cryptedPan = pan.substring(0, subIndex) + Cryptographer.byte2hex(epinblock);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                crypt.getClass();
                epinblock = crypt.doCryto(pan.substring(subIndex), mmk, 2);
                String decPan = Cryptographer.byte2hex(epinblock).substring(6);
                if (decPan.startsWith("FFFFFF")) {
                    decPan = decPan.substring(6);
                }
                cryptedPan = pan.substring(0, subIndex) + decPan;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cryptedPan;
    }
	
	public static String generateRandomString(int length) {
		Random RANDOM = new SecureRandom();
		String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder returnValue = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}

		return new String(returnValue);
	}
	
	public static String maskCardNumber(String value, int start, int end) {
        String message = "";

        try {
            message = value.substring(0, start) + "----" + value.substring(value.length() - end);
        } catch (Exception ex) {
            message = value;
            ex.printStackTrace();
        }
        return message;
    }
	
	/*public static String getAppName(String code) {
		String c = "";

		try {
			if (code.length() == 7) {
				c = code.substring(code.length() - 2, code.length()).replace("#", "");
			} else if (code.length() > 5 && code.length() <= 9) {
				c = code.substring(5).replace("#", "");
			} else if (code.length() > 9) {
				c = code.substring(9, code.length()).replace("#", "");
			} else {
				c = code.substring(1).replace("#", "");
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			return PropsCache.getInstance().getProperty("389");
		}

		return PropsCache.getInstance().getProperty(c);
	}*/
	
	public static String getAppName(String sc) {
		return PropsCache.getInstance().getProperty(sc).split(":")[1];
	}
	
}
