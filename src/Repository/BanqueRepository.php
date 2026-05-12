<?php

namespace App\Repository;

use App\Entity\Offres\Banque;
use App\Entity\Offres\Offre;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
/**
 * @extends ServiceEntityRepository<Banque>
 */
/**
 * @extends ServiceEntityRepository<Banque>
 */
class BanqueRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Banque::class);
    }

    public function findByOffre(Offre $offre): ?Banque
    {
        return $this->createQueryBuilder('b')
            ->andWhere('b.offre = :offre')
            ->setParameter('offre', $offre)
            ->orderBy('b.id', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * @return array<int, Banque>
     */
    public function findPaidPayments(): array
    {
        return $this->createQueryBuilder('b')
            ->andWhere('b.statut = :statut')
            ->setParameter('statut', 'payee')
            ->orderBy('b.datePaiement', 'DESC')
            ->getQuery()
            ->getResult();
    }
}

