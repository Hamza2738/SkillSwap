package org.model.suiviTache;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class suivi_tache {

    private int idTache;
    private String titre;
    private String description;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    // valeurs: faible / moyenne / haute
    private String priorite;

    // valeurs: a_faire / en_cours / terminee / bloquee
    private String statut;

    private int idCreateur;
    private int idAssigner;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public suivi_tache() {}

    public suivi_tache(int idTache, String titre, String description,
                       LocalDate dateDebut, LocalDate dateFin,
                       String priorite, String statut,
                       int idCreateur, int idAssigner,
                       LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.idTache = idTache;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.priorite = priorite;
        this.statut = statut;
        this.idCreateur = idCreateur;
        this.idAssigner = idAssigner;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    public int getIdTache() { return idTache; }
    public void setIdTache(int idTache) { this.idTache = idTache; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdCreateur() { return idCreateur; }
    public void setIdCreateur(int idCreateur) { this.idCreateur = idCreateur; }

    public int getIdAssigner() { return idAssigner; }
    public void setIdAssigner(int idAssigner) { this.idAssigner = idAssigner; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "SuiviTache{" +
                "idTache=" + idTache +
                ", titre='" + titre + '\'' +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                ", idCreateur=" + idCreateur +
                ", idAssigner=" + idAssigner +
                '}';
    }
}
