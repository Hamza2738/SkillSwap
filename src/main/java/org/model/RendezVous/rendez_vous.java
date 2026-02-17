package org.model.RendezVous;

import java.time.LocalDateTime;

public class rendez_vous {

    private int id_rendez_vous;
    private int id_admin_createur;     // FK vers utilisateur.id_utilisateur
    private String titre;
    private String description;
    private LocalDateTime date_rendez_vous;
    private String type;              // 'presentiel' | 'distance'
    private String competence;
    private LocalDateTime date_creation_rendez_vous;

    public rendez_vous() {}

    public rendez_vous(int id_rendez_vous, int id_admin_createur, String titre, String description,
                       LocalDateTime date_rendez_vous, String type, String competence,
                       LocalDateTime date_creation_rendez_vous) {
        this.id_rendez_vous = id_rendez_vous;
        this.id_admin_createur = id_admin_createur;
        this.titre = titre;
        this.description = description;
        this.date_rendez_vous = date_rendez_vous;
        this.type = type;
        this.competence = competence;
        this.date_creation_rendez_vous = date_creation_rendez_vous;
    }

    public int getId_rendez_vous() { return id_rendez_vous; }
    public void setId_rendez_vous(int id_rendez_vous) { this.id_rendez_vous = id_rendez_vous; }

    public int getId_admin_createur() { return id_admin_createur; }
    public void setId_admin_createur(int id_admin_createur) { this.id_admin_createur = id_admin_createur; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate_rendez_vous() { return date_rendez_vous; }
    public void setDate_rendez_vous(LocalDateTime date_rendez_vous) { this.date_rendez_vous = date_rendez_vous; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCompetence() { return competence; }
    public void setCompetence(String competence) { this.competence = competence; }

    public LocalDateTime getDate_creation_rendez_vous() { return date_creation_rendez_vous; }
    public void setDate_creation_rendez_vous(LocalDateTime date_creation_rendez_vous) {
        this.date_creation_rendez_vous = date_creation_rendez_vous;
    }

    @Override
    public String toString() {
        return "RDV: " + titre + " (" + type + ")";
    }
}
