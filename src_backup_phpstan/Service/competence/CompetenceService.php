<?php

namespace App\Service\competence;

use App\Entity\competence\Competence;
use Doctrine\ORM\EntityManagerInterface;

class CompetenceService
{
    private EntityManagerInterface $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    /** @return array<int, Competence> */
    public function getAll(): array
    {
        return $this->em->getRepository(Competence::class)->findAll();
    }

    // ✅ FIX: Validate that email exists in utilisateur before persisting
   // CompetenceService.php — retirer ou assouplir cette vérification
public function add(Competence $c): void
{
    $this->em->persist($c);
    $this->em->flush();
}

    public function update(Competence $c): void
    {
        $this->em->flush();
    }

    // ✅ FIX: Return the competence object so the controller
    //         can log historique BEFORE removal (ID still valid)
    public function delete(int $id): ?Competence
    {
        $c = $this->em->getRepository(Competence::class)->find($id);
        if ($c) {
            $this->em->remove($c);
            $this->em->flush();
            return $c; // ✅ return after flush — ID still in object memory
        }
        return null;
    }

    public function find(int $id): ?Competence
    {
        return $this->em->getRepository(Competence::class)->find($id);
    }

    /** @return array<int, Competence> */
    public function search(?string $keyword, ?string $field): array
    {
        $qb = $this->em->createQueryBuilder();
        $qb->select('c')->from(Competence::class, 'c');

        $allowedFields = ['type', 'category', 'niveau', 'statut', 'email', 'certification'];

        if ($keyword && $field && in_array($field, $allowedFields, true)) {
            $qb->andWhere("c.$field LIKE :k")->setParameter('k', "%$keyword%");
        } elseif ($keyword) {
            $qb->andWhere(
                'c.category LIKE :k OR c.type LIKE :k OR c.description LIKE :k OR
                 c.email LIKE :k OR c.niveau LIKE :k OR c.statut LIKE :k OR
                 c.certification LIKE :k'
            )->setParameter('k', "%$keyword%");
        }

        return $qb->getQuery()->getResult();
    }
}