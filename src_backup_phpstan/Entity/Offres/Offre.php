<?php

namespace App\Entity\Offres;

use App\Repository\OffreRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: OffreRepository::class)]
#[ORM\Table(name: 'offre')]
class Offre
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_offre', type: 'integer')]
    private ?int $idOffre = null;

    #[ORM\Column(name: 'titre', type: 'string', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'type_offre', type: 'string', length: 50, nullable: true)]
    private ?string $typeOffre = null;

    #[ORM\Column(name: 'budget', type: 'decimal', precision: 12, scale: 2, nullable: true)]
    private ?string $budget = null;

    #[ORM\Column(name: 'prix', type: 'decimal', precision: 12, scale: 2, nullable: true)]
    private ?string $prix = null;

    #[ORM\Column(name: 'duree', type: 'integer', nullable: true)]
    private ?int $duree = null;

    #[ORM\Column(name: 'matricule', type: 'string', length: 50, nullable: true)]
    private ?string $matricule = null;

    #[ORM\Column(name: 'localisation', type: 'string', length: 150, nullable: true)]
    private ?string $localisation = null;

    #[ORM\Column(name: 'date_publication', type: 'date', nullable: true)]
    private ?\DateTimeInterface $datePublication = null;

    #[ORM\Column(name: 'date_limite', type: 'date', nullable: true)]
    private ?\DateTimeInterface $dateLimite = null;

    #[ORM\Column(name: 'statut', type: 'string', length: 50, nullable: true)]
    private ?string $statut = 'ouverte';

    #[ORM\Column(name: 'id_utilisateur', type: 'integer', nullable: true)]
    private ?int $idUtilisateur = null;

    #[ORM\Column(name: 'mail_user', type: 'string', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'vues', type: 'integer', nullable: true)]
    private ?int $vues = 0;

    #[ORM\Column(name: 'tags', type: 'string', length: 500, nullable: true)]
    private ?string $tags = null;

    public function getIdOffre(): ?int
    {
        return $this->idOffre;
    }

    public function getId(): ?int
    {
        return $this->idOffre;
    }

    public function getTitre(): ?string
    {
        return $this->titre;
    }

    public function setTitre(?string $titre): self
    {
        $this->titre = $titre;
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

    public function getTypeOffre(): ?string
    {
        return $this->typeOffre;
    }

    public function setTypeOffre(?string $typeOffre): self
    {
        $this->typeOffre = $typeOffre;
        return $this;
    }

    public function getBudget(): ?string
    {
        return $this->budget;
    }

    public function setBudget($budget): self
    {
        $this->budget = $budget !== null ? (string) $budget : null;
        return $this;
    }

    public function getPrix(): ?string
    {
        return $this->prix;
    }

    public function setPrix($prix): self
    {
        $this->prix = $prix !== null ? (string) $prix : null;
        return $this;
    }

    public function getDuree(): ?int
    {
        return $this->duree;
    }

    public function setDuree(?int $duree): self
    {
        $this->duree = $duree;
        return $this;
    }

    public function getMatricule(): ?string
    {
        return $this->matricule;
    }

    public function setMatricule(?string $matricule): self
    {
        $this->matricule = $matricule;
        return $this;
    }

    public function getLocalisation(): ?string
    {
        return $this->localisation;
    }

    public function setLocalisation(?string $localisation): self
    {
        $this->localisation = $localisation;
        return $this;
    }

    public function getDatePublication(): ?\DateTimeInterface
    {
        return $this->datePublication;
    }

    public function setDatePublication(?\DateTimeInterface $datePublication): self
    {
        $this->datePublication = $datePublication;
        return $this;
    }

    public function getDateLimite(): ?\DateTimeInterface
    {
        return $this->dateLimite;
    }

    public function setDateLimite(?\DateTimeInterface $dateLimite): self
    {
        $this->dateLimite = $dateLimite;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(?string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getIdUtilisateur(): ?int
    {
        return $this->idUtilisateur;
    }

    public function setIdUtilisateur(?int $idUtilisateur): self
    {
        $this->idUtilisateur = $idUtilisateur;
        return $this;
    }

    public function getMailUser(): ?string
    {
        return $this->mailUser;
    }

    public function setMailUser(?string $mailUser): self
    {
        $this->mailUser = $mailUser;
        return $this;
    }

    public function getVues(): ?int
    {
        return $this->vues;
    }

    public function setVues(?int $vues): self
    {
        $this->vues = $vues;
        return $this;
    }

    public function getTags(): ?string
    {
        return $this->tags;
    }

    public function setTags(?string $tags): self
    {
        $this->tags = $tags;
        return $this;
    }

    public function getTypeIcon(): string
    {
        return match ($this->typeOffre) {
            'emploi' => '💼',
            'projet' => '🚀',
            'tache' => '✅',
            default => '📌',
        };
    }

    public function getNbLikes(): int
    {
        return 0;
    }

    public function getNbCommentaires(): int
    {
        return 0;
    }
}