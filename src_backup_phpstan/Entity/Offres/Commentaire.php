<?php

namespace App\Entity\Offres;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'commentaire')]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_commentaire', type: 'integer')]
    private ?int $idCommentaire = null;

    #[ORM\Column(name: 'id_offre', type: 'integer')]
    private ?int $idOffre = null;

    #[ORM\Column(name: 'id_utilisateur', type: 'integer')]
    private ?int $idUtilisateur = null;

    #[ORM\Column(name: 'contenu', type: 'text')]
    private ?string $contenu = null;

    #[ORM\Column(name: 'date_commentaire', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCommentaire = null;

    public function getIdCommentaire(): ?int
    {
        return $this->idCommentaire;
    }

    public function getIdOffre(): ?int
    {
        return $this->idOffre;
    }

    public function setIdOffre(?int $idOffre): self
    {
        $this->idOffre = $idOffre;
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

    public function getContenu(): ?string
    {
        return $this->contenu;
    }

    public function setContenu(?string $contenu): self
    {
        $this->contenu = $contenu;
        return $this;
    }

    public function getDateCommentaire(): ?\DateTimeInterface
    {
        return $this->dateCommentaire;
    }

    public function setDateCommentaire(?\DateTimeInterface $dateCommentaire): self
    {
        $this->dateCommentaire = $dateCommentaire;
        return $this;
    }
}