package org.model;

public class utilisateur {

    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String photoProfil;
    private String role;
    private String statut;

    public utilisateur() {}

    public utilisateur(int idUtilisateur, String nom, String prenom, String email,
                       String motDePasse, String telephone, String photoProfil,
                       String role, String statut) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.photoProfil = photoProfil;
        this.role = role;
        this.statut = statut;
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }
    public String getTelephone() { return telephone; }
    public String getPhotoProfil() { return photoProfil; }
    public String getRole() { return role; }
    public String getStatut() { return statut; }

    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }
    public void setRole(String role) { this.role = role; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
