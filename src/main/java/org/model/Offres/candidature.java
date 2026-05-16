    package org.model.Offres;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    public class Candidature {

        private int           idCandidature;
        private LocalDateTime dateCandidature;
        private LocalDateTime dateAbonnement;
        private LocalDateTime dateExpiration;
        private String        matriculeOffre;
        private String        mailAcheteur;
        private BigDecimal    prixPaye;
        private String        modePaiement;     // carte | virement | paypal | gratuit
        private String        referencePaiement;
        private String        statut;           // en_attente | acceptee | refusee
        private String        message;
        private String        cv;
        private String        lettreMotivation;
        private String        noteRecruteur;
        private Integer       noteCandidat;     // 1-5
        private boolean       isActive;
        private int           idUtilisateur;
        private int           idOffre;

        // ── titres dénormalisés pour affichage ──
        private String        titreOffre;
        private String        nomCandidat;
        private String        prenomCandidat;

        public Candidature() {}

        // ── Getters / Setters ───────────────────────────────────────
        public int           getIdCandidature()                    { return idCandidature; }
        public void          setIdCandidature(int v)               { this.idCandidature = v; }

        public LocalDateTime getDateCandidature()                  { return dateCandidature; }
        public void          setDateCandidature(LocalDateTime v)   { this.dateCandidature = v; }

        public LocalDateTime getDateAbonnement()                   { return dateAbonnement; }
        public void          setDateAbonnement(LocalDateTime v)    { this.dateAbonnement = v; }

        public LocalDateTime getDateExpiration()                   { return dateExpiration; }
        public void          setDateExpiration(LocalDateTime v)    { this.dateExpiration = v; }

        public String        getMatriculeOffre()                   { return matriculeOffre; }
        public void          setMatriculeOffre(String v)           { this.matriculeOffre = v; }

        public String        getMailAcheteur()                     { return mailAcheteur; }
        public void          setMailAcheteur(String v)             { this.mailAcheteur = v; }

        public BigDecimal    getPrixPaye()                         { return prixPaye; }
        public void          setPrixPaye(BigDecimal v)             { this.prixPaye = v; }

        public String        getModePaiement()                     { return modePaiement; }
        public void          setModePaiement(String v)             { this.modePaiement = v; }

        public String        getReferencePaiement()                { return referencePaiement; }
        public void          setReferencePaiement(String v)        { this.referencePaiement = v; }

        public String        getStatut()                           { return statut; }
        public void          setStatut(String v)                   { this.statut = v; }

        public String        getMessage()                          { return message; }
        public void          setMessage(String v)                  { this.message = v; }

        public String        getCv()                               { return cv; }
        public void          setCv(String v)                       { this.cv = v; }

        public String        getLettreMotivation()                 { return lettreMotivation; }
        public void          setLettreMotivation(String v)         { this.lettreMotivation = v; }

        public String        getNoteRecruteur()                    { return noteRecruteur; }
        public void          setNoteRecruteur(String v)            { this.noteRecruteur = v; }

        public Integer       getNoteCandidat()                     { return noteCandidat; }
        public void          setNoteCandidat(Integer v)            { this.noteCandidat = v; }

        public boolean       isActive()                            { return isActive; }
        public void          setActive(boolean v)                  { this.isActive = v; }

        public int           getIdUtilisateur()                    { return idUtilisateur; }
        public void          setIdUtilisateur(int v)               { this.idUtilisateur = v; }

        public int           getIdOffre()                          { return idOffre; }
        public void          setIdOffre(int v)                     { this.idOffre = v; }

        public String        getTitreOffre()                       { return titreOffre; }
        public void          setTitreOffre(String v)               { this.titreOffre = v; }

        public String        getNomCandidat()                      { return nomCandidat; }
        public void          setNomCandidat(String v)              { this.nomCandidat = v; }

        public String        getPrenomCandidat()                   { return prenomCandidat; }
        public void          setPrenomCandidat(String v)           { this.prenomCandidat = v; }

        // ── Helpers ─────────────────────────────────────────────────
        public boolean isExpiree() {
            return dateExpiration != null && LocalDateTime.now().isAfter(dateExpiration);
        }

        public String getStatutLabel() {
            return switch (statut == null ? "" : statut) {
                case "acceptee"   -> "Acceptée ✅";
                case "refusee"    -> "Refusée ❌";
                case "en_attente" -> "En attente ⏳";
                default           -> statut;
            };
        }

        public String getAbonnementLabel() {
            if (!isActive)     return "Expiré 🔴";
            if (isExpiree())   return "Expiré 🔴";
            return "Actif 🟢";
        }
    }