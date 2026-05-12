<?php

namespace App\Service\offres;

use App\Entity\Offres\OffreLike;

class OffreLikeValidatorService
{
    public function validate(OffreLike $offreLike): bool
    {
        if ($offreLike->getIdOffre() === null || $offreLike->getIdOffre() <= 0) {
            throw new \InvalidArgumentException('L’offre est obligatoire.');
        }

        if ($offreLike->getIdUtilisateur() === null || $offreLike->getIdUtilisateur() <= 0) {
            throw new \InvalidArgumentException('L’utilisateur est obligatoire.');
        }

        return true;
    }
}