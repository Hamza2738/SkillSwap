<?php

namespace App\Entity\suiviTache;

use App\Repository\ProjetRepository;
use Doctrine\ORM\Mapping as ORM;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: ProjetRepository::class)]
#[ORM\Table(name: 'projet')]
class Projet
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\Column(name: 'titre', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: Types::TEXT, nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'date_debut', type: Types::DATE_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateDebut = null;

    #[ORM\Column(name: 'date_fin', type: Types::DATE_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateFin = null;

    #[ORM\Column(name: 'statut', length: 50, nullable: true)]
    private ?string $statut = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $user = null;

    #[ORM\Column(name: 'mail_user', length: 150, nullable: true)]
    private ?string $mailUser = null;

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getTitre(): ?string { return $this->titre; }
    public function getDescription(): ?string { return $this->description; }
    public function getDateDebut(): ?\DateTimeInterface { return $this->dateDebut; }
    public function getDateFin(): ?\DateTimeInterface { return $this->dateFin; }
    public function getStatut(): ?string { return $this->statut; }
    public function getUser(): ?Utilisateur { return $this->user; }
    public function getMailUser(): ?string { return $this->mailUser; }

    // ── Setters ──────────────────────────────────────────────
    public function setTitre(?string $v): self { $this->titre = $v; return $this; }
    public function setDescription(?string $v): self { $this->description = $v; return $this; }
    public function setDateDebut(?\DateTimeInterface $v): self { $this->dateDebut = $v; return $this; }
    public function setDateFin(?\DateTimeInterface $v): self { $this->dateFin = $v; return $this; }
    public function setStatut(?string $v): self { $this->statut = $v; return $this; }
    public function setUser(?Utilisateur $v): self { $this->user = $v; return $this; }
    public function setMailUser(?string $v): self { $this->mailUser = $v; return $this; }
}