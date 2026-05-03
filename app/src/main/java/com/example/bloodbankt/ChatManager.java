package com.example.bloodbankt;

public class ChatManager {
    public static String getChatRoomId(String email1, String email2) {
        String e1 = email1.trim().replace(".", ",");
        String e2 = email2.trim().replace(".", ",");
        if (e1.compareTo(e2) < 0) {
            return e1 + "_" + e2;
        } else {
            return e2 + "_" + e1;
        }
    }
}
