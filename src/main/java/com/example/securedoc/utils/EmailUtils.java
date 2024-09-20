package com.example.securedoc.utils;

public class EmailUtils {

    public static String getEmailMessage(String name, String host, String key) {
        return "Hello " + name + ",\n\nYour account has been created. Please verify your account by clicking on the link below.\n\n"
                + getVerificationUrl(host, key) + "\n\nThe Support Team";
    }

    public static String getResetPasswordMessage(String name, String host, String token) {
        return "Hello " + name + ",\n\nCLick on the link below to reset your password.\n\n"
                + getResetPasswordUrl(host, token) + "\n\nThe Support Team";
    }

    public static String getVerificationUrl(String host, String key) {
        return host + "/user/verify/account?key=" + key;
    }

    public static String getResetPasswordUrl(String host, String token) {
        return host + "/user/verify/password?token=" + token;
    }
}
