<?php

namespace App\Entity\offres;
use App\Entity\utilisateur\Utilisateur;
use App\Entity\Offre;

use App\Repository\CommentaireRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: CommentaireRepository::class)]
#[ORM\Table(name: 'commentaire')]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_commentaire', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Offre::class)]
    #[ORM\JoinColumn(name: 'id_offre', referencedColumnName: 'id_offre', nullable: false, onDelete: 'CASCADE')]
    private ?Offre $offre = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_utilisateur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\Column(name: 'contenu', type: Types::TEXT)]
    private ?string $contenu = null;

    #[ORM\Column(name: 'date_commentaire', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateCommentaire = null;

    public function __construct()
    {
        $this->dateCommentaire = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────────
    public function getId(): ?int { return $this->id; }
    public function getOffre(): ?Offre { return $this->offre; }
    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function getContenu(): ?string { return $this->contenu; }
    public function getDateCommentaire(): ?\DateTimeInterface { return $this->dateCommentaire; }

    // ── Setters ──────────────────────────────────────────────
    public function setOffre(?Offre $v): self { $this->offre = $v; return $this; }
    public function setUtilisateur(?Utilisateur $v): self { $this->utilisateur = $v; return $this; }
    public function setContenu(?string $v): self { $this->contenu = $v; return $this; }
    public function setDateCommentaire(?\DateTimeInterface $v): self { $this->dateCommentaire = $v; return $this; }
}