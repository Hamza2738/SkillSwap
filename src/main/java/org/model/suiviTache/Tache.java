package org.model.suiviTache;

import java.time.LocalDate;

public class Tache {

    private int id;
    private String titre;
    private String description;
    private String statut;
    private String priorite;
    private LocalDate echeance;
    private int projetId;
    private int userId;


    private String mailUser;

    // ✅ NEW
    private String evaluation;

    public Tache() {}

    // Ancien constructeur
    public Tache(int id, String titre, String description,
                 String statut, String priorite,
                 LocalDate echeance, int projetId, int userId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.priorite = priorite;
        this.echeance = echeance;
        this.projetId = projetId;
        this.userId = userId;
    }

    // Constructeur avec mailUser
    public Tache(int id, String titre, String description,
                 String statut, String priorite,
                 LocalDate echeance, int projetId, int userId, String mailUser) {
        this(id, titre, description, statut, priorite, echeance, projetId, userId);
        this.mailUser = mailUser;
    }

    // ✅ Nouveau constructeur complet
    public Tache(int id, String titre, String description,
                 String statut, String priorite,
                 LocalDate echeance, int projetId, int userId,
                 String mailUser, String evaluation) {
        this(id, titre, description, statut, priorite, echeance, projetId, userId, mailUser);
        this.evaluation = evaluation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public LocalDate getEcheance() { return echeance; }
    public void setEcheance(LocalDate echeance) { this.echeance = echeance; }

    public int getProjetId() { return projetId; }
    public void setProjetId(int projetId) { this.projetId = projetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMailUser() { return mailUser; }
    public void setMailUser(String mailUser) { this.mailUser = mailUser; }

    // ✅ NEW
    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
}