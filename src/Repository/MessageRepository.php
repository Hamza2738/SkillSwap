<?php

namespace App\Repository;

use App\Entity\Message;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * MessageRepository — version finale.
 *
 * Les requêtes DQL joignent via les relations ManyToOne
 * (envoyeur / receveur) et filtrent sur les IDs d'utilisateur,
 * ce qui correspond exactement aux requêtes SQL procédurales
 * utilisant id_envoyeur / id_receveur.
 */
class MessageRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
      //  parent::__construct($registry, Message::class);
    }

    // =========================================================
    // CONVERSATION BIDIRECTIONNELLE TRIÉE PAR DATE
    // Équivalent SQL procédural :
    //   SELECT * FROM message
    //   WHERE (id_envoyeur=? AND id_receveur=?)
    //      OR (id_envoyeur=? AND id_receveur=?)
    //   ORDER BY date_envoi ASC
    //
    // En DQL on filtre via les relations :
    //   m.envoyeur = utilisateur(idA) plutôt que id_envoyeur = idA
    // =========================================================
    public function findConversation(int $idA, int $idB): array
    {
        return $this->createQueryBuilder('m')
            ->where(
                '(IDENTITY(m.envoyeur) = :a AND IDENTITY(m.receveur) = :b)
                 OR (IDENTITY(m.envoyeur) = :b AND IDENTITY(m.receveur) = :a)'
            )
            ->setParameter('a', $idA)
            ->setParameter('b', $idB)
            ->orderBy('m.dateEnvoi', 'ASC')
            ->getQuery()
            ->getResult();
    }

    // =========================================================
    // COMPTE LES MESSAGES NON LUS
    // Équivalent SQL procédural :
    //   SELECT COUNT(*) as nb FROM message
    //   WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE
    // =========================================================
    public function countNonLus(int $idEnvoyeur, int $idReceveur): int
    {
        return (int) $this->createQueryBuilder('m')
            ->select('COUNT(m.id)')
            ->where(
                'IDENTITY(m.envoyeur) = :env
                 AND IDENTITY(m.receveur) = :rec
                 AND m.lu = false'
            )
            ->setParameter('env', $idEnvoyeur)
            ->setParameter('rec', $idReceveur)
            ->getQuery()
            ->getSingleScalarResult();
    }

    // =========================================================
    // MARQUE LES MESSAGES COMME LUS
    // Équivalent SQL procédural :
    //   UPDATE message SET lu=TRUE
    //   WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE
    //
    // UPDATE DQL ne supporte pas IDENTITY() directement,
    // on utilise donc une requête SQL native via la connexion.
    // =========================================================
    public function marquerLus(int $idEnvoyeur, int $idReceveur): void
    {
        $this->getEntityManager()
            ->getConnection()
            ->executeStatement(
                'UPDATE message
                 SET lu = TRUE
                 WHERE id_envoyeur = :env
                   AND id_receveur = :rec
                   AND lu = FALSE',
                ['env' => $idEnvoyeur, 'rec' => $idReceveur]
            );
    }
}