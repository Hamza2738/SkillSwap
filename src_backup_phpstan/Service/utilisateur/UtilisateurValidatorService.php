<?php

namespace App\Service\utilisateur;

use App\Entity\utilisateur\Utilisateur;

class UtilisateurValidatorService
{
    public function validate(Utilisateur $utilisateur): bool
    {
        if (empty(trim((string) $utilisateur->getNom()))) {
            throw new \InvalidArgumentException('Le nom est obligatoire.');
        }

        if (empty(trim((string) $utilisateur->getPrenom()))) {
            throw new \InvalidArgumentException('Le prénom est obligatoire.');
        }

        if (empty(trim((string) $utilisateur->getEmail()))) {
            throw new \InvalidArgumentException('L’email est obligatoire.');
        }

        if (!filter_var($utilisateur->getEmail(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('Email invalide.');
        }

        if (empty(trim((string) $utilisateur->getMotDePasse()))) {
            throw new \InvalidArgumentException('Le mot de passe est obligatoire.');
        }

        if (strlen($utilisateur->getMotDePasse()) < 8) {
            throw new \InvalidArgumentException('Le mot de passe doit contenir au moins 8 caractères.');
        }

        if (!empty($utilisateur->getTelephone()) && strlen($utilisateur->getTelephone()) < 8) {
            throw new \InvalidArgumentException('Le numéro de téléphone est invalide.');
        }

        $rolesAutorises = ['admin', 'freelancer', 'entrepreneur', 'ROLE_ADMIN', 'ROLE_USER'];

        if (!empty($utilisateur->getRole()) && !in_array($utilisateur->getRole(), $rolesAutorises, true)) {
            throw new \InvalidArgumentException('Rôle utilisateur invalide.');
        }

        $statutsAutorises = ['actif', 'inactif', 'bloque'];

        if (!empty($utilisateur->getStatut()) && !in_array($utilisateur->getStatut(), $statutsAutorises, true)) {
            throw new \InvalidArgumentException('Statut utilisateur invalide.');
        }

        return true;
    }
}