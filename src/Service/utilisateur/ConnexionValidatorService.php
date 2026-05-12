<?php

namespace App\Service\utilisateur;

use App\Entity\utilisateur\Connexion;

class ConnexionValidatorService
{
    public function validate(Connexion $connexion): bool
    {
        if ($connexion->getDemandeur() === null) {
            throw new \InvalidArgumentException('Le demandeur est obligatoire.');
        }

        if ($connexion->getReceveur() === null) {
            throw new \InvalidArgumentException('Le receveur est obligatoire.');
        }

        if ($connexion->getDemandeur() === $connexion->getReceveur()) {
            throw new \InvalidArgumentException('Le demandeur et le receveur doivent être différents.');
        }

        $statutsAutorises = ['en_attente', 'accepte', 'refuse'];

        if (!in_array($connexion->getStatut(), $statutsAutorises, true)) {
            throw new \InvalidArgumentException('Statut de connexion invalide.');
        }

        if ($connexion->getDateDemande() === null) {
            throw new \InvalidArgumentException('La date de demande est obligatoire.');
        }

        if ($connexion->getDateDemande() > new \DateTimeImmutable('now')) {
            throw new \InvalidArgumentException('La date de demande ne peut pas être dans le futur.');
        }

        return true;
    }
}