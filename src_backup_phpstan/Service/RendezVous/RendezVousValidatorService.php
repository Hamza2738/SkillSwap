<?php

namespace App\Service\RendezVous;

use App\Entity\RendezVous\RendezVous;

class RendezVousValidatorService
{
    public function validate(RendezVous $rendezVous): bool
    {
        if ($rendezVous->getIdAdminCreateur() === null || $rendezVous->getIdAdminCreateur() <= 0) {
            throw new \InvalidArgumentException('L’administrateur créateur est obligatoire.');
        }

        if (!empty($rendezVous->getMailUser()) && !filter_var($rendezVous->getMailUser(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('Email utilisateur invalide.');
        }

        if (empty($rendezVous->getTitre())) {
            throw new \InvalidArgumentException('Le titre du rendez-vous est obligatoire.');
        }

        if ($rendezVous->getDateRendezVous() === null) {
            throw new \InvalidArgumentException('La date du rendez-vous est obligatoire.');
        }

        if ($rendezVous->getDateRendezVous() < new \DateTimeImmutable('now')) {
            throw new \InvalidArgumentException('La date du rendez-vous ne peut pas être dans le passé.');
        }

        if (empty($rendezVous->getType())) {
            throw new \InvalidArgumentException('Le type du rendez-vous est obligatoire.');
        }

        if (empty($rendezVous->getCompetence())) {
            throw new \InvalidArgumentException('La compétence est obligatoire.');
        }

        if ($rendezVous->getNombrePlaces() === null || $rendezVous->getNombrePlaces() <= 0) {
            throw new \InvalidArgumentException('Le nombre de places doit être supérieur à zéro.');
        }

        return true;
    }
}