package org.model.RendezVous;

import java.time.LocalDateTime;

public class postulation {

    private int id_postulation;
    private int id_rendez_vous;     // FK vers rendez_vous.id_rendez_vous
    private int id_postulant;       // FK vers utilisateur.id_utilisateur
    private String message;
    private String status;          // 'en_attente' | 'acceptee' | 'refusee'
    private String competence_offerte;
    private LocalDateTime date_postulation;

    public postulation() {}

    public postulation(int id_postulation, int id_rendez_vous, int id_postulant, String message,
                       String status, String competence_offerte, LocalDateTime date_postulation) {
        this.id_postulation = id_postulation;
        this.id_rendez_vous = id_rendez_vous;
        this.id_postulant = id_postulant;
        this.message = message;
        this.status = status;
        this.competence_offerte = competence_offerte;
        this.date_postulation = date_postulation;
    }

    public int getId_postulation() { return id_postulation; }
    public void setId_postulation(int id_postulation) { this.id_postulation = id_postulation; }

    public int getId_rendez_vous() { return id_rendez_vous; }
    public void setId_rendez_vous(int id_rendez_vous) { this.id_rendez_vous = id_rendez_vous; }

    public int getId_postulant() { return id_postulant; }
    public void setId_postulant(int id_postulant) { this.id_postulant = id_postulant; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCompetence_offerte() { return competence_offerte; }
    public void setCompetence_offerte(String competence_offerte) { this.competence_offerte = competence_offerte; }

    public LocalDateTime getDate_postulation() { return date_postulation; }
    public void setDate_postulation(LocalDateTime date_postulation) { this.date_postulation = date_postulation; }

    @Override
    public String toString() {
        return "Postulation #" + id_postulation + " (" + status + ")";
    }
}
