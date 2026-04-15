<?php

namespace App\Controller\competence;

use App\Service\competence\HistoriqueCompetenceService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Dompdf\Dompdf;
use Dompdf\Options;

#[Route('/admin/competence/historique')]
class HistoriqueCompetenceController extends AbstractController
{
    private HistoriqueCompetenceService $service;

    public function __construct(HistoriqueCompetenceService $service)
    {
        $this->service = $service;
    }

    #[Route('', name: 'competence_historique', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $q     = $request->get('q');
        $attr  = $request->get('attr', 'Tous');
        $month = (int) $request->get('month', (int) date('n'));
        $year  = (int) $request->get('year',  (int) date('Y'));

        $historique = $this->service->search($q, $attr, $month, $year);
        $current    = new \DateTimeImmutable("$year-$month-01");
        $prev       = $current->modify('-1 month');
        $next       = $current->modify('+1 month');

        $months = [
            1=>'Janvier',2=>'Février',3=>'Mars',4=>'Avril',
            5=>'Mai',6=>'Juin',7=>'Juillet',8=>'Août',
            9=>'Septembre',10=>'Octobre',11=>'Novembre',12=>'Décembre',
        ];

        return $this->render('Competences/back/historique.html.twig', [
            'historique'        => $historique,
            'currentMonthLabel' => $months[$month],
            'currentYear'       => $year,
            'prevMonth'         => (int) $prev->format('n'),
            'prevYear'          => (int) $prev->format('Y'),
            'nextMonth'         => (int) $next->format('n'),
            'nextYear'          => (int) $next->format('Y'),
        ]);
    }

    #[Route('/delete-multiple', name: 'competence_historique_delete_multiple', methods: ['POST'])]
    public function deleteMultiple(Request $request): Response
    {
        $ids = $request->request->all('ids');
        if (empty($ids)) {
            $this->addFlash('error', 'Aucune ligne sélectionnée.');
            return $this->redirectToRoute('competence_historique');
        }
        $this->service->deleteMultiple($ids);
        $this->addFlash('success', count($ids) . ' ligne(s) supprimée(s).');
        return $this->redirectToRoute('competence_historique');
    }

    #[Route('/export-pdf', name: 'competence_historique_export_pdf', methods: ['GET'])]
    public function exportPdf(Request $request): Response
    {
        $month = (int) $request->get('month', (int) date('n'));
        $year  = (int) $request->get('year',  (int) date('Y'));
        $historique = $this->service->search(null, 'Tous', $month, $year);

        $months = [
            1=>'Janvier',2=>'Février',3=>'Mars',4=>'Avril',
            5=>'Mai',6=>'Juin',7=>'Juillet',8=>'Août',
            9=>'Septembre',10=>'Octobre',11=>'Novembre',12=>'Décembre',
        ];

        $html = '<html><head><meta charset="UTF-8">
        <style>
            body{font-family:Arial,sans-serif;font-size:11px;}
            h1{color:#6c63ff;text-align:center;margin-bottom:6px;}
            h2{color:#64748b;text-align:center;font-size:13px;margin-bottom:18px;}
            table{width:100%;border-collapse:collapse;}
            th{background:#6c63ff;color:white;padding:7px;text-align:left;font-size:10px;}
            td{padding:5px 7px;border-bottom:1px solid #e2e8f0;}
            tr:nth-child(even){background:#f8fafc;}
            .add{color:#22c55e;font-weight:bold;}
            .edit{color:#f59e0b;font-weight:bold;}
            .delete{color:#ef4444;font-weight:bold;}
        </style></head><body>
        <h1>SkillSwap — Historique des Compétences</h1>
        <h2>' . ($months[$month] ?? '') . ' ' . $year . '</h2>
        <table><thead><tr>
            <th>Action</th><th>Date</th><th>Email</th>
            <th>Type</th><th>Catégorie</th><th>Niveau</th>
            <th>Exp.</th><th>Statut</th><th>Certification</th>
        </tr></thead><tbody>';

        foreach ($historique as $h) {
            $action   = strtolower($h->getStatusChange() ?? '');
            $css      = str_contains($action, 'ajout') ? 'add'
                      : (str_contains($action, 'modif') ? 'edit' : 'delete');
            $date     = $h->getDateModification()
                        ? $h->getDateModification()->format('d/m/Y H:i') : '—';

            $html .= sprintf(
                '<tr><td class="%s">%s</td><td>%s</td><td>%s</td>
                      <td>%s</td><td>%s</td><td>%s</td>
                      <td>%s ans</td><td>%s</td><td>%s</td></tr>',
                $css,
                htmlspecialchars($h->getStatusChange() ?? ''),
                $date,
                htmlspecialchars($h->getEmail() ?? ''),
                htmlspecialchars($h->getType() ?? ''),
                htmlspecialchars($h->getCategory() ?? ''),
                htmlspecialchars($h->getNiveau() ?? ''),
                $h->getAnneesExperience() ?? 0,
                htmlspecialchars($h->getStatut() ?? ''),
                htmlspecialchars($h->getCertification() ?? '—')
            );
        }

        $html .= '</tbody></table></body></html>';

        $options = new Options();
        $options->set('defaultFont', 'Arial');
        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'landscape');
        $dompdf->render();

        return new Response($dompdf->output(), 200, [
            'Content-Type'        => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="historique_'.$year.'_'.$month.'.pdf"',
        ]);
    }
}