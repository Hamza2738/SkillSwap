package org.utils;

import org.model.Utilisateurs.utilisateur;

public final class Session {

    private static utilisateur currentUser;

    private Session() {
        // Empêche l'instanciation
    }

    public static void setCurrentUser(utilisateur user) {
        currentUser = user;
    }

    public static utilisateur getCurrentUser() {
        return currentUser;
    }

    public static String getEmail() {
        return (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }

}