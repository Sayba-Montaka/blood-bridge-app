package com.example.bloodbankt;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MyMethod {
    public static String MY_KEY = "";

    public static String encryptedData(String text) throws Exception {
        byte[] textBytes = text.getBytes("UTF-8");
        String pass = "abcdEFGHijklmnop";
        byte[] passBytes = pass.getBytes("UTF-8");

        // Fix: Specify the algorithm explicitly to match PHP
        SecretKeySpec secretKeySpec = new SecretKeySpec(passBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Match PHP's default padding
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] securedBytes = cipher.doFinal(textBytes);

        String encodedString = Base64.encodeToString(securedBytes, Base64.NO_WRAP);
        return encodedString;
    }
}
