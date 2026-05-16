package org.model.Utilisateurs;

import java.time.LocalDateTime;

public class Connexion {

    private int id;
    private int idDemandeur;
    private int idReceveur;
    private String statut;
    private LocalDateTime dateDemande;

    // ===== GETTERS =====
    public int getId() { return id; }
    public int getIdDemandeur() { return idDemandeur; }
    public int getIdReceveur() { return idReceveur; }
    public String getStatut() { return statut; }
    public LocalDateTime getDateDemande() { return dateDemande; }

    // ===== SETTERS =====
    public void setId(int id) { this.id = id; }
    public void setIdDemandeur(int idDemandeur) { this.idDemandeur = idDemandeur; }
    public void setIdReceveur(int idReceveur) { this.idReceveur = idReceveur; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }
}