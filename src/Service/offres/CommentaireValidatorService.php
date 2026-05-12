<?php

namespace App\Service\offres;

use App\Entity\Offres\Commentaire;

class CommentaireValidatorService
{
    public function validate(Commentaire $commentaire): bool
    {
        if ($commentaire->getIdOffre() === null || $commentaire->getIdOffre() <= 0) {
            throw new \InvalidArgumentException('L’offre est obligatoire.');
        }

        if ($commentaire->getIdUtilisateur() === null || $commentaire->getIdUtilisateur() <= 0) {
            throw new \InvalidArgumentException('L’utilisateur est obligatoire.');
        }

        if (empty(trim((string) $commentaire->getContenu()))) {
            throw new \InvalidArgumentException('Le contenu du commentaire est obligatoire.');
        }

        if (strlen(trim((string) $commentaire->getContenu())) < 3) {
            throw new \InvalidArgumentException('Le commentaire doit contenir au moins 3 caractères.');
        }

        if ($commentaire->getDateCommentaire() !== null && $commentaire->getDateCommentaire() > new \DateTimeImmutable('now')) {
            throw new \InvalidArgumentException('La date du commentaire ne peut pas être dans le futur.');
        }

        return true;
    }
}