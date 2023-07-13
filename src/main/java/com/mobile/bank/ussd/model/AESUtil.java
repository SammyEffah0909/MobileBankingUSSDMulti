/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.ussd.model;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class AESUtil {
//AES only supports key sizes of 16, 24 or 32 bytes

    public static void main(String[] args) {
        String clean = "{\n"
                + "	\"startDate\":\"2018-05-01 00:00:00\",\n"
                + "	\"endDate\":\"2018-05-07 23:59:00\",\n"
                + "	\"account\":\"0554538775\",\n"
                + "	\"reference\":\"02TC61620723745\"\n"
                + "} ";
        String key = "abcdefgh90abcdef";
        String iv = "1234QWERDFGYHJUI";
        System.out.println("ORIGINAL: " + clean);
        String encrypted = AESCBCEncrypt(clean, key, iv);
        System.out.println("ENCRYPTED: " + encrypted);
        String decrypted = AESCBCDecrypt(encrypted, key, iv);
        System.out.println("DECRYPTED: " + decrypted);
    }

    public static String AESCBCEncrypt(String clean, String key, String iv) {
        try {
            IvParameterSpec _iv = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec _key = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, _key, _iv);
            byte[] encrypted = cipher.doFinal(clean.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String AESCBCDecrypt(String encrypted, String key, String iv) {
        try {
            IvParameterSpec _iv = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec _key = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, _key, _iv);
            byte[] clean = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(clean);
        } catch (Exception ex) {
            Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
