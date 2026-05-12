<?php

namespace App\Entity\suiviTache;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'tache')]
class Tache
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'titre', type: 'string', length: 200)]
    private ?string $titre = null;

    #[ORM\Column(name: 'description', type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'statut', type: 'string', length: 50)]
    private ?string $statut = null;

    #[ORM\Column(name: 'priorite', type: 'string', length: 50)]
    private ?string $priorite = null;

    #[ORM\Column(name: 'echeance', type: 'date', nullable: true)]
    private ?\DateTimeInterface $echeance = null;

    #[ORM\Column(name: 'projet_id', type: 'integer')]
    private ?int $projetId = null;

    #[ORM\Column(name: 'user_id', type: 'integer')]
    private ?int $userId = null;

    #[ORM\Column(name: 'mail_user', type: 'string', length: 150, nullable: true)]
    private ?string $mailUser = null;

    #[ORM\Column(name: 'evaluation', type: 'string', length: 255, nullable: true)]
    private ?string $evaluation = null;

    public function getId(): ?int
    {
        return $this->id;
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

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(?string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getPriorite(): ?string
    {
        return $this->priorite;
    }

    public function setPriorite(?string $priorite): self
    {
        $this->priorite = $priorite;
        return $this;
    }

    public function getEcheance(): ?\DateTimeInterface
    {
        return $this->echeance;
    }

    public function setEcheance(?\DateTimeInterface $echeance): self
    {
        $this->echeance = $echeance;
        return $this;
    }

    public function getProjetId(): ?int
    {
        return $this->projetId;
    }

    public function setProjetId(?int $projetId): self
    {
        $this->projetId = $projetId;
        return $this;
    }

    public function getUserId(): ?int
    {
        return $this->userId;
    }

    public function setUserId(?int $userId): self
    {
        $this->userId = $userId;
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

    public function getEvaluation(): ?string
    {
        return $this->evaluation;
    }

    public function setEvaluation(?string $evaluation): self
    {
        $this->evaluation = $evaluation;
        return $this;
    }
}