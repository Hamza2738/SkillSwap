<?php

namespace App\Service\RendezVous;

use App\Entity\RendezVous\Postulation;

class PostulationValidatorService
{
    public function validate(Postulation $postulation): bool
    {
        if ($postulation->getIdRendezVous() === null || $postulation->getIdRendezVous() <= 0) {
            throw new \InvalidArgumentException('Le rendez-vous est obligatoire.');
        }

        if ($postulation->getIdPostulant() === null || $postulation->getIdPostulant() <= 0) {
            throw new \InvalidArgumentException('Le postulant est obligatoire.');
        }

        if (!empty($postulation->getMailUser()) && !filter_var($postulation->getMailUser(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('Email utilisateur invalide.');
        }

        if (empty($postulation->getStatus())) {
            throw new \InvalidArgumentException('Le statut est obligatoire.');
        }

        $statusAutorises = ['en_attente', 'accepte', 'refuse'];

        if (!in_array($postulation->getStatus(), $statusAutorises, true)) {
            throw new \InvalidArgumentException('Statut invalide.');
        }

        if (!empty($postulation->getCv())) {
            $extension = strtolower(pathinfo($postulation->getCv(), PATHINFO_EXTENSION));

            if ($extension !== 'pdf') {
                throw new \InvalidArgumentException('Le CV doit être un fichier PDF.');
            }
        }

        return true;
    }
}