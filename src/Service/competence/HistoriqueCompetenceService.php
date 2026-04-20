<?php

namespace App\Service\competence;

use App\Entity\competence\Competence;
use App\Entity\competence\HistoriqueCompetence;
use App\Repository\competence\HistoriqueCompetenceRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Response;

class HistoriqueCompetenceService
{
    public function __construct(
        private EntityManagerInterface $em,
        private HistoriqueCompetenceRepository $repo
    ) {
    }

    public function logAction(Competence $c, string $action): void
    {
        $h = new HistoriqueCompetence();
        $h->setCompetenceId($action === 'Supprimée' ? null : $c->getId());
        $h->setCategory($c->getCategory());
        $h->setType($c->getType());
        $h->setDescription($c->getDescription());
        $h->setNiveau($c->getNiveau());
        $h->setAnneesExperience((int) ($c->getAnneesExperience() ?? 0));
        $h->setCertification($c->getCertification());
        $h->setStatut($c->getStatut());
        $h->setEmail($c->getEmail());
        $h->setStatusChange($action);
        $h->setDateModification(new \DateTime());

        $this->em->persist($h);
        $this->em->flush();
    }

    public function findAll(): array
    {
        return $this->repo->findBy([], [
            'dateModification' => 'DESC',
            'id' => 'DESC',
        ]);
    }

    public function deleteMany(array $ids): void
    {
        if (empty($ids)) {
            return;
        }

        $ids = array_filter(array_map('intval', $ids));

        if (empty($ids)) {
            return;
        }

        $items = $this->repo->findBy(['id' => $ids]);

        foreach ($items as $item) {
            $this->em->remove($item);
        }

        $this->em->flush();
    }

    public function exportPdfResponse(): Response
    {
        $items = $this->findAll();

        $html = '
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Historique des compétences</title>
            <style>
                body {
                    font-family: DejaVu Sans, Arial, sans-serif;
                    font-size: 12px;
                    color: #111;
                    padding: 20px;
                }
                h1 {
                    margin-bottom: 18px;
                    font-size: 22px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
                th, td {
                    border: 1px solid #999;
                    padding: 6px;
                    text-align: left;
                    vertical-align: top;
                }
                th {
                    background: #f0f0f0;
                }
            </style>
        </head>
        <body>
            <h1>Historique des compétences</h1>
            <table>
                <thead>
                    <tr>
                        <th>Action</th>
                        <th>Date</th>
                        <th>Email</th>
                        <th>Catégorie</th>
                        <th>Type</th>
                        <th>Niveau</th>
                        <th>Exp.</th>
                        <th>Statut</th>
                    </tr>
                </thead>
                <tbody>
        ';

        foreach ($items as $h) {
            $html .= sprintf(
                '<tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>',
                htmlspecialchars((string) ($h->getStatusChange() ?? '')),
                htmlspecialchars($h->getDateModification()?->format('d/m/Y H:i') ?? ''),
                htmlspecialchars((string) ($h->getEmail() ?? '')),
                htmlspecialchars((string) ($h->getCategory() ?? '')),
                htmlspecialchars((string) ($h->getType() ?? '')),
                htmlspecialchars((string) ($h->getNiveau() ?? '')),
                htmlspecialchars((string) ($h->getAnneesExperience() ?? '')),
                htmlspecialchars((string) ($h->getStatut() ?? ''))
            );
        }

        $html .= '
                </tbody>
            </table>
        </body>
        </html>';

        return new Response(
            $html,
            200,
            [
                'Content-Type' => 'text/html; charset=UTF-8',
                'Content-Disposition' => 'inline; filename="historique_competences.html"',
            ]
        );
    }
}