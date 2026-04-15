<?php

namespace App\Entity\RendezVous;

use App\Entity\utilisateur\Utilisateur;
use App\Repository\RendezVousRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: RendezVousRepository::class)]
#[ORM\Table(name: 'rendez_vous')]
class RendezVous
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_rendez_vous', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_admin_createur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $adminCreateur = null;

    #[ORM\Column(name: 'mail_user', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'titre', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: Types::TEXT, nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'date_rendez_vous', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateRendezVous = null;

    #[ORM\Column(name: 'type', length: 50, nullable: true)]
    private ?string $type = null;

    #[ORM\Column(name: 'competence', length: 150, nullable: true)]
    private ?string $competence = null;

    #[ORM\Column(name: 'date_creation_rendez_vous', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateCreationRendezVous = null;

    #[ORM\Column(name: 'nombre_places', type: Types::INTEGER, options: ['default' => 0])]
    private int $nombrePlaces = 0;

    public function __construct()
    {
        $this->dateCreationRendezVous = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getAdminCreateur(): ?Utilisateur { return $this->adminCreateur; }
    public function getMailUser(): ?string { return $this->mailUser; }
    public function getTitre(): ?string { return $this->titre; }
    public function getDescription(): ?string { return $this->description; }
    public function getDateRendezVous(): ?\DateTimeInterface { return $this->dateRendezVous; }
    public function getType(): ?string { return $this->type; }
    public function getCompetence(): ?string { return $this->competence; }
    public function getDateCreationRendezVous(): ?\DateTimeInterface { return $this->dateCreationRendezVous; }
    public function getNombrePlaces(): int { return $this->nombrePlaces; }

    // ── Setters ──────────────────────────────────────────────
    public function setAdminCreateur(?Utilisateur $v): self { $this->adminCreateur = $v; return $this; }
    public function setMailUser(?string $v): self { $this->mailUser = $v; return $this; }
    public function setTitre(?string $v): self { $this->titre = $v; return $this; }
    public function setDescription(?string $v): self { $this->description = $v; return $this; }
    public function setDateRendezVous(?\DateTimeInterface $v): self { $this->dateRendezVous = $v; return $this; }
    public function setType(?string $v): self { $this->type = $v; return $this; }
    public function setCompetence(?string $v): self { $this->competence = $v; return $this; }
    public function setDateCreationRendezVous(?\DateTimeInterface $v): self { $this->dateCreationRendezVous = $v; return $this; }
    public function setNombrePlaces(int $v): self { $this->nombrePlaces = $v; return $this; }
}