<?php

namespace App\Repository;

use App\Entity\Offre;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * Repository pour les likes d'offres.
 * L'entité OffreLike n'existe pas encore — on pointe temporairement sur Offre.
 *
 * @extends ServiceEntityRepository<Offre>
 */
class OffreLikeRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Offre::class);
    }
}
