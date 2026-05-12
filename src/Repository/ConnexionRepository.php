<?php

namespace App\Repository;

use App\Entity\utilisateur\Connexion;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Connexion>
 */
class ConnexionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Connexion::class);
    }

    public function findRelation(Utilisateur $a, Utilisateur $b): ?Connexion
    {
        return $this->createQueryBuilder('c')
            ->where('(c.demandeur = :a AND c.receveur = :b)')
            ->orWhere('(c.demandeur = :b AND c.receveur = :a)')
            ->setParameter('a', $a)
            ->setParameter('b', $b)
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function estDemandeur(Utilisateur $demandeur, Utilisateur $receveur): bool
    {
        return $this->createQueryBuilder('c')
            ->select('COUNT(c.id)')
            ->where('c.demandeur = :demandeur')
            ->andWhere('c.receveur = :receveur')
            ->setParameter('demandeur', $demandeur)
            ->setParameter('receveur', $receveur)
            ->getQuery()
            ->getSingleScalarResult() > 0;
    }

    /**
     * @return array<int, Connexion>
     */
    public function getInvitationsRecues(Utilisateur $user): array
    {
        return $this->createQueryBuilder('c')
            ->where('c.receveur = :user')
            ->andWhere('c.statut = :statut')
            ->setParameter('user', $user)
            ->setParameter('statut', 'en_attente')
            ->orderBy('c.dateDemande', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return array<int, Connexion>
     */
    public function getConnexions(Utilisateur $user): array
    {
        return $this->createQueryBuilder('c')
            ->where('(c.demandeur = :user OR c.receveur = :user)')
            ->andWhere('c.statut = :statut')
            ->setParameter('user', $user)
            ->setParameter('statut', 'accepte')
            ->orderBy('c.dateDemande', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return array<int, Connexion>
     */
    public function getContacts(Utilisateur $user): array
    {
        return $this->getConnexions($user);
    }

    /**
     * @return array<int, mixed>
     */
    public function getContactUsers(Utilisateur $user): array
    {
        $connexions = $this->getConnexions($user);
        $contacts = [];

        foreach ($connexions as $connexion) {
            if ($connexion->getDemandeur()?->getIdUtilisateur() === $user->getIdUtilisateur()) {
                $contacts[] = $connexion->getReceveur();
            } else {
                $contacts[] = $connexion->getDemandeur();
            }
        }

        return array_filter($contacts);
    }
}

