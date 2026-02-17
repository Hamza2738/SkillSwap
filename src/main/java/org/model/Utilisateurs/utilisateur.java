package org.model.Utilisateurs;

import org.model.Competences.Competence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class utilisateur {

    private int id_utilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String mot_de_passe;
    private String telephone;
    private String photo_profil;
    private String role; // freelance / entrepreneur / admin
    private LocalDateTime date_inscription;
    private String statut; // actif / suspendu / supprimé

    // ✅ Relation (Many-to-Many)
    private List<Competence> competences = new ArrayList<>();

    public utilisateur() {}

    // --- getters/setters ---
    public int getId_utilisateur() { return id_utilisateur; }
    public void setId_utilisateur(int id_utilisateur) { this.id_utilisateur = id_utilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMot_de_passe() { return mot_de_passe; }
    public void setMot_de_passe(String mot_de_passe) { this.mot_de_passe = mot_de_passe; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getPhoto_profil() { return photo_profil; }
    public void setPhoto_profil(String photo_profil) { this.photo_profil = photo_profil; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getDate_inscription() { return date_inscription; }
    public void setDate_inscription(LocalDateTime date_inscription) { this.date_inscription = date_inscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    // ✅ getters/setters relation
    public List<Competence> getCompetences() { return competences; }
    public void setCompetences(List<Competence> competences) { this.competences = competences; }

    // ✅ helpers (pratique)
    public void addCompetence(Competence c) {
        if (c != null) competences.add(c);
    }
}
