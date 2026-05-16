    package org.model.Utilisateurs;

    import java.time.LocalDateTime;

    public class utilisateur {

        private int id_utilisateur;
        private String nom;
        private String prenom;
        private String email;
        private String mot_de_passe;
        private String telephone;
        private String photo_profil;
        private String cle_acces;
        // ✅ nouveaux attributs profil
        private String bio;
        private String photo_couverture;
        private String lieu;

        private String role;
        private String statut;
        private LocalDateTime date_inscription;

        public utilisateur() {}

        // ===== GETTERS =====
        public String getCle_acces() {
            return cle_acces;
        }
        public int getId_utilisateur() { return id_utilisateur; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getMot_de_passe() { return mot_de_passe; }
        public String getTelephone() { return telephone; }
        public String getPhoto_profil() { return photo_profil; }

        // ✅ getters nouveaux attributs
        public String getBio() { return bio; }
        public String getPhoto_couverture() { return photo_couverture; }
        public String getLieu() { return lieu; }

        public String getRole() { return role; }
        public String getStatut() { return statut; }
        public LocalDateTime getDate_inscription() { return date_inscription; }

        // ===== SETTERS =====
        public void setCle_acces(String cle_acces) {
            this.cle_acces = cle_acces;
        }
        public void setId_utilisateur(int id_utilisateur) { this.id_utilisateur = id_utilisateur; }
        public void setNom(String nom) { this.nom = nom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public void setEmail(String email) { this.email = email; }
        public void setMot_de_passe(String mot_de_passe) { this.mot_de_passe = mot_de_passe; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
        public void setPhoto_profil(String photo_profil) { this.photo_profil = photo_profil; }

        // ✅ setters nouveaux attributs
        public void setBio(String bio) { this.bio = bio; }
        public void setPhoto_couverture(String photo_couverture) { this.photo_couverture = photo_couverture; }
        public void setLieu(String lieu) { this.lieu = lieu; }

        public void setRole(String role) { this.role = role; }
        public void setStatut(String statut) { this.statut = statut; }
        public void setDate_inscription(LocalDateTime date_inscription) { this.date_inscription = date_inscription; }
    }