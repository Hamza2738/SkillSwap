package org.model.Offres;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Offre {

    private int          idOffre;
    private String       titre;
    private String       description;
    private String       typeOffre;        // emploi | projet | tache
    private BigDecimal   budget;
    private BigDecimal   prix;
    private int          duree;            // jours
    private String       matricule;        // OFF-2024-00042

    private String       localisation;

    private LocalDate    datePublication;
    private LocalDate    dateLimite;
    private String       statut;           // ouverte | fermee
    private int          idUtilisateur;
    private String       mailUser;

    private int          vues;
    private String       tags;

    // ── Nouveaux champs likes & commentaires ───────────────────
    private int          nbLikes;
    private int          nbCommentaires;
    private boolean      likedByCurrentUser; // transient, non persisté

    // ── Constructeurs ──────────────────────────────────────────
    public Offre() {}

    public Offre(String titre, String description, String typeOffre,
                 BigDecimal budget, BigDecimal prix, int duree,
                 int nombrePlaces, String competencesReq,
                 String localisation, String modeTravail, String niveauRequis,
                 LocalDate datePublication, LocalDate dateLimite,
                 String statut, int idUtilisateur, String mailUser, String tags) {
        this.titre           = titre;
        this.description     = description;
        this.typeOffre       = typeOffre;
        this.budget          = budget;
        this.prix            = prix;
        this.duree           = duree;
        this.localisation    = localisation;
        this.datePublication = datePublication;
        this.dateLimite      = dateLimite;
        this.statut          = statut;
        this.idUtilisateur   = idUtilisateur;
        this.mailUser        = mailUser;
        this.tags            = tags;
    }

    // ── Getters / Setters ──────────────────────────────────────
    public int          getIdOffre()           { return idOffre; }
    public void         setIdOffre(int v)      { this.idOffre = v; }

    public String       getTitre()             { return titre; }
    public void         setTitre(String v)     { this.titre = v; }

    public String       getDescription()       { return description; }
    public void         setDescription(String v){ this.description = v; }

    public String       getTypeOffre()         { return typeOffre; }
    public void         setTypeOffre(String v) { this.typeOffre = v; }

    public BigDecimal   getBudget()            { return budget; }
    public void         setBudget(BigDecimal v){ this.budget = v; }

    public BigDecimal   getPrix()              { return prix; }
    public void         setPrix(BigDecimal v)  { this.prix = v; }

    public int          getDuree()             { return duree; }
    public void         setDuree(int v)        { this.duree = v; }

    public String       getMatricule()         { return matricule; }
    public void         setMatricule(String v) { this.matricule = v; }

    public String       getLocalisation()             { return localisation; }
    public void         setLocalisation(String v)     { this.localisation = v; }

    public LocalDate    getDatePublication()          { return datePublication; }
    public void         setDatePublication(LocalDate v){ this.datePublication = v; }

    public LocalDate    getDateLimite()               { return dateLimite; }
    public void         setDateLimite(LocalDate v)    { this.dateLimite = v; }

    public String       getStatut()                   { return statut; }
    public void         setStatut(String v)           { this.statut = v; }

    public int          getIdUtilisateur()            { return idUtilisateur; }
    public void         setIdUtilisateur(int v)       { this.idUtilisateur = v; }

    public String       getMailUser()                 { return mailUser; }
    public void         setMailUser(String v)         { this.mailUser = v; }

    public int          getVues()                     { return vues; }
    public void         setVues(int v)                { this.vues = v; }

    public String       getTags()                     { return tags; }
    public void         setTags(String v)             { this.tags = v; }

    // ── Nouveaux getters/setters likes & commentaires ──────────
    public int          getNbLikes()                  { return nbLikes; }
    public void         setNbLikes(int v)             { this.nbLikes = v; }

    public int          getNbCommentaires()           { return nbCommentaires; }
    public void         setNbCommentaires(int v)      { this.nbCommentaires = v; }

    public boolean      isLikedByCurrentUser()        { return likedByCurrentUser; }
    public void         setLikedByCurrentUser(boolean v){ this.likedByCurrentUser = v; }

    // ── Helpers ────────────────────────────────────────────────
    public boolean isOuverte() { return "ouverte".equalsIgnoreCase(statut); }

    public boolean isExpiree() {
        return dateLimite != null && LocalDate.now().isAfter(dateLimite);
    }

    public String getTypeIcon() {
        return switch (typeOffre == null ? "" : typeOffre) {
            case "emploi"  -> "💼";
            case "projet"  -> "🚀";
            case "tache"   -> "📋";
            default        -> "📄";
        };
    }

    @Override
    public String toString() {
        return "[" + matricule + "] " + titre + " (" + typeOffre + ") — " + prix + " TND";
    }
}