package org.model.RendezVous;

import java.time.LocalDateTime;

public class postulation {

    private int id_postulation;
    private int id_rendez_vous;
    private int id_postulant;

    private String mail_user;
    private String message;
    private String cv;

    private String status; // en_attente, accepte, refuse
    private LocalDateTime date_postulation;

    private String role;   // freelance, entrepreneur
    private String nom;
    private String prenom;
    private String titre;


    // ================= CONSTRUCTEUR VIDE =================
    public postulation() {}

    // ================= CONSTRUCTEUR COMPLET =================
    public postulation(int id_postulation,
                       int id_rendez_vous,
                       int id_postulant,
                       String mail_user,
                       String message,
                       String cv,
                       String status,
                       LocalDateTime date_postulation,
                       String role,
                       String nom,
                       String prenom) {

        this.id_postulation = id_postulation;
        this.id_rendez_vous = id_rendez_vous;
        this.id_postulant = id_postulant;
        this.mail_user = mail_user;
        this.message = message;
        this.cv = cv;
        this.status = status;
        this.date_postulation = date_postulation;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
    }

    // ================= GETTERS & SETTERS =================

    public int getId_postulation() {
        return id_postulation;
    }

    public void setId_postulation(int id_postulation) {
        this.id_postulation = id_postulation;
    }

    public int getId_rendez_vous() {
        return id_rendez_vous;
    }

    public void setId_rendez_vous(int id_rendez_vous) {
        this.id_rendez_vous = id_rendez_vous;
    }

    public int getId_postulant() {
        return id_postulant;
    }

    public void setId_postulant(int id_postulant) {
        this.id_postulant = id_postulant;
    }

    public String getMail_user() {
        return mail_user;
    }

    public void setMail_user(String mail_user) {
        this.mail_user = mail_user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDate_postulation() {
        return date_postulation;
    }

    public void setDate_postulation(LocalDateTime date_postulation) {
        this.date_postulation = date_postulation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    // ================= TO STRING =================
    @Override
    public String toString() {
        return "Postulation{" +
                "id=" + id_postulation +
                ", nom='" + nom + " " + prenom + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}