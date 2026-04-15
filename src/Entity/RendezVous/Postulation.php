<?php

namespace App\Entity\RendezVous;
use App\Entity\utilisateur\Utilisateur;

use App\Repository\PostulationRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: PostulationRepository::class)]
#[ORM\Table(name: 'postulation_rendez_vous')]
class Postulation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_postulation', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: RendezVous::class)]
    #[ORM\JoinColumn(name: 'id_rendez_vous', referencedColumnName: 'id_rendez_vous', nullable: false, onDelete: 'CASCADE')]
    private ?RendezVous $rendezVous = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_postulant', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $postulant = null;

    #[ORM\Column(name: 'mail_user', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'message', type: Types::TEXT, nullable: true)]
    private ?string $message = null;

    #[ORM\Column(name: 'cv', length: 255, nullable: true)]
    private ?string $cv = null;

    #[ORM\Column(name: 'status', length: 50, options: ['default' => 'en_attente'])]
    private ?string $status = 'en_attente';

    #[ORM\Column(name: 'date_postulation', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $datePostulation = null;

    #[ORM\Column(name: 'role', length: 50, nullable: true)]
    private ?string $role = null;

    #[ORM\Column(name: 'nom', length: 100, nullable: true)]
    private ?string $nom = null;

    #[ORM\Column(name: 'prenom', length: 100, nullable: true)]
    private ?string $prenom = null;

    #[ORM\Column(name: 'titre', length: 200, nullable: true)]
    private ?string $titre = null;

    public function __construct()
    {
        $this->datePostulation = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getRendezVous(): ?RendezVous { return $this->rendezVous; }
    public function getPostulant(): ?Utilisateur { return $this->postulant; }
    public function getMailUser(): ?string { return $this->mailUser; }
    public function getMessage(): ?string { return $this->message; }
    public function getCv(): ?string { return $this->cv; }
    public function getStatus(): ?string { return $this->status; }
    public function getDatePostulation(): ?\DateTimeInterface { return $this->datePostulation; }
    public function getRole(): ?string { return $this->role; }
    public function getNom(): ?string { return $this->nom; }
    public function getPrenom(): ?string { return $this->prenom; }
    public function getTitre(): ?string { return $this->titre; }

    // ── Setters ──────────────────────────────────────────────
    public function setRendezVous(?RendezVous $v): self { $this->rendezVous = $v; return $this; }
    public function setPostulant(?Utilisateur $v): self { $this->postulant = $v; return $this; }
    public function setMailUser(?string $v): self { $this->mailUser = $v; return $this; }
    public function setMessage(?string $v): self { $this->message = $v; return $this; }
    public function setCv(?string $v): self { $this->cv = $v; return $this; }
    public function setStatus(?string $v): self { $this->status = $v; return $this; }
    public function setDatePostulation(?\DateTimeInterface $v): self { $this->datePostulation = $v; return $this; }
    public function setRole(?string $v): self { $this->role = $v; return $this; }
    public function setNom(?string $v): self { $this->nom = $v; return $this; }
    public function setPrenom(?string $v): self { $this->prenom = $v; return $this; }
    public function setTitre(?string $v): self { $this->titre = $v; return $this; }
}