package org.model.Competences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDateTime;

public class HistoriqueCompetence {

    private int id;
    private int competenceId;

    private String category;
    private String type;
    private String description;

    private String niveau;
    private int anneesExperience;
    private String certification;
    private String statut;

    private String email;

    private String statusChange;
    private LocalDateTime dateModification;

    // ✅ Checkbox selection (JavaFX)
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public HistoriqueCompetence() {}

    public HistoriqueCompetence(int id, int competenceId,
                                String category, String type, String description,
                                String niveau, int anneesExperience,
                                String certification, String statut,
                                String email,
                                String statusChange,
                                LocalDateTime dateModification) {

        this.id = id;
        this.competenceId = competenceId;
        this.category = category;
        this.type = type;
        this.description = description;
        this.niveau = niveau;
        this.anneesExperience = anneesExperience;
        this.certification = certification;
        this.statut = statut;
        this.email = email;
        this.statusChange = statusChange;
        this.dateModification = dateModification;
    }

    /* =========================
       GETTERS
       ========================= */

    public int getId() { return id; }
    public int getCompetenceId() { return competenceId; }

    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    public String getNiveau() { return niveau; }
    public int getAnneesExperience() { return anneesExperience; }
    public String getCertification() { return certification; }
    public String getStatut() { return statut; }

    public String getEmail() { return email; }
    public String getStatusChange() { return statusChange; }
    public LocalDateTime getDateModification() { return dateModification; }

    /* =========================
       SETTERS
       ========================= */

    public void setId(int id) { this.id = id; }
    public void setCompetenceId(int competenceId) { this.competenceId = competenceId; }

    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }

    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setAnneesExperience(int anneesExperience) { this.anneesExperience = anneesExperience; }
    public void setCertification(String certification) { this.certification = certification; }
    public void setStatut(String statut) { this.statut = statut; }

    public void setEmail(String email) { this.email = email; }
    public void setStatusChange(String statusChange) { this.statusChange = statusChange; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    /* =========================
       CHECKBOX PROPERTY
       ========================= */

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }
}
