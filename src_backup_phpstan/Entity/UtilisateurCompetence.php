<?php

namespace App\Entity;

use App\Entity\competence\Competence;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'utilisateur_competence')]
class UtilisateurCompetence
{
    #[ORM\Id]
    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_utilisateur', referencedColumnName: 'id_utilisateur', onDelete: 'CASCADE')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\Id]
    #[ORM\ManyToOne(targetEntity: Competence::class)]
    #[ORM\JoinColumn(name: 'id_competence', referencedColumnName: 'id', onDelete: 'CASCADE')]
    private ?Competence $competence = null;

    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function setUtilisateur(?Utilisateur $v): self { $this->utilisateur = $v; return $this; }

    public function getCompetence(): ?Competence { return $this->competence; }
    public function setCompetence(?Competence $v): self { $this->competence = $v; return $this; }
}