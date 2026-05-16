package org.model.Offres;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Commentaire {

    private int           idCommentaire;
    private int           idOffre;
    private int           idUtilisateur;
    private String        contenu;
    private LocalDateTime dateCommentaire;

    // Dénormalisé pour affichage
    private String        nomAuteur;
    private String        prenomAuteur;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Commentaire() {}

    // ── Getters / Setters ──────────────────────────────────────
    public int           getIdCommentaire()              { return idCommentaire; }
    public void          setIdCommentaire(int v)         { this.idCommentaire = v; }

    public int           getIdOffre()                    { return idOffre; }
    public void          setIdOffre(int v)               { this.idOffre = v; }

    public int           getIdUtilisateur()              { return idUtilisateur; }
    public void          setIdUtilisateur(int v)         { this.idUtilisateur = v; }

    public String        getContenu()                    { return contenu; }
    public void          setContenu(String v)            { this.contenu = v; }

    public LocalDateTime getDateCommentaire()            { return dateCommentaire; }
    public void          setDateCommentaire(LocalDateTime v){ this.dateCommentaire = v; }

    public String        getNomAuteur()                  { return nomAuteur; }
    public void          setNomAuteur(String v)          { this.nomAuteur = v; }

    public String        getPrenomAuteur()               { return prenomAuteur; }
    public void          setPrenomAuteur(String v)       { this.prenomAuteur = v; }

    // ── Helper ─────────────────────────────────────────────────
    public String getAuteurLabel() {
        String n = nomAuteur    != null ? nomAuteur    : "";
        String p = prenomAuteur != null ? prenomAuteur : "";
        return (n + " " + p).trim();
    }

    public String getDateLabel() {
        return dateCommentaire != null ? dateCommentaire.format(FMT) : "—";
    }
}