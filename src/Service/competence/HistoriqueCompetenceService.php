<?php

namespace App\Service\competence;

use App\Entity\competence\Competence;
use App\Entity\competence\HistoriqueCompetence;
use Doctrine\ORM\EntityManagerInterface;

class HistoriqueCompetenceService
{
    private EntityManagerInterface $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    public function getAll(): array
    {
        return $this->em->getRepository(HistoriqueCompetence::class)
            ->findBy([], ['dateModification' => 'DESC', 'id' => 'DESC']);
    }

    public function getByCompetenceId(int $competenceId): array
    {
        return $this->em->getRepository(HistoriqueCompetence::class)
            ->findBy(
                ['competenceId' => $competenceId],
                ['dateModification' => 'DESC', 'id' => 'DESC']
            );
    }

    public function add(HistoriqueCompetence $h): void
    {
        $h->setDateModification(new \DateTime());
        $this->em->persist($h);
        $this->em->flush();
    }

    public function update(HistoriqueCompetence $h): void
    {
        $h->setDateModification(new \DateTime());
        $this->em->flush();
    }

    public function delete(int $id): void
    {
        $h = $this->em->getRepository(HistoriqueCompetence::class)->find($id);
        if ($h) {
            $this->em->remove($h);
            $this->em->flush();
        }
    }

    // ✅ FIX: un seul flush pour tous les ids au lieu de N flush
    public function deleteMultiple(array $ids): void
    {
        foreach ($ids as $id) {
            $h = $this->em->getRepository(HistoriqueCompetence::class)->find((int) $id);
            if ($h) {
                $this->em->remove($h);
                // ❌ Ancien : $this->delete((int) $id) → flush à chaque tour
            }
        }
        $this->em->flush(); // ✅ Un seul flush pour tout
    }

    // ✅ FIX: competence_id toujours rempli + pas de double flush
    public function logAction(Competence $c, string $action): void
    {
        $h = new HistoriqueCompetence();

        // ✅ On garde toujours l'ID même pour Supprimée
        // (l'ID existe encore en mémoire même après remove())
        $h->setCompetenceId($c->getId());

        $h->setCategory($c->getCategory());
        $h->setType($c->getType());
        $h->setDescription($c->getDescription());
        $h->setNiveau($c->getNiveau());
        $h->setAnneesExperience($c->getAnneesExperience());
        $h->setCertification($c->getCertification());
        $h->setStatut($c->getStatut());
        $h->setEmail($c->getEmail());
        $h->setStatusChange($action);
        $h->setDateModification(new \DateTime());

        $this->em->persist($h);
        $this->em->flush();
    }

    public function search(?string $keyword, ?string $attr, ?int $month, ?int $year): array
    {
        $qb = $this->em->createQueryBuilder();
        $qb->select('h')
           ->from(HistoriqueCompetence::class, 'h')
           ->orderBy('h.dateModification', 'DESC');

        if ($month && $year) {
            $qb->andWhere('MONTH(h.dateModification) = :month')
               ->andWhere('YEAR(h.dateModification) = :year')
               ->setParameter('month', $month)
               ->setParameter('year', $year);
        }

        if ($keyword && $attr) {
            $like = '%' . $keyword . '%';
            switch ($attr) {
                case 'Action':
                    $qb->andWhere('h.statusChange LIKE :k')->setParameter('k', $like);
                    break;
                case 'Email':
                    $qb->andWhere('h.email LIKE :k')->setParameter('k', $like);
                    break;
                case 'Type':
                    $qb->andWhere('h.type LIKE :k')->setParameter('k', $like);
                    break;
                case 'Catégorie':
                    $qb->andWhere('h.category LIKE :k')->setParameter('k', $like);
                    break;
                case 'Niveau':
                    $qb->andWhere('h.niveau LIKE :k')->setParameter('k', $like);
                    break;
                case 'Statut':
                    $qb->andWhere('h.statut LIKE :k')->setParameter('k', $like);
                    break;
                case 'Certification':
                    $qb->andWhere('h.certification LIKE :k')->setParameter('k', $like);
                    break;
                case 'Description':
                    $qb->andWhere('h.description LIKE :k')->setParameter('k', $like);
                    break;
                default:
                    $qb->andWhere(
                        $qb->expr()->orX(
                            $qb->expr()->like('h.statusChange', ':k'),
                            $qb->expr()->like('h.email',        ':k'),
                            $qb->expr()->like('h.type',         ':k'),
                            $qb->expr()->like('h.category',     ':k'),
                            $qb->expr()->like('h.niveau',       ':k'),
                            $qb->expr()->like('h.statut',       ':k'),
                            $qb->expr()->like('h.certification',':k'),
                            $qb->expr()->like('h.description',  ':k')
                        )
                    )->setParameter('k', $like);
            }
        }

        return $qb->getQuery()->getResult();
    }
}