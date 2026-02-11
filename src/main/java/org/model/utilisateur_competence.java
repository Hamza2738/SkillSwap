package org.model;

public class utilisateur_competence {

    private int idUtilisateur;
    private int idCompetence;

    public utilisateur_competence() {}

    public utilisateur_competence(int idUtilisateur, int idCompetence) {
        this.idUtilisateur = idUtilisateur;
        this.idCompetence = idCompetence;
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public int getIdCompetence() { return idCompetence; }

    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setIdCompetence(int idCompetence) { this.idCompetence = idCompetence; }

    @Override
    public String toString() {
        return "UtilisateurCompetence{" +
                "idUtilisateur=" + idUtilisateur +
                ", idCompetence=" + idCompetence +
                '}';
    }
}
