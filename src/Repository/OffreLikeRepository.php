<?php

namespace App\Repository;

use App\Entity\Offres\OffreLike;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class OffreLikeRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, OffreLike::class);
    }

    public function findOneByOffreAndUser(int $idOffre, int $idUtilisateur): ?OffreLike
    {
        return $this->findOneBy([
            'idOffre' => $idOffre,
            'idUtilisateur' => $idUtilisateur,
        ]);
    }

    public function countByOffre(int $idOffre): int
    {
        return (int) $this->createQueryBuilder('l')
            ->select('COUNT(l.id)')
            ->where('l.idOffre = :idOffre')
            ->setParameter('idOffre', $idOffre)
            ->getQuery()
            ->getSingleScalarResult();
    }
}