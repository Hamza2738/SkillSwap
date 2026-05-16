package org.utils;

public final class SessionContext {

    private static int userId = -1;
    private static String email = null;

    private SessionContext() {}

    public static void setUser(int id, String mail) {
        userId = id;
        email = mail;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getEmail() {
        return email;
    }

    public static boolean isLoggedIn() {
        return userId > 0 && email != null && !email.isBlank();
    }

    public static void clear() {
        userId = -1;
        email = null;
    }
}