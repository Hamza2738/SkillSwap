package org.model.Utilisateurs;

import java.time.LocalDateTime;

public class Message {

    // ── Champs de base ──
    private int           id;
    private int           idEnvoyeur;
    private int           idReceveur;
    private String        contenu;
    private LocalDateTime dateEnvoi;
    private boolean       lu;

    // ── Champs média ──
    private String  typeMessage;   // "texte" | "image" | "video" | "vocal" | "emoji"
    private String  fichierPath;   // chemin absolu fichier image/vidéo
    private String  fichierNom;    // nom original du fichier
    private long    fichierTaille; // taille en octets
    private float   dureeVocal;    // durée en secondes (messages vocaux)
    private byte[]  audioData;     // données binaires audio

    public Message() {
        this.typeMessage = "texte";
    }

    // ── Getters / Setters de base ──
    public int           getId()           { return id; }
    public int           getIdEnvoyeur()   { return idEnvoyeur; }
    public int           getIdReceveur()   { return idReceveur; }
    public String        getContenu()      { return contenu; }
    public LocalDateTime getDateEnvoi()    { return dateEnvoi; }
    public boolean       isLu()           { return lu; }

    public void setId(int id)                         { this.id = id; }
    public void setIdEnvoyeur(int idEnvoyeur)         { this.idEnvoyeur = idEnvoyeur; }
    public void setIdReceveur(int idReceveur)         { this.idReceveur = idReceveur; }
    public void setContenu(String contenu)            { this.contenu = contenu; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public void setLu(boolean lu)                     { this.lu = lu; }

    // ── Getters / Setters média ──
    public String  getTypeMessage()   { return typeMessage; }
    public String  getFichierPath()   { return fichierPath; }
    public String  getFichierNom()    { return fichierNom; }
    public long    getFichierTaille() { return fichierTaille; }
    public float   getDureeVocal()    { return dureeVocal; }
    public byte[]  getAudioData()     { return audioData; }

    public void setTypeMessage(String typeMessage)     { this.typeMessage = typeMessage; }
    public void setFichierPath(String fichierPath)     { this.fichierPath = fichierPath; }
    public void setFichierNom(String fichierNom)       { this.fichierNom = fichierNom; }
    public void setFichierTaille(long fichierTaille)   { this.fichierTaille = fichierTaille; }
    public void setDureeVocal(float dureeVocal)        { this.dureeVocal = dureeVocal; }
    public void setAudioData(byte[] audioData)         { this.audioData = audioData; }

    // ── Helpers ──
    public boolean isTexte()  { return "texte".equals(typeMessage)  || typeMessage == null; }
    public boolean isImage()  { return "image".equals(typeMessage); }
    public boolean isVideo()  { return "video".equals(typeMessage); }
    public boolean isVocal()  { return "vocal".equals(typeMessage); }
    public boolean isEmoji()  { return "emoji".equals(typeMessage); }
    public boolean isFichier() { return "fichier".equals(typeMessage); }
    public boolean hasFichier() {
        return fichierPath != null && !fichierPath.isBlank();
    }

    @Override
    public String toString() {
        return "Message{id=" + id +
                ", de=" + idEnvoyeur +
                ", à=" + idReceveur +
                ", type=" + typeMessage +
                ", contenu=" + contenu + "}";
    }
}