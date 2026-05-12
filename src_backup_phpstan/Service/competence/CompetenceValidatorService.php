<?php

namespace App\Service\competence;

use App\Entity\competence\Competence;
use App\Entity\competence\HistoriqueCompetence;

/**
 * Service de validation des règles métier pour les entités Competence et HistoriqueCompetence.
 *
 * Règles métier validées :
 *  1. La catégorie est obligatoire
 *  2. Le type est obligatoire
 *  3. Le niveau doit appartenir à une liste de valeurs autorisées
 *  4. Les années d'expérience ne peuvent pas être négatives
 *  5. L'email doit être valide
 *  6. Le statut doit appartenir à une liste de valeurs autorisées
 *  7. (HistoriqueCompetence) La date de modification ne peut pas être dans le futur
 *  8. (HistoriqueCompetence) Le statusChange est obligatoire
 */
class CompetenceValidatorService
{
    /** Niveaux autorisés */
    public const NIVEAUX_VALIDES = ['Débutant', 'Intermédiaire', 'Avancé', 'Expert'];

    /** Statuts autorisés */
    public const STATUTS_VALIDES = ['Active', 'Inactive', 'En attente'];

    // =========================================================
    // Validation de l'entité Competence
    // =========================================================

    /**
     * Valide toutes les règles métier d'une Competence.
     *
     * @throws \InvalidArgumentException si une règle est violée
     */
    public function validateCompetence(Competence $competence): bool
    {
        $this->validateCategory($competence->getCategory());
        $this->validateType($competence->getType());
        $this->validateNiveau($competence->getNiveau());
        $this->validateAnneesExperience($competence->getAnneesExperience());
        $this->validateEmail($competence->getEmail());
        $this->validateStatut($competence->getStatut());

        return true;
    }

    /**
     * Règle 1 : La catégorie est obligatoire.
     */
    public function validateCategory(?string $category): void
    {
        if (empty(trim((string) $category))) {
            throw new \InvalidArgumentException('La catégorie est obligatoire.');
        }
    }

    /**
     * Règle 2 : Le type est obligatoire.
     */
    public function validateType(?string $type): void
    {
        if (empty(trim((string) $type))) {
            throw new \InvalidArgumentException('Le type est obligatoire.');
        }
    }

    /**
     * Règle 3 : Le niveau doit appartenir aux valeurs autorisées.
     */
    public function validateNiveau(?string $niveau): void
    {
        if (empty($niveau) || !in_array($niveau, self::NIVEAUX_VALIDES, true)) {
            throw new \InvalidArgumentException(
                sprintf(
                    'Le niveau "%s" est invalide. Valeurs autorisées : %s.',
                    $niveau,
                    implode(', ', self::NIVEAUX_VALIDES)
                )
            );
        }
    }

    /**
     * Règle 4 : Les années d'expérience ne peuvent pas être négatives.
     */
    public function validateAnneesExperience(?int $annees): void
    {
        if ($annees === null || $annees < 0) {
            throw new \InvalidArgumentException(
                'Les années d\'expérience ne peuvent pas être négatives.'
            );
        }
    }

    /**
     * Règle 5 : L'email doit être valide.
     */
    public function validateEmail(?string $email): void
    {
        if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('L\'email est invalide.');
        }
    }

    /**
     * Règle 6 : Le statut doit appartenir aux valeurs autorisées.
     */
    public function validateStatut(?string $statut): void
    {
        if (empty($statut) || !in_array($statut, self::STATUTS_VALIDES, true)) {
            throw new \InvalidArgumentException(
                sprintf(
                    'Le statut "%s" est invalide. Valeurs autorisées : %s.',
                    $statut,
                    implode(', ', self::STATUTS_VALIDES)
                )
            );
        }
    }

    // =========================================================
    // Validation de l'entité HistoriqueCompetence
    // =========================================================

    /**
     * Valide toutes les règles métier d'un HistoriqueCompetence.
     *
     * @throws \InvalidArgumentException si une règle est violée
     */
    public function validateHistorique(HistoriqueCompetence $historique): bool
    {
        $this->validateStatusChange($historique->getStatusChange());
        $this->validateDateModification($historique->getDateModification());

        return true;
    }

    /**
     * Règle 7 : Le statusChange (action) est obligatoire.
     */
    public function validateStatusChange(?string $statusChange): void
    {
        if (empty(trim((string) $statusChange))) {
            throw new \InvalidArgumentException('Le statusChange (action) est obligatoire.');
        }
    }

    /**
     * Règle 8 : La date de modification ne peut pas être dans le futur.
     */
    public function validateDateModification(?\DateTimeInterface $date): void
    {
        if ($date === null) {
            throw new \InvalidArgumentException('La date de modification est obligatoire.');
        }

        if ($date > new \DateTime()) {
            throw new \InvalidArgumentException(
                'La date de modification ne peut pas être dans le futur.'
            );
        }
    }
}
