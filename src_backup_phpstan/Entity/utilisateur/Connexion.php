<?php

namespace App\Entity\utilisateur;

use App\Repository\ConnexionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ConnexionRepository::class)]
#[ORM\Table(name: 'connexion')]
class Connexion
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_demandeur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $demandeur = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_receveur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $receveur = null;

    #[ORM\Column(name: 'statut', length: 30)]
    private string $statut = 'en_attente';

  #[ORM\Column(name: 'date_demande', type: 'datetime_immutable')]
private ?\DateTimeImmutable $dateDemande = null;
    public function __construct()
    {
        $this->dateDemande = new \DateTimeImmutable();
        $this->statut = 'en_attente';
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDemandeur(): ?Utilisateur
    {
        return $this->demandeur;
    }

    public function setDemandeur(?Utilisateur $demandeur): self
    {
        $this->demandeur = $demandeur;
        return $this;
    }

    public function getReceveur(): ?Utilisateur
    {
        return $this->receveur;
    }

    public function setReceveur(?Utilisateur $receveur): self
    {
        $this->receveur = $receveur;
        return $this;
    }

    public function getStatut(): string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getDateDemande(): ?\DateTimeImmutable
    {
        return $this->dateDemande;
    }

    public function setDateDemande(?\DateTimeImmutable $dateDemande): self
    {
        $this->dateDemande = $dateDemande;
        return $this;
    }
    
}