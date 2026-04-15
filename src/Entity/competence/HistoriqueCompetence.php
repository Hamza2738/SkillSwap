<?php

namespace App\Entity\competence;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: "historiques_competences")]
class HistoriqueCompetence
{
    // ============================================================
    // 🔑 ID
    // ============================================================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id = null;

    // ============================================================
    // 🔗 COMPETENCE ID (référence)
    // ============================================================
    #[ORM\Column(name: "competence_id", type: "integer", nullable: true)]
    private ?int $competenceId = null;

    // ============================================================
    // 📌 CHAMPS MÉTIER (snapshot au moment de l'action)
    // ============================================================
    #[ORM\Column(type: "string", length: 100, nullable: true)]
    private ?string $category = null;

    #[ORM\Column(type: "string", length: 100, nullable: true)]
    private ?string $type = null;

    #[ORM\Column(type: "text", nullable: true)]
    private ?string $description = null;

    #[ORM\Column(type: "string", length: 50, nullable: true)]
    private ?string $niveau = null;

    #[ORM\Column(name: "annees_experience", type: "integer", nullable: true)]
    private ?int $anneesExperience = null;

    #[ORM\Column(type: "string", length: 255, nullable: true)]
    private ?string $certification = null;

    #[ORM\Column(type: "string", length: 50, nullable: true)]
    private ?string $statut = null;

    #[ORM\Column(type: "string", length: 150, nullable: true)]
    private ?string $email = null;

    // ============================================================
    // 📌 ACTION EFFECTUÉE (Ajoutée / Modifiée / Supprimée)
    // ============================================================
    #[ORM\Column(name: "status_change", type: "string", length: 50, nullable: true)]
    private ?string $statusChange = null;

    // ============================================================
    // 📌 DATE DE MODIFICATION
    // ============================================================
    #[ORM\Column(name: "date_modification", type: "datetime", nullable: true)]
    private ?\DateTimeInterface $dateModification = null;

    // ============================================================
    // 🏗️ CONSTRUCTEUR
    // ============================================================
    public function __construct()
    {
        $this->dateModification = new \DateTime();
    }

    // ============================================================
    // 🧠 GETTERS & SETTERS
    // ============================================================

    public function getId(): ?int { return $this->id; }

    public function getCompetenceId(): ?int { return $this->competenceId; }
    public function setCompetenceId(?int $competenceId): self
    {
        $this->competenceId = $competenceId;
        return $this;
    }

    public function getCategory(): ?string { return $this->category; }
    public function setCategory(?string $category): self
    {
        $this->category = $category;
        return $this;
    }

    public function getType(): ?string { return $this->type; }
    public function setType(?string $type): self
    {
        $this->type = $type;
        return $this;
    }

    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getNiveau(): ?string { return $this->niveau; }
    public function setNiveau(?string $niveau): self
    {
        $this->niveau = $niveau;
        return $this;
    }

    public function getAnneesExperience(): ?int { return $this->anneesExperience; }
    public function setAnneesExperience(?int $anneesExperience): self
    {
        $this->anneesExperience = $anneesExperience;
        return $this;
    }

    public function getCertification(): ?string { return $this->certification; }
    public function setCertification(?string $certification): self
    {
        $this->certification = $certification;
        return $this;
    }

    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(?string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getEmail(): ?string { return $this->email; }
    public function setEmail(?string $email): self
    {
        $this->email = $email;
        return $this;
    }

    public function getStatusChange(): ?string { return $this->statusChange; }
    public function setStatusChange(?string $statusChange): self
    {
        $this->statusChange = $statusChange;
        return $this;
    }

    public function getDateModification(): ?\DateTimeInterface { return $this->dateModification; }
    public function setDateModification(?\DateTimeInterface $dateModification): self
    {
        $this->dateModification = $dateModification;
        return $this;
    }
}