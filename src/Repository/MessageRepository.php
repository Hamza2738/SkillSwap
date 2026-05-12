<?php

namespace App\Repository;

use App\Entity\utilisateur\Message;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Message>
 */
class MessageRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Message::class);
    }

    /**
     * @return array<int, Message>
     */
    public function getConversation(Utilisateur $a, Utilisateur $b): array
    {
        return $this->createQueryBuilder('m')
            ->where('(m.envoyeur = :a AND m.receveur = :b)')
            ->orWhere('(m.envoyeur = :b AND m.receveur = :a)')
            ->setParameter('a', $a)
            ->setParameter('b', $b)
            ->orderBy('m.dateEnvoi', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function countNonLus(Utilisateur $envoyeur, Utilisateur $receveur): int
    {
        return (int) $this->createQueryBuilder('m')
            ->select('COUNT(m.id)')
            ->where('m.envoyeur = :envoyeur')
            ->andWhere('m.receveur = :receveur')
            ->andWhere('m.lu = false')
            ->setParameter('envoyeur', $envoyeur)
            ->setParameter('receveur', $receveur)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function marquerLus(Utilisateur $envoyeur, Utilisateur $receveur): void
    {
        $this->createQueryBuilder('m')
            ->update()
            ->set('m.lu', ':lu')
            ->where('m.envoyeur = :envoyeur')
            ->andWhere('m.receveur = :receveur')
            ->andWhere('m.lu = false')
            ->setParameter('lu', true)
            ->setParameter('envoyeur', $envoyeur)
            ->setParameter('receveur', $receveur)
            ->getQuery()
            ->execute();
    }
}

