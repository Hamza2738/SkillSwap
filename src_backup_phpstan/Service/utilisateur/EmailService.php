<?php

namespace App\Service\utilisateur;

use Doctrine\ORM\EntityManagerInterface;

/**
 * Service d'historique des emails envoyés.
 */
class EmailHistoryService
{
    public function __construct(
        private readonly EntityManagerInterface $em
    ) {
    }

    /** @return array<int, array<string, mixed>> */
    public function getAllEmails(): array
    {
        return $this->em->getConnection()->fetchAllAssociative(
            "SELECT * FROM email_history ORDER BY date_envoi DESC"
        );
    }

    /** @return array<int, array<string, mixed>> */
    public function getEmailsByDestinataire(string $email): array
    {
        return $this->em->getConnection()->fetchAllAssociative(
            "SELECT * FROM email_history WHERE destinataire=? ORDER BY date_envoi DESC",
            [$email]
        );
    }
}
