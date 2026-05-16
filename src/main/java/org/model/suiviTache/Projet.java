package org.model.suiviTache;

import java.time.LocalDate;

public class Projet {

    private int id;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private int userId;

    // ✅ NEW
    private String mailUser;

    public Projet() {}

    // ✅ ancien constructeur conservé (si tu l’utilises ailleurs)
    public Projet(int id, String titre, String description,
                  LocalDate dateDebut, LocalDate dateFin,
                  String statut, int userId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.userId = userId;
    }

    // ✅ nouveau constructeur avec mailUser
    public Projet(int id, String titre, String description,
                  LocalDate dateDebut, LocalDate dateFin,
                  String statut, int userId, String mailUser) {
        this(id, titre, description, dateDebut, dateFin, statut, userId);
        this.mailUser = mailUser;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // ✅ NEW getters/setters
    public String getMailUser() { return mailUser; }
    public void setMailUser(String mailUser) { this.mailUser = mailUser; }
}