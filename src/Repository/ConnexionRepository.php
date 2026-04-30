<?php

namespace App\Repository;

use App\Entity\Connexion;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class ConnexionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
  //      parent::__construct($registry, Connexion::class);
    }
}