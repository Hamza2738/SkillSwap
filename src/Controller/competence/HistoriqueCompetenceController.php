<?php

namespace App\Controller\competence;

use App\Service\competence\HistoriqueCompetenceService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/competence/historique')]
class HistoriqueCompetenceController extends AbstractController
{
    public function __construct(
        private HistoriqueCompetenceService $historiqueService
    ) {
    }

    #[Route('/data', name: 'competence_historique_data', methods: ['GET'])]
    public function data(): JsonResponse
    {
        $items = $this->historiqueService->findAll();

        $rows = array_map(function ($h) {
            return [
                'id' => $h->getId(),
                'competenceId' => $h->getCompetenceId(),
                'category' => $h->getCategory(),
                'type' => $h->getType(),
                'description' => $h->getDescription(),
                'niveau' => $h->getNiveau(),
                'anneesExperience' => $h->getAnneesExperience(),
                'certification' => $h->getCertification(),
                'statut' => $h->getStatut(),
                'email' => $h->getEmail(),
                'statusChange' => $h->getStatusChange(),
                'dateModification' => $h->getDateModification()?->format('Y-m-d H:i:s'),
            ];
        }, $items);

        return new JsonResponse(['items' => $rows]);
    }

    #[Route('/delete-multiple', name: 'competence_historique_delete_multiple', methods: ['POST'])]
    public function deleteMultiple(Request $request): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $ids = $data['ids'] ?? [];

        if (!is_array($ids) || empty($ids)) {
            return new JsonResponse(['message' => 'Aucune ligne sélectionnée.'], 400);
        }

        $this->historiqueService->deleteMany($ids);

        return new JsonResponse(['message' => 'Suppression effectuée.']);
    }

    #[Route('/export-pdf', name: 'competence_historique_export_pdf', methods: ['GET'])]
    public function exportPdf(): Response
    {
        return $this->historiqueService->exportPdfResponse();
    }
}