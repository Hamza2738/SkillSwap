<?php

namespace App\Entity\RendezVous;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'rendez_vous')]
class RendezVous
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_rendez_vous', type: 'integer')]
    private ?int $idRendezVous = null;

    #[ORM\Column(name: 'id_admin_createur', type: 'integer')]
    private ?int $idAdminCreateur = null;

    #[ORM\Column(name: 'mail_user', type: 'string', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'titre', type: 'string', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'date_rendez_vous', type: 'datetime')]
    private ?\DateTimeInterface $dateRendezVous = null;

    #[ORM\Column(name: 'type', type: 'string', length: 50)]
    private ?string $type = null;

    #[ORM\Column(name: 'competence', type: 'string', length: 150)]
    private ?string $competence = null;

    #[ORM\Column(name: 'date_creation_rendez_vous', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCreationRendezVous = null;

    #[ORM\Column(name: 'nombre_places', type: 'integer')]
    private ?int $nombrePlaces = null;
    

    public function getIdRendezVous(): ?int
    {
        return $this->idRendezVous;
    }

    public function getIdAdminCreateur(): ?int
    {
        return $this->idAdminCreateur;
    }

    public function setIdAdminCreateur(?int $idAdminCreateur): self
    {
        $this->idAdminCreateur = $idAdminCreateur;
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

    public function getDateRendezVous(): ?\DateTimeInterface
    {
        return $this->dateRendezVous;
    }

    public function setDateRendezVous(?\DateTimeInterface $dateRendezVous): self
    {
        $this->dateRendezVous = $dateRendezVous;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(?string $type): self
    {
        $this->type = $type;
        return $this;
    }

    public function getCompetence(): ?string
    {
        return $this->competence;
    }

    public function setCompetence(?string $competence): self
    {
        $this->competence = $competence;
        return $this;
    }

    public function getDateCreationRendezVous(): ?\DateTimeInterface
    {
        return $this->dateCreationRendezVous;
    }

    public function setDateCreationRendezVous(?\DateTimeInterface $dateCreationRendezVous): self
    {
        $this->dateCreationRendezVous = $dateCreationRendezVous;
        return $this;
    }

    public function getNombrePlaces(): ?int
    {
        return $this->nombrePlaces;
    }

    public function setNombrePlaces(?int $nombrePlaces): self
    {
        $this->nombrePlaces = $nombrePlaces;
        return $this;
    }
}