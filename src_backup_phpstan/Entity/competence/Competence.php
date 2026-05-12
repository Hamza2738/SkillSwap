<?php

namespace App\Entity\competence;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: "competences")]
class Competence
{
    // ============================================================
    // 🔑 ID (clé primaire)
    // ============================================================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id = null;

    // ============================================================
    // 📌 CATEGORY
    // ============================================================
    #[ORM\Column(type: "string", length: 100)]
    private ?string $category = null;

    // ============================================================
    // 📌 TYPE
    // ============================================================
    #[ORM\Column(type: "string", length: 100)]
    private ?string $type = null;

    // ============================================================
    // 📌 DESCRIPTION
    // ============================================================
    #[ORM\Column(type: "text", nullable: true)]
    private ?string $description = null;

    // ============================================================
    // 📌 NIVEAU
    // ============================================================
    #[ORM\Column(type: "string", length: 50)]
    private ?string $niveau = null;

    // ============================================================
    // 📌 ANNÉES D’EXPÉRIENCE
    // ============================================================
    #[ORM\Column(name: "annees_experience", type: "integer")]
    private ?int $anneesExperience = null;

    // ============================================================
    // 📌 CERTIFICATION
    // ============================================================
    #[ORM\Column(type: "string", length: 255, nullable: true)]
    private ?string $certification = null;

    // ============================================================
    // 📌 STATUT
    // ============================================================
    #[ORM\Column(type: "string", length: 50)]
    private ?string $statut = null;

    // ============================================================
    // 📌 EMAIL
    // ============================================================
    #[ORM\Column(type: "string", length: 150)]
    private ?string $email = null;

    // ============================================================
    // 🧠 GETTERS & SETTERS
    // ============================================================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getCategory(): ?string
    {
        return $this->category;
    }

    public function setCategory(string $category): self
    {
        $this->category = $category;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): self
    {
        $this->type = $type;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getNiveau(): ?string
    {
        return $this->niveau;
    }

    public function setNiveau(string $niveau): self
    {
        $this->niveau = $niveau;
        return $this;
    }

    public function getAnneesExperience(): ?int
    {
        return $this->anneesExperience;
    }

    public function setAnneesExperience(int $anneesExperience): self
    {
        $this->anneesExperience = $anneesExperience;
        return $this;
    }

    public function getCertification(): ?string
    {
        return $this->certification;
    }

    public function setCertification(?string $certification): self
    {
        $this->certification = $certification;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): self
    {
        $this->email = $email;
        return $this;
    }
}