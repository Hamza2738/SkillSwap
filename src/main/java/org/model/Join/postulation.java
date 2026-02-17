package org.model.Join;
import java.time.LocalDateTime;

public class postulation {

    private int idRendezVous;
    private String titre;
    private String adminCreateur;

    private int idPostulation;
    private String postulant;
    private String status;
    private LocalDateTime datePostulation;

    public postulation  () {}

    public postulation(int idRendezVous, String titre, String adminCreateur,
                           int idPostulation, String postulant, String status, LocalDateTime datePostulation) {
        this.idRendezVous = idRendezVous;
        this.titre = titre;
        this.adminCreateur = adminCreateur;
        this.idPostulation = idPostulation;
        this.postulant = postulant;
        this.status = status;
        this.datePostulation = datePostulation;
    }

    public int getIdRendezVous() { return idRendezVous; }
    public String getTitre() { return titre; }
    public String getAdminCreateur() { return adminCreateur; }
    public int getIdPostulation() { return idPostulation; }
    public String getPostulant() { return postulant; }
    public String getStatus() { return status; }
    public LocalDateTime getDatePostulation() { return datePostulation; }
}
