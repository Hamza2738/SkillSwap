<?php

namespace App\Entity\suiviTache;

use App\Repository\TacheRepository;
use Doctrine\ORM\Mapping as ORM;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: TacheRepository::class)]
#[ORM\Table(name: 'tache')]
class Tache
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\Column(name: 'titre', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: Types::TEXT, nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'statut', length: 50, nullable: true)]
    private ?string $statut = null;

    #[ORM\Column(name: 'priorite', length: 50, nullable: true)]
    private ?string $priorite = null;

    #[ORM\Column(name: 'echeance', type: Types::DATE_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $echeance = null;

    #[ORM\ManyToOne(targetEntity: Projet::class)]
    #[ORM\JoinColumn(name: 'projet_id', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    private ?Projet $projet = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $user = null;

    #[ORM\Column(name: 'mail_user', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'evaluation', length: 255, nullable: true)]
    private ?string $evaluation = null;

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getTitre(): ?string { return $this->titre; }
    public function getDescription(): ?string { return $this->description; }
    public function getStatut(): ?string { return $this->statut; }
    public function getPriorite(): ?string { return $this->priorite; }
    public function getEcheance(): ?\DateTimeInterface { return $this->echeance; }
    public function getProjet(): ?Projet { return $this->projet; }
    public function getUser(): ?Utilisateur { return $this->user; }
    public function getMailUser(): ?string { return $this->mailUser; }
    public function getEvaluation(): ?string { return $this->evaluation; }

    // ── Setters ──────────────────────────────────────────────
    public function setTitre(?string $v): self { $this->titre = $v; return $this; }
    public function setDescription(?string $v): self { $this->description = $v; return $this; }
    public function setStatut(?string $v): self { $this->statut = $v; return $this; }
    public function setPriorite(?string $v): self { $this->priorite = $v; return $this; }
    public function setEcheance(?\DateTimeInterface $v): self { $this->echeance = $v; return $this; }
    public function setProjet(?Projet $v): self { $this->projet = $v; return $this; }
    public function setUser(?Utilisateur $v): self { $this->user = $v; return $this; }
    public function setMailUser(?string $v): self { $this->mailUser = $v; return $this; }
    public function setEvaluation(?string $v): self { $this->evaluation = $v; return $this; }
}