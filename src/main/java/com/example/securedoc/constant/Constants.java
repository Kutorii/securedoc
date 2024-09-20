package com.example.securedoc.constant;

public class Constants {
    public static final String JWT_AUDIENCE = "EXAMPLE SECUREDOC";
    public static final String JWT_TYPE = "JWT";
    public static final String TYPE = "typ";
    public static final String EMPTY_VALUE = "empty";
    public static final String ISSUER = "securedoc";
    public static final String MFA_AUTH = "MFA_AUTH";
    public static final String DOCUMENT_ROOT_LOCATION = "./documents";

    public static final String LOGIN_PATH = "/user/login";
    public static final int NINETY_DAYS = 90;
    public static final int MAXIMUM_LOGIN_ATTEMPTS = 3;

    public static final long CONFIRMATION_SCHEDULED_TASK_RATE = 24 * 60 * 60 * 1000; // 1 jour
    public static final long MAXIMUM_CONFIRMATION_DURATION = 10; // 10 minutes

    public static final String AUTHORITIES = "authorities";
    public static final String ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";
    public static final String USER_AUTHORITIES = "document:create,document:read,document:update,document:delete";
    public static final String ADMIN_AUTHORITIES = "user:create,user:read,user:update,document:create,document:read,document:update,document:delete";
    public static final String SUPER_ADMIN_AUTHORITIES = "user:create,user:read,user:update,user:delete,document:create,document:read,document:update,document:delete";
    public static final String MANAGER_AUTHORITIES = "document:create,document:read,document:update,document:delete";
}
