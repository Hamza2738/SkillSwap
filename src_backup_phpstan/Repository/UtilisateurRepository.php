<?php

namespace App\Repository;

use App\Entity\utilisateur\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Utilisateur>
 */
class UtilisateurRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Utilisateur::class);
    }

    public function findOneByEmail(string $email): ?Utilisateur
    {
        return $this->createQueryBuilder('u')
            ->andWhere('u.email = :email')
            ->setParameter('email', $email)
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * @return array<int, Utilisateur>
     */
    public function searchSuggestions(string $q, int $limit = 10): array
    {
        $q = trim($q);

        if (mb_strlen($q) < 2) {
            return [];
        }

        $safeLimit = max(1, min($limit, 15));
        $parts = preg_split('/\s+/', mb_strtolower($q));

        $p1   = '%' . ($parts[0] ?? '') . '%';
        $p2   = '%' . ($parts[1] ?? ($parts[0] ?? '')) . '%';
        $full = '%' . mb_strtolower($q) . '%';

        return $this->createQueryBuilder('u')
            ->andWhere('u.statut != :statut')
            ->andWhere(
                '(LOWER(u.nom) LIKE :full
                OR LOWER(u.prenom) LIKE :full
                OR LOWER(u.email) LIKE :full
                OR (LOWER(u.nom) LIKE :p1 AND LOWER(u.prenom) LIKE :p2)
                OR (LOWER(u.nom) LIKE :p2 AND LOWER(u.prenom) LIKE :p1))'
            )
            ->setParameter('statut', 'supprime')
            ->setParameter('full', $full)
            ->setParameter('p1', $p1)
            ->setParameter('p2', $p2)
            ->orderBy('u.nom', 'ASC')
            ->addOrderBy('u.prenom', 'ASC')
            ->setMaxResults($safeLimit)
            ->getQuery()
            ->getResult();
    }
}
