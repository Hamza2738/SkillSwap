<?php

namespace App\Service\offres;

use App\Entity\Offres\Offre;

class OffreValidatorService
{
    public function validate(Offre $offre): bool
    {
        if (empty($offre->getTitre())) {
            throw new \InvalidArgumentException('Le titre de l’offre est obligatoire.');
        }

        if ($offre->getPrix() !== null && (float) $offre->getPrix() <= 0) {
            throw new \InvalidArgumentException('Le prix doit être supérieur à zéro.');
        }

        if ($offre->getBudget() !== null && (float) $offre->getBudget() <= 0) {
            throw new \InvalidArgumentException('Le budget doit être supérieur à zéro.');
        }

        if ($offre->getDuree() !== null && $offre->getDuree() <= 0) {
            throw new \InvalidArgumentException('La durée doit être supérieure à zéro.');
        }

        if ($offre->getDateLimite() !== null && $offre->getDateLimite() < new \DateTimeImmutable('today')) {
            throw new \InvalidArgumentException('La date limite ne peut pas être dans le passé.');
        }

        if ($offre->getMailUser() !== null && !filter_var($offre->getMailUser(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('Email utilisateur invalide.');
        }

        return true;
    }
}