<?php

namespace App\Entity\RendezVous;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'postulation')]
class Postulation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_postulation', type: 'integer')]
    private ?int $idPostulation = null;

    #[ORM\Column(name: 'id_rendez_vous', type: 'integer')]
    private ?int $idRendezVous = null;

    #[ORM\Column(name: 'id_postulant', type: 'integer')]
    private ?int $idPostulant = null;

    #[ORM\Column(name: 'mail_user', type: 'string', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'message', type: 'text', nullable: true)]
    private ?string $message = null;

    #[ORM\Column(name: 'cv', type: 'string', length: 255, nullable: true)]
    private ?string $cv = null;

    #[ORM\Column(name: 'status', type: 'string', length: 50)]
    private ?string $status = 'en_attente';

    #[ORM\Column(name: 'date_postulation', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $datePostulation = null;

    #[ORM\Column(name: 'role', type: 'string', length: 50, nullable: true)]
    private ?string $role = null;

    #[ORM\Column(name: 'nom', type: 'string', length: 100, nullable: true)]
    private ?string $nom = null;

    #[ORM\Column(name: 'prenom', type: 'string', length: 100, nullable: true)]
    private ?string $prenom = null;

    #[ORM\Column(name: 'titre', type: 'string', length: 200, nullable: true)]
    private ?string $titre = null;
    

    public function getIdPostulation(): ?int
    {
        return $this->idPostulation;
    }

    public function getIdRendezVous(): ?int
    {
        return $this->idRendezVous;
    }

    public function setIdRendezVous(?int $idRendezVous): self
    {
        $this->idRendezVous = $idRendezVous;
        return $this;
    }

    public function getIdPostulant(): ?int
    {
        return $this->idPostulant;
    }

    public function setIdPostulant(?int $idPostulant): self
    {
        $this->idPostulant = $idPostulant;
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

    public function getMessage(): ?string
    {
        return $this->message;
    }

    public function setMessage(?string $message): self
    {
        $this->message = $message;
        return $this;
    }

    public function getCv(): ?string
    {
        return $this->cv;
    }

    public function setCv(?string $cv): self
    {
        $this->cv = $cv;
        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(?string $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function getDatePostulation(): ?\DateTimeInterface
    {
        return $this->datePostulation;
    }

    public function setDatePostulation(?\DateTimeInterface $datePostulation): self
    {
        $this->datePostulation = $datePostulation;
        return $this;
    }

    public function getRole(): ?string
    {
        return $this->role;
    }

    public function setRole(?string $role): self
    {
        $this->role = $role;
        return $this;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(?string $nom): self
    {
        $this->nom = $nom;
        return $this;
    }

    public function getPrenom(): ?string
    {
        return $this->prenom;
    }

    public function setPrenom(?string $prenom): self
    {
        $this->prenom = $prenom;
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
}