<?php

namespace App\Entity\utilisateur;

use App\Repository\ConnexionRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: ConnexionRepository::class)]
#[ORM\Table(name: 'connexion')]
#[ORM\UniqueConstraint(name: 'uq_connexion', columns: ['id_demandeur', 'id_receveur'])]
class Connexion
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_demandeur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $demandeur = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_receveur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $receveur = null;

    #[ORM\Column(name: 'statut', length: 50, options: ['default' => 'en_attente'])]
    private ?string $statut = 'en_attente';

    #[ORM\Column(name: 'date_demande', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateDemande = null;

    public function __construct()
    {
        $this->dateDemande = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getDemandeur(): ?Utilisateur { return $this->demandeur; }
    public function getReceveur(): ?Utilisateur { return $this->receveur; }
    public function getStatut(): ?string { return $this->statut; }
    public function getDateDemande(): ?\DateTimeInterface { return $this->dateDemande; }
    public function getIdDemandeur(): ?int { return $this->demandeur?->getIdUtilisateur(); }
    public function getIdReceveur(): ?int { return $this->receveur?->getIdUtilisateur(); }

    // ── Setters ──────────────────────────────────────────────
    public function setDemandeur(?Utilisateur $v): self { $this->demandeur = $v; return $this; }
    public function setReceveur(?Utilisateur $v): self { $this->receveur = $v; return $this; }
    public function setStatut(?string $v): self { $this->statut = $v; return $this; }
    public function setDateDemande(?\DateTimeInterface $v): self { $this->dateDemande = $v; return $this; }
}