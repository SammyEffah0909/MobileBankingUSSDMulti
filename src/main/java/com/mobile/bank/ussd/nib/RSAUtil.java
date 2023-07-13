package com.mobile.bank.ussd.nib;

/**
 *
 * @author seth.sebeh
 */
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;

public class RSAUtil {
    public static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtDwD7Q4SSMPGwGgGNRpB8zHCUiGDSswa0ekZNpIHT8u3D1z7bao3A10i2Rky1cWd/MF8xiyRkPn6zQ4SABswROLmawk5SuBihlddS30WMi/4mloM4NoMatp/vofrBJIX2ArZUhV1mFS1YstiGg+7KFHNI0SaQuXuVuXvNc81FzcNeorGMrKhMfDtsexkLE2VIT9J9C8ZuyOBM5/Hr1rC+Tu9GAwJ2wGmQuqSjUteTcC7ZrEIMegqg3j//9uxg6HD7OkNFicmQ9n5n/SYdJ57JPKQ8K988uBvHU2DMxLvTFrfjsxu/s5T9NDTUcW+iJWMlv83NblPfFyDh9fsCxoUxwIDAQAB";
    public static String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0PAPtDhJIw8bAaAY1GkHzMcJSIYNKzBrR6Rk2kgdPy7cPXPttqjcDXSLZGTLVxZ38wXzGLJGQ+frNDhIAGzBE4uZrCTlK4GKGV11LfRYyL/iaWgzg2gxq2n++h+sEkhfYCtlSFXWYVLViy2IaD7soUc0jRJpC5e5W5e81zzUXNw16isYysqEx8O2x7GQsTZUhP0n0Lxm7I4Ezn8evWsL5O70YDAnbAaZC6pKNS15NwLtmsQgx6CqDeP//27GDocPs6Q0WJyZD2fmf9Jh0nnsk8pDwr3zy4G8dTYMzEu9MWt+OzG7+zlP00NNRxb6IlYyW/zc1uU98XIOH1+wLGhTHAgMBAAECggEAb9KQiQJi+7xj0rp3p3i8FDv6GWTrhXuu5C+gUgg1+x8kW6bP5BFognDFjzagIJu0Ur5lQLskMXO3VIP3TRORVhwE48HcRwWCe2E25AAexlKXBoT6WoNOkvkvqrG/cZ2Gp6bLgK8Jv9JGm54eJpc+Al0GTmxg3UK1JjsQuCzv/o9BbF75DP9PNmwA1GnSWNt8GFshkpyYxITlRfcSqEtqY6oLw8u4smRczZYW2asI4v1FfeeUbBetpTYi9gnmz5WzZtaY46IS/KaWnd9ntWpiFLvHhyP63uoC+Xh6khL2lJB0zM2FyEcyPHZWDCvWuB6m6olF17ccw89U2gGv2MeiYQKBgQDlsIBw03Rt8yWzD5kE3MzdcN5PMrwXk9D0A2IkNh0SLgt4YQ5yXi0ewwaPWjhVbcAD4grs17LHRkvC+pxuGnhRvHFHrkurvEZrR4RT+RaXZFlHXTHuhedY0vh+nd65KfZ+p8WHBxcGEsgDeMjgLk6GV15BDoOF4CcidkoLc9LElwKBgQDI4Ua4v2YdgDBKSh18mFeBJekN9b+sAvWJTGTlcLp3Rfg4Fgs+nPbilv9dngfkxH0L46Ky3EN3kb7TNaHnLzxgykpt6MepTdOD10U/9pJ58RQbekWd3s3/i9uLHIUHUHP/Gw4EUdFQ3o/OgN0rSh6ituxkGxpvU52RHGR4PrxHUQKBgQC4pnI7QeMh2Qz/rJRpclBH3Ur7BGBt2+lpoaauUcqBKP8ToYMJsmg3iLWnXPJYy0hIVtpNmQIibYLOlsZXRXRg0UuIQriWsV8zM2VQVLkyr/uOAX/4rzFTGReoQqYg2XlOTPXhHXQG7+ZX1Cw4/UEngulFB7P4VkFf1RSlrFFOtQKBgAqjm8pHINJpKqWUnVEvBFtrtZcur9MdaQG3kRRxzv5oL0hRKpFLYOwJUUcVCpf8LB9IFCLi9ZPMnU20ZnnHD6yUqjJ+BK0b2Rr/eMR1e7aHpSf7ZtqApWpmbt9ESGtlwG7/cNObvblJwFYQjyYuy8aUZ5q5sMG6rPq1hUC1GeWxAoGAJ2vyoYViN1NLQVH11Wp2uCe4NB6K2DjibFNI5DhrDt/ZMDsmTUXrfmW/Gudp3/EojsLhY92r92mlY2qWvrJnoKAs2MyyaSIeqzvioYjMSqrK8nrEqM+kqjxFrTzceKFjskSiklN606Dnq+5qfvJRzmhRI1rfFXASbfzmy2egT4k=";
    
    public static PublicKey getPublicKey(String base64PublicKey) {
        PublicKey publicKey = null;
        try {
//            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
        	X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(base64PublicKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(base64PrivateKey));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static byte[] encrypt(String data, String publicKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
        return cipher.doFinal(data.getBytes());
    }

    public static String decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data));
    }

    public static String decrypt(String data, String base64PrivateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
//        return decrypt(Base64.getDecoder().decode(data.getBytes()), getPrivateKey(base64PrivateKey));
    	return decrypt(DatatypeConverter.parseBase64Binary(data), getPrivateKey(base64PrivateKey));
    }

    public static void main(String[] args) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        try {
        	long start = System.currentTimeMillis();
        	System.out.println("TAT:: " + start);
//            String encryptedString = Base64.getEncoder().encodeToString(encrypt("{\"phone_number\": \"233209157113\",\"country_code\": \"233\"}", publicKey));
        	
        	String s = "{" + 
        			"\"srcAccount\":\"02001103920\"," + 
        			"\"amount\":\"5\"," + 
        			"\"destAcct\":\"2323456786789\"," + 
        			"\"destBank\":\"300315\"," + 
        			"\"narration\":\"etztest\"," + 
        			"\"productCode\":\"GIPO\"," + 
        			"\"reference\":\"Nara839200\"," + 
        			"\"channelCode\":\"100\"," + 
        			"\"srcAccountName\":\"Nii\"" + 
        			"}";
//        	String encryptedString = Base64.getEncoder().encodeToString(encrypt(s, publicKey));
        	String encryptedString = DatatypeConverter.printBase64Binary(encrypt(s, publicKey));
        	System.out.println(encryptedString);
            System.out.printf("Encrypted String:: %s", encryptedString);
        	
            String decryptedString = RSAUtil.decrypt(encryptedString, privateKey);
            System.out.println();
            long stop = System.currentTimeMillis();
            System.out.println("TAT:: " + (stop - start));
            System.out.printf("Decrypted String:: %s", decryptedString);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }

    }
}