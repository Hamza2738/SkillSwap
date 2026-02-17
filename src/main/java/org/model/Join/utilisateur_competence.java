package org.model.Join;

public class utilisateur_competence {

    private int id_utilisateur;
    private int id_competence;

    public utilisateur_competence() {}

    public utilisateur_competence(int id_utilisateur, int id_competence) {
        this.id_utilisateur = id_utilisateur;
        this.id_competence = id_competence;
    }

    public int getId_utilisateur() { return id_utilisateur; }
    public void setId_utilisateur(int id_utilisateur) { this.id_utilisateur = id_utilisateur; }

    public int getId_competence() { return id_competence; }
    public void setId_competence(int id_competence) { this.id_competence = id_competence; }
}
