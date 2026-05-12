<?php

namespace App\Service\utilisateur;

use App\Entity\utilisateur\Connexion;
use App\Repository\ConnexionRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * Service de gestion des connexions/invitations entre utilisateurs.
 */
class ConnexionService
{
    public function __construct(
        private readonly EntityManagerInterface $em
    ) {
    }

    public function envoyerInvitation(int $idDemandeur, int $idReceveur): void
    {
        $this->em->getConnection()->executeStatement(
            "INSERT INTO connexion (id_demandeur, id_receveur, statut) VALUES (?, ?, 'en_attente')",
            [$idDemandeur, $idReceveur]
        );
    }

    public function accepterInvitation(int $idDemandeur, int $idReceveur): void
    {
        $this->em->getConnection()->executeStatement(
            "UPDATE connexion SET statut='accepte' WHERE id_demandeur=? AND id_receveur=?",
            [$idDemandeur, $idReceveur]
        );
    }

    public function supprimerConnexion(int $idA, int $idB): void
    {
        $this->em->getConnection()->executeStatement(
            "DELETE FROM connexion
             WHERE (id_demandeur=? AND id_receveur=?) OR (id_demandeur=? AND id_receveur=?)",
            [$idA, $idB, $idB, $idA]
        );
    }

    public function getStatut(int $idA, int $idB): ?string
    {
        /** @var array<string, mixed>|false $res */
        $res = $this->em->getConnection()->fetchAssociative(
            "SELECT statut FROM connexion
             WHERE (id_demandeur=? AND id_receveur=?) OR (id_demandeur=? AND id_receveur=?)
             LIMIT 1",
            [$idA, $idB, $idB, $idA]
        );

        return $res !== false ? (string) $res['statut'] : null;
    }

    public function estDemandeur(int $idDemandeur, int $idReceveur): bool
    {
        /** @var array<string, mixed>|false $res */
        $res = $this->em->getConnection()->fetchAssociative(
            "SELECT id FROM connexion WHERE id_demandeur=? AND id_receveur=? LIMIT 1",
            [$idDemandeur, $idReceveur]
        );

        return $res !== false;
    }

    /** @return array<int, array<string, mixed>> */
    public function getInvitationsRecues(int $idReceveur): array
    {
        return $this->em->getConnection()->fetchAllAssociative(
            "SELECT * FROM connexion WHERE id_receveur=? AND statut='en_attente'",
            [$idReceveur]
        );
    }

    /** @return array<int, array<string, mixed>> */
    public function getConnexions(int $idUtilisateur): array
    {
        return $this->em->getConnection()->fetchAllAssociative(
            "SELECT * FROM connexion
             WHERE (id_demandeur=? OR id_receveur=?) AND statut='accepte'",
            [$idUtilisateur, $idUtilisateur]
        );
    }
}
