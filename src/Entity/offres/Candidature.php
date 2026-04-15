<?php

namespace App\Entity\offres;
use App\Entity\utilisateur\Utilisateur;
use App\Entity\Offre;
use App\Repository\CandidatureRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: CandidatureRepository::class)]
#[ORM\Table(name: 'candidature')]
class Candidature
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_candidature', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\Column(name: 'date_candidature', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateCandidature = null;

    #[ORM\Column(name: 'date_abonnement', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateAbonnement = null;

    #[ORM\Column(name: 'date_expiration', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateExpiration = null;

    #[ORM\Column(name: 'matricule_offre', length: 50, nullable: true)]
    private ?string $matriculeOffre = null;

    #[ORM\Column(name: 'mail_acheteur', length: 150, nullable: true)]
    private ?string $mailAcheteur = null;

    #[ORM\Column(name: 'prix_paye', type: Types::DECIMAL, precision: 12, scale: 2, nullable: true)]
    private ?string $prixPaye = null;

    #[ORM\Column(name: 'mode_paiement', length: 50, nullable: true)]
    private ?string $modePaiement = null;

    #[ORM\Column(name: 'reference_paiement', length: 255, nullable: true)]
    private ?string $referencePaiement = null;

    #[ORM\Column(name: 'statut', length: 50, options: ['default' => 'en_attente'])]
    private ?string $statut = 'en_attente';

    #[ORM\Column(name: 'message', type: Types::TEXT, nullable: true)]
    private ?string $message = null;

    #[ORM\Column(name: 'cv', length: 255, nullable: true)]
    private ?string $cv = null;

    #[ORM\Column(name: 'lettre_motivation', length: 255, nullable: true)]
    private ?string $lettreMotivation = null;

    #[ORM\Column(name: 'note_recruteur', type: Types::TEXT, nullable: true)]
    private ?string $noteRecruteur = null;

    #[ORM\Column(name: 'note_candidat', type: Types::SMALLINT, nullable: true)]
    private ?int $noteCandidat = null;

    #[ORM\Column(name: 'is_active', type: Types::BOOLEAN, options: ['default' => true])]
    private bool $isActive = true;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_utilisateur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\ManyToOne(targetEntity: Offre::class)]
    #[ORM\JoinColumn(name: 'id_offre', referencedColumnName: 'id_offre', nullable: false, onDelete: 'CASCADE')]
    private ?Offre $offre = null;

    public function __construct()
    {
        $this->dateCandidature = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getDateCandidature(): ?\DateTimeInterface { return $this->dateCandidature; }
    public function getDateAbonnement(): ?\DateTimeInterface { return $this->dateAbonnement; }
    public function getDateExpiration(): ?\DateTimeInterface { return $this->dateExpiration; }
    public function getMatriculeOffre(): ?string { return $this->matriculeOffre; }
    public function getMailAcheteur(): ?string { return $this->mailAcheteur; }
    public function getPrixPaye(): ?string { return $this->prixPaye; }
    public function getModePaiement(): ?string { return $this->modePaiement; }
    public function getReferencePaiement(): ?string { return $this->referencePaiement; }
    public function getStatut(): ?string { return $this->statut; }
    public function getMessage(): ?string { return $this->message; }
    public function getCv(): ?string { return $this->cv; }
    public function getLettreMotivation(): ?string { return $this->lettreMotivation; }
    public function getNoteRecruteur(): ?string { return $this->noteRecruteur; }
    public function getNoteCandidat(): ?int { return $this->noteCandidat; }
    public function isActive(): bool { return $this->isActive; }
    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function getOffre(): ?Offre { return $this->offre; }

    // ── Setters ──────────────────────────────────────────────
    public function setDateCandidature(?\DateTimeInterface $v): self { $this->dateCandidature = $v; return $this; }
    public function setDateAbonnement(?\DateTimeInterface $v): self { $this->dateAbonnement = $v; return $this; }
    public function setDateExpiration(?\DateTimeInterface $v): self { $this->dateExpiration = $v; return $this; }
    public function setMatriculeOffre(?string $v): self { $this->matriculeOffre = $v; return $this; }
    public function setMailAcheteur(?string $v): self { $this->mailAcheteur = $v; return $this; }
    public function setPrixPaye(?string $v): self { $this->prixPaye = $v; return $this; }
    public function setModePaiement(?string $v): self { $this->modePaiement = $v; return $this; }
    public function setReferencePaiement(?string $v): self { $this->referencePaiement = $v; return $this; }
    public function setStatut(?string $v): self { $this->statut = $v; return $this; }
    public function setMessage(?string $v): self { $this->message = $v; return $this; }
    public function setCv(?string $v): self { $this->cv = $v; return $this; }
    public function setLettreMotivation(?string $v): self { $this->lettreMotivation = $v; return $this; }
    public function setNoteRecruteur(?string $v): self { $this->noteRecruteur = $v; return $this; }
    public function setNoteCandidat(?int $v): self { $this->noteCandidat = $v; return $this; }
    public function setActive(bool $v): self { $this->isActive = $v; return $this; }
    public function setUtilisateur(?Utilisateur $v): self { $this->utilisateur = $v; return $this; }
    public function setOffre(?Offre $v): self { $this->offre = $v; return $this; }
}