package org.service.RendezVous;

import org.model.Utilisateurs.utilisateur;

public class SessionR {

    // Utilisateur courant connecté
    private static utilisateur currentUser;

    // Getter pour récupérer l'utilisateur connecté
    public static utilisateur getCurrentUser() {
        return currentUser;
    }

    // Setter pour définir l'utilisateur connecté
    public static void setCurrentUser(utilisateur user) {
        currentUser = user;
    }
}