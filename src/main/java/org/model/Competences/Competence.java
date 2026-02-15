package org.model.Competences;

public class Competence {

    private int id;
    private String category;
    private String type;
    private String description;


    private String niveau;
    private int anneesExperience;
    private String certification;
    private String statut;


    private String email;

    public Competence() {}


    public Competence(int id, String category, String type, String description,
                      String niveau, int anneesExperience, String certification, String statut,
                      String email) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.description = description;
        this.niveau = niveau;
        this.anneesExperience = anneesExperience;
        this.certification = certification;
        this.statut = statut;
        this.email = email;
    }


    public Competence(int id, String category, String type, String description) {
        this(id, category, type, description, "Débutant", 0, null, "En cours", null);
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    public String getNiveau() { return niveau; }
    public int getAnneesExperience() { return anneesExperience; }
    public String getCertification() { return certification; }
    public String getStatut() { return statut; }
    public String getEmail() { return email; }


    public void setId(int id) { this.id = id; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }

    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setAnneesExperience(int anneesExperience) { this.anneesExperience = anneesExperience; }
    public void setCertification(String certification) { this.certification = certification; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setEmail(String email) { this.email = email; }
}
