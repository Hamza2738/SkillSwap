<?php

namespace App\Controller\competence;

use App\Entity\competence\Competence;

use App\Form\CompetenceTypebackback;
use App\Service\competence\CompetenceService;
use App\Service\competence\HistoriqueCompetenceService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use App\Form\CompetenceTypeback;
use App\Service\competence\MailService;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Dompdf\Dompdf;
use App\Service\GeminiService;
use App\Repository\CompetenceRepository;

use Symfony\Component\HttpFoundation\JsonResponse;

use Dompdf\Options;


#[Route('/admin/competence')]
class CompetenceController extends AbstractController
{
    private CompetenceService $service;
    private HistoriqueCompetenceService $historiqueService;
    private MailerInterface $mailer;

    public function __construct(
        CompetenceService $service,
        HistoriqueCompetenceService $historiqueService,
        MailerInterface $mailer
    ) {
        $this->service = $service;
        $this->historiqueService = $historiqueService;
        $this->mailer = $mailer;
    }

  #[Route('', name: 'competence_list', methods: ['GET'])]
public function index(): Response
{
    $competences = $this->service->getAll();
    $form = $this->createForm(CompetenceTypeback::class, new Competence());

    return $this->render('Competences/back/competence.html.twig', [
        'competences' => $competences,
        'form'        => $form->createView(),
        'formAction'  => $this->generateUrl('competence_add'),
        'selectedId'  => null,
        'stats'       => $this->buildStats($competences),
    ]);
}

    #[Route('/add', name: 'competence_add', methods: ['POST'])]
    public function add(Request $request): Response
    {
        $competence = new Competence();
        $form = $this->createForm(CompetenceTypeback::class, $competence);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $error = $this->validateMetier($competence);
            if ($error) {
                $this->addFlash('error', $error);
            } else {
                $this->service->add($competence);
                $this->historiqueService->logAction($competence, 'Ajoutée');
                $this->addFlash('success', 'Compétence ajoutée avec succès.');
                return $this->redirectToRoute('competence_list');
            }
        }

        return $this->render('Competences/back/competence.html.twig', [
            'competences' => $this->service->getAll(),
            'form'        => $form->createView(),
            'formAction'  => $this->generateUrl('competence_add'),
            'selectedId'  => null,
        ]);
    }

   #[Route('/edit/{id}', name: 'competence_edit', methods: ['POST'])]
public function edit(
    int $id,
    Request $request,
    CompetenceService $competenceService,
    MailService $mailService
): Response {
    $competence = $competenceService->find($id);

    if (!$competence) {
        $this->addFlash('error', 'Compétence introuvable.');
        return $this->redirectToRoute('competence_list');
    }

    // copie avant modification
    $before = clone $competence;

    $form = $this->createForm(CompetenceTypeback::class, $competence);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        try {
            $competenceService->update($competence);

            // envoi automatique si statut devient Validée ou Expirée
            $this->handleStatusMail($mailService, $before, $competence);

            $this->addFlash('success', 'Compétence modifiée avec succès.');
        } catch (\Throwable $e) {
            $this->addFlash('error', 'Erreur modification : ' . $e->getMessage());
        }

        return $this->redirectToRoute('competence_list');
    }

    $competences = $competenceService->getAll();

    return $this->render('Competences/back/competence.html.twig', [
        'competences' => $competences,
        'form' => $form->createView(),
        'formAction' => $this->generateUrl('competence_edit', ['id' => $id]),
        'selectedId' => $id,
    ]);
}

    #[Route('/delete/{id}', name: 'competence_delete', methods: ['POST'])]
    public function delete(int $id): Response
    {
        $competence = $this->service->find($id);

        if ($competence) {
            $this->historiqueService->logAction($competence, 'Supprimée');
            $this->service->delete($id);
            $this->addFlash('success', 'Compétence supprimée.');
        } else {
            $this->addFlash('error', 'Compétence introuvable.');
        }

        return $this->redirectToRoute('competence_list');
    }

    #[Route('/search', name: 'competence_search', methods: ['GET'])]
    public function search(Request $request): Response
    {
        $keyword = $request->get('q');
        $field   = $request->get('type');
        $results = $this->service->search($keyword, $field);

        $form = $this->createForm(CompetenceTypeback::class, new Competence());

        return $this->render('Competences/back/competence.html.twig', [
            'competences' => $results,
            'form'        => $form->createView(),
            'formAction'  => $this->generateUrl('competence_add'),
            'selectedId'  => null,
        ]);
    }

    #[Route('/export-pdf', name: 'competence_export_pdf', methods: ['GET'])]
    public function exportPdf(): Response
    {
        $competences = $this->service->getAll();

        $validees = array_filter($competences, function (Competence $c) {
            $statut = $this->normalizeStr($c->getStatut() ?? '');
            return $statut === 'valide' || $statut === 'validee';
        });

        $logoPath = $this->getParameter('kernel.project_dir');
        assert(is_string($logoPath));
        $logoContent = file_exists($logoPath . '/public/images/competences/logo.png')
            ? file_get_contents($logoPath . '/public/images/competences/logo.png')
            : false;
        $logoBase64 = $logoContent !== false
            ? 'data:image/png;base64,' . base64_encode($logoContent)
            : '';

        $signaturePath = $logoPath . '/public/images/competences/signature.png';
        $signatureContent = file_exists($signaturePath) ? file_get_contents($signaturePath) : false;
        $signatureBase64 = $signatureContent !== false
            ? 'data:image/png;base64,' . base64_encode($signatureContent)
            : '';

        $rows = '';
        foreach ($validees as $c) {
            $rows .= sprintf(
                '<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>',
                htmlspecialchars($c->getEmail() ?? '—'),
                htmlspecialchars($c->getNiveau() ?? '—'),
                htmlspecialchars($c->getType() ?? '—'),
                htmlspecialchars($c->getCertification() ?? '—'),
                htmlspecialchars($c->getDescription() ?? '—')
            );
        }

        $html = "
        <html><head><meta charset='UTF-8'>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12px; border: 2px solid #000; padding: 20px; }
            .header{ display:flex; align-items:center; gap:16px; margin-bottom:20px; }
            .header-text h1 { font-size:22px; margin:0; }
            .header-text p { font-size:12px; margin:0; }
            h2 { color:#6c63ff; text-align:center; margin-bottom:16px; }
            table { width:100%; border-collapse:collapse; }
            th { background:#6c63ff; color:#fff; padding:8px; text-align:left; }
            td { padding:6px 8px; border-bottom:1px solid #ddd; }
            tr:nth-child(even){ background:#f5f5f5; }
        </style>
        </head><body>
        <div class='header'>
            " . ($logoBase64 ? "<img src='{$logoBase64}' style='height:50px'>" : "") . "
            <div class='header-text'>
                <h1>SkillSwap</h1>
                <p>Liste des Compétences</p>
            </div>
        </div>
        <h2>Compétences Validées</h2>
        <table>
            <thead>
                <tr>
                    <th>Email</th><th>Niveau</th><th>Type</th><th>Certification</th><th>Description</th>
                </tr>
            </thead>
            <tbody>{$rows}</tbody>
        </table>
        " . ($signatureBase64 ? "<div style='text-align:right;margin-top:20px'><img src='{$signatureBase64}' style='max-height:80px;max-width:160px'></div>" : "") . "
        </body></html>";

        $options = new Options();
        $options->set('defaultFont', 'Arial');
        $options->set('isRemoteEnabled', false);

        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'landscape');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type'        => 'application/pdf',
                'Content-Disposition' => 'attachment; filename=\"competences_validees_' . date('Y-m-d') . '.pdf\"',
            ]
        );
    }

   

    #[Route('/dashboard', name: 'competence_dashboard', methods: ['GET'])]
    public function dashboard(): Response
    {
        $competences = $this->service->getAll();

        $stats = [
            'total'      => count($competences),
            'parNiveau'  => [],
            'parStatut'  => [],
            'parType'    => [],
            'moyenneAns' => 0,
        ];

        $totalAns = 0;
        foreach ($competences as $c) {
            $niveau = $c->getNiveau() ?? 'Inconnu';
            $stats['parNiveau'][$niveau] = ($stats['parNiveau'][$niveau] ?? 0) + 1;

            $statut = $c->getStatut() ?? 'Inconnu';
            $stats['parStatut'][$statut] = ($stats['parStatut'][$statut] ?? 0) + 1;

            $type = $c->getType() ?? 'Inconnu';
            $stats['parType'][$type] = ($stats['parType'][$type] ?? 0) + 1;

            $totalAns += $c->getAnneesExperience() ?? 0;
        }

        if ($stats['total'] > 0) {
            $stats['moyenneAns'] = round($totalAns / $stats['total'], 1);
        }

        arsort($stats['parType']);
        arsort($stats['parNiveau']);
        arsort($stats['parStatut']);

        return $this->render('Competences/back/dashboard.html.twig', [
            'competences' => $competences,
            'stats'       => $stats,
        ]);
    }
   #[Route('/chatbot/send', name: 'competence_chatbot_send', methods: ['POST'])]
public function chatbot(
    Request $request,
    CompetenceRepository $repo,
    GeminiService $ai
): JsonResponse {
    $data = json_decode($request->getContent(), true);
    $message = trim((string)($data['message'] ?? ''));

    if ($message === '') {
        return new JsonResponse(['reply' => 'Message vide.'], 400);
    }

    $competences = $repo->findBy([], null, 20);

    $profiles = array_map(static function ($c) {
        return [
            'email' => $c->getEmail(),
            'type' => $c->getType(),
            'category' => $c->getCategory(),
            'niveau' => $c->getNiveau(),
            'statut' => $c->getStatut(),
            'description' => $c->getDescription(),
            'certification' => $c->getCertification(),
            'anneesExperience' => $c->getAnneesExperience(),
        ];
    }, $competences);

    $reply = $ai->ask($message, $profiles);

    return new JsonResponse(['reply' => $reply]);
}
    #[Route('/historique', name: 'competence_historique_redirect', methods: ['GET'])]
    public function historique(): Response
    {
        return $this->redirectToRoute('competence_historique');
    }

  #[Route('/send-mail/{id}', name: 'competence_send_mail', methods: ['POST'])]
public function sendMail(Request $request, int $id): Response
{
    $competence = $this->service->find($id);

    if (!$competence) {
        if ($request->isXmlHttpRequest()) {
            return new JsonResponse(['message' => 'Compétence introuvable.'], 404);
        }

        $this->addFlash('error', 'Compétence introuvable.');
        return $this->redirectToRoute('competence_list');
    }

    $result = $this->sendStatusMailIfNeeded($competence);

    if ($request->isXmlHttpRequest()) {
        if ($result) {
            return new JsonResponse(['message' => $result]);
        }

        return new JsonResponse(['message' => 'Aucun mail envoyé pour ce statut.'], 400);
    }

    if ($result) {
        $this->addFlash('success', $result);
    } else {
        $this->addFlash('error', 'Aucun mail envoyé pour ce statut.');
    }

    return $this->redirectToRoute('competence_list');
}

    private function validateMetier(Competence $c): ?string
    {
        $category = trim($c->getCategory() ?? '');
        $type     = trim($c->getType() ?? '');
        $desc     = trim($c->getDescription() ?? '');
        $cert     = trim($c->getCertification() ?? '');
        $annees   = $c->getAnneesExperience() ?? 0;

        $vides = [];
        if (!$category) $vides[] = 'Catégorie';
        if (!$type)     $vides[] = 'Type';
        if (!$desc)     $vides[] = 'Description';

        if (count($vides) === 3) return 'Complétez les données.';
        if ($vides) return 'Complétez les données (' . implode(', ', $vides) . ').';

        if (strlen($category) > 80) return 'Catégorie trop longue (max 80).';
        if (strlen($type) > 80) return 'Type trop long (max 80).';
        if (strlen($desc) > 500) return 'Description trop longue (max 500).';
        if (strlen($cert) > 100) return 'Certification trop longue (max 100).';
        if ($annees < 0 || $annees > 60) return 'Années d\'expérience invalides (0..60).';

        if (!preg_match('/^[\p{L}0-9 _-]*$/u', $category)) return 'Catégorie : caractères interdits.';
        if (!preg_match('/^[\p{L}0-9 _-]*$/u', $type)) return 'Type : caractères interdits.';

        return null;
    }
private function handleStatusMail(MailService $mailService, Competence $before, Competence $after): void
{
    $oldStatus = $this->normalizeStatus((string) $before->getStatut());
    $newStatus = $this->normalizeStatus((string) $after->getStatut());

    // envoyer seulement si le statut a changé
    if ($oldStatus === $newStatus) {
        return;
    }

    $email = trim((string) $after->getEmail());
    if ($email === '' || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
        return;
    }

    $teamType = trim((string) $after->getType());
    if ($teamType === '') {
        $teamType = 'votre domaine';
    }

    if ($newStatus === 'validee') {
        $mailService->sendSelectionEmail($email, $teamType);
    } elseif ($newStatus === 'expiree') {
        $mailService->sendExpiredEmail($email, $teamType);
    }
}

private function normalizeStatus(string $value): string
{
    $value = trim(mb_strtolower($value, 'UTF-8'));
    $value = str_replace(['é', 'è', 'ê', 'ë'], 'e', $value);
    return $value;
}
    private function sendStatusMailIfNeeded(Competence $competence): ?string
    {
        $statut = $this->normalizeStatus((string) $competence->getStatut());
        $email  = trim($competence->getEmail() ?? '');
        $type   = trim($competence->getType() ?? '') ?: 'votre domaine';

        $isValidee = $statut === 'valide' || $statut === 'validee';
        $isExpiree = $statut === 'expiree' || $statut === 'expire';

        if (!$isValidee && !$isExpiree) return null;
        if (!$email || !filter_var($email, FILTER_VALIDATE_EMAIL)) return null;

        try {
            if ($isValidee) {
                $mail = (new Email())
                    ->from('noreply@skillswap.com')
                    ->to($email)
                    ->subject('SkillSwap — Votre compétence a été validée !')
                    ->html("
                        <h2>Félicitations !</h2>
                        <p>Votre compétence en <strong>{$type}</strong> a été <strong>validée</strong> sur SkillSwap.</p>
                        <p>Vous êtes maintenant sélectionné(e) pour ce domaine.</p>
                        <br><p>— L'équipe SkillSwap</p>
                    ");
                $this->mailer->send($mail);
                return "✅ Email de sélection envoyé à {$email}";
            }

            $mail = (new Email())
                ->from('noreply@skillswap.com')
                ->to($email)
                ->subject('SkillSwap — Votre compétence a expiré')
                ->html("
                    <h2>Notification d'expiration</h2>
                    <p>Votre compétence en <strong>{$type}</strong> a expiré sur SkillSwap.</p>
                    <p>Veuillez mettre à jour votre profil pour rester actif.</p>
                    <br><p>— L'équipe SkillSwap</p>
                ");
            $this->mailer->send($mail);
            return "✅ Email d'expiration envoyé à {$email}";
        } catch (\Exception $e) {
            return null;
        }
    }
    /**
     * @param array<int, Competence> $competences
     * @return array<string, mixed>
     */
    private function buildStats(array $competences): array
{
    $stats = [
        'total' => count($competences),
        'parType' => [],
        'moyenneAns' => 0,
    ];

    $totalAns = 0;

    foreach ($competences as $c) {
        $type = trim((string) ($c->getType() ?? ''));
        if ($type === '') {
            $type = 'Inconnu';
        }

        $stats['parType'][$type] = ($stats['parType'][$type] ?? 0) + 1;
        $totalAns += (int) ($c->getAnneesExperience() ?? 0);
    }

    if ($stats['total'] > 0) {
        $stats['moyenneAns'] = round($totalAns / $stats['total'], 1);
    }

    arsort($stats['parType']);

    return $stats;
}

  private function normalizeStr(string $s): string
{
    $s = trim(mb_strtolower($s, 'UTF-8'));

    $replace = [
        'à' => 'a', 'á' => 'a', 'â' => 'a', 'ã' => 'a', 'ä' => 'a',
        'ç' => 'c',
        'è' => 'e', 'é' => 'e', 'ê' => 'e', 'ë' => 'e',
        'ì' => 'i', 'í' => 'i', 'î' => 'i', 'ï' => 'i',
        'ñ' => 'n',
        'ò' => 'o', 'ó' => 'o', 'ô' => 'o', 'õ' => 'o', 'ö' => 'o',
        'ù' => 'u', 'ú' => 'u', 'û' => 'u', 'ü' => 'u',
        'ý' => 'y', 'ÿ' => 'y',
    ];

    return strtr($s, $replace);
}
}