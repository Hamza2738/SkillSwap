<?php

namespace App\Entity\Offres;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'offre_like')]
class OffreLike
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_offre', type: 'integer')]
    private ?int $idOffre = null;

    #[ORM\Column(name: 'id_utilisateur', type: 'integer')]
    private ?int $idUtilisateur = null;

    public function getId(): ?int
    {
        return $this->id;
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
}