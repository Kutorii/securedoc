package com.example.securedoc.domain;

public class RequestContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void run() {
        USER_ID.remove();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }
}
