<?php

namespace App\Entity\Offres;

use App\Entity\Offres\Offre;
use App\Entity\utilisateur\Utilisateur;
use App\Repository\CandidatureRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CandidatureRepository::class)]
#[ORM\Table(name: 'candidature')]
class Candidature
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_candidature', type: 'integer')]
    private ?int $idCandidature = null;

    #[ORM\Column(name: 'date_candidature', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCandidature = null;

    #[ORM\Column(name: 'date_abonnement', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateAbonnement = null;

    #[ORM\Column(name: 'date_expiration', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateExpiration = null;

    #[ORM\Column(name: 'matricule_offre', type: 'string', length: 50, nullable: true)]
    private ?string $matriculeOffre = null;

    #[ORM\Column(name: 'mail_acheteur', type: 'string', length: 150, nullable: true)]
    private ?string $mailAcheteur = null;

    #[ORM\Column(name: 'prix_paye', type: 'decimal', precision: 12, scale: 2, nullable: true)]
    private ?string $prixPaye = null;

    #[ORM\Column(name: 'mode_paiement', type: 'string', length: 50, nullable: true)]
    private ?string $modePaiement = null;

    #[ORM\Column(name: 'reference_paiement', type: 'string', length: 255, nullable: true)]
    private ?string $referencePaiement = null;

    #[ORM\Column(name: 'statut', type: 'string', length: 50, nullable: true)]
    private ?string $statut = 'en_attente';

    #[ORM\Column(name: 'message', type: 'text', nullable: true)]
    private ?string $message = null;

    #[ORM\Column(name: 'cv', type: 'string', length: 255, nullable: true)]
    private ?string $cv = null;

    #[ORM\Column(name: 'lettre_motivation', type: 'string', length: 255, nullable: true)]
    private ?string $lettreMotivation = null;

    #[ORM\Column(name: 'note_recruteur', type: 'text', nullable: true)]
    private ?string $noteRecruteur = null;

    #[ORM\Column(name: 'note_candidat', type: 'integer', nullable: true)]
    private ?int $noteCandidat = null;

    #[ORM\Column(name: 'is_active', type: 'boolean', options: ['default' => true])]
    private ?bool $isActive = true;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_utilisateur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\ManyToOne(targetEntity: Offre::class)]
    #[ORM\JoinColumn(name: 'id_offre', referencedColumnName: 'id_offre', nullable: false, onDelete: 'CASCADE')]
    private ?Offre $offre = null;

    public function getIdCandidature(): ?int
    {
        return $this->idCandidature;
    }

    public function getDateCandidature(): ?\DateTimeInterface
    {
        return $this->dateCandidature;
    }

    public function setDateCandidature(?\DateTimeInterface $dateCandidature): self
    {
        $this->dateCandidature = $dateCandidature;
        return $this;
    }

    public function getDateAbonnement(): ?\DateTimeInterface
    {
        return $this->dateAbonnement;
    }

    public function setDateAbonnement(?\DateTimeInterface $dateAbonnement): self
    {
        $this->dateAbonnement = $dateAbonnement;
        return $this;
    }

    public function getDateExpiration(): ?\DateTimeInterface
    {
        return $this->dateExpiration;
    }

    public function setDateExpiration(?\DateTimeInterface $dateExpiration): self
    {
        $this->dateExpiration = $dateExpiration;
        return $this;
    }

    public function getMatriculeOffre(): ?string
    {
        return $this->matriculeOffre;
    }

    public function setMatriculeOffre(?string $matriculeOffre): self
    {
        $this->matriculeOffre = $matriculeOffre;
        return $this;
    }

    public function getMailAcheteur(): ?string
    {
        return $this->mailAcheteur;
    }

    public function setMailAcheteur(?string $mailAcheteur): self
    {
        $this->mailAcheteur = $mailAcheteur;
        return $this;
    }

    public function getPrixPaye(): ?string
    {
        return $this->prixPaye;
    }

    public function setPrixPaye($prixPaye): self
    {
        $this->prixPaye = $prixPaye !== null ? (string) $prixPaye : null;
        return $this;
    }

    public function getModePaiement(): ?string
    {
        return $this->modePaiement;
    }

    public function setModePaiement(?string $modePaiement): self
    {
        $this->modePaiement = $modePaiement;
        return $this;
    }

    public function getReferencePaiement(): ?string
    {
        return $this->referencePaiement;
    }

    public function setReferencePaiement(?string $referencePaiement): self
    {
        $this->referencePaiement = $referencePaiement;
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

    public function getStatutLabel(): string
    {
        return match ($this->statut) {
            'payee' => 'Payée',
            'acceptee' => 'Acceptée',
            'refusee' => 'Refusée',
            'en_attente' => 'En attente',
            default => $this->statut ?? '—',
        };
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

    public function getLettreMotivation(): ?string
    {
        return $this->lettreMotivation;
    }

    public function setLettreMotivation(?string $lettreMotivation): self
    {
        $this->lettreMotivation = $lettreMotivation;
        return $this;
    }

    public function getNoteRecruteur(): ?string
    {
        return $this->noteRecruteur;
    }

    public function setNoteRecruteur(?string $noteRecruteur): self
    {
        $this->noteRecruteur = $noteRecruteur;
        return $this;
    }

    public function getNoteCandidat(): ?int
    {
        return $this->noteCandidat;
    }

    public function setNoteCandidat(?int $noteCandidat): self
    {
        $this->noteCandidat = $noteCandidat;
        return $this;
    }

    public function isActive(): ?bool
    {
        return $this->isActive;
    }

    public function getIsActive(): ?bool
    {
        return $this->isActive;
    }

    public function setIsActive(?bool $isActive): self
    {
        $this->isActive = $isActive;
        return $this;
    }

    public function getAbonnementLabel(): string
    {
        return $this->isActive ? 'Oui' : 'Non';
    }

    public function getUtilisateur(): ?Utilisateur
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(?Utilisateur $utilisateur): self
    {
        $this->utilisateur = $utilisateur;
        return $this;
    }

    public function getOffre(): ?Offre
    {
        return $this->offre;
    }

    public function setOffre(?Offre $offre): self
    {
        $this->offre = $offre;
        return $this;
    }
}