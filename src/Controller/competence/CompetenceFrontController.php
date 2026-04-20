<?php

namespace App\Controller\competence;

use App\Entity\competence\Competence;
use App\Entity\utilisateur\Utilisateur;
use App\Form\CompetenceType;
use App\Service\competence\CompetenceService;
use App\Service\GeminiService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;
use Dompdf\Dompdf;
use Dompdf\Options;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\HttpFoundation\ResponseHeaderBag;
use Symfony\Component\Filesystem\Filesystem;

#[Route('/front/competences', name: 'competence_front_')]
class CompetenceFrontController extends AbstractController
{
    public function __construct(
        private CompetenceService $competenceService
    ) {}

    private function getUserEmail(): string
    {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user) {
            throw $this->createAccessDeniedException('Vous devez être connecté');
        }

        return (string) $user->getEmail();
    }

    #[Route('/', name: 'index', methods: ['GET', 'POST'])]
    public function index(Request $request): Response
    {
        $email = $this->getUserEmail();

        $allCompetences = $this->competenceService->getAll();
        $competences = array_values(array_filter(
            $allCompetences,
            fn (Competence $c) => $c->getEmail() === $email
        ));

        $newCompetence = new Competence();
        $form = $this->createForm(CompetenceType::class, $newCompetence);
        $form->handleRequest($request);

        $error = '';

        if ($form->isSubmitted() && $form->isValid()) {
            $newCompetence->setEmail($email);
            $newCompetence->setStatut('En cours');

            try {
                $this->competenceService->add($newCompetence);
                $this->addFlash('success', 'Compétence ajoutée avec succès.');
                return $this->redirectToRoute('competence_front_index');
            } catch (\Throwable $e) {
                $error = $e->getMessage();
            }
        }

        return $this->render('Competences/front/CompetenceFront.html.twig', [
            'competences' => $competences,
            'form'        => $form->createView(),
            'error'       => $error,
            'email'       => $email,
            'editing'     => null,
        ]);
    }

    #[Route('/edit/{id}', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(int $id, Request $request): Response
    {
        $email = $this->getUserEmail();
        $competence = $this->competenceService->find($id);

        if (!$competence || $competence->getEmail() !== $email) {
            $this->addFlash('error', 'Compétence introuvable ou accès refusé.');
            return $this->redirectToRoute('competence_front_index');
        }

        $form = $this->createForm(CompetenceType::class, $competence);
        $form->handleRequest($request);

        $error = '';

        if ($form->isSubmitted() && $form->isValid()) {
            $competence->setEmail($email);
            $competence->setStatut('En cours');

            try {
                $this->competenceService->update($competence);
                $this->addFlash('success', 'Compétence modifiée.');
                return $this->redirectToRoute('competence_front_index');
            } catch (\Throwable $e) {
                $error = $e->getMessage();
            }
        }

        $allCompetences = $this->competenceService->getAll();
        $competences = array_values(array_filter(
            $allCompetences,
            fn (Competence $c) => $c->getEmail() === $email
        ));

        return $this->render('Competences/front/CompetenceFront.html.twig', [
            'competences' => $competences,
            'form'        => $form->createView(),
            'editing'     => $competence,
            'error'       => $error,
            'email'       => $email,
        ]);
    }

    #[Route('/delete/{id}', name: 'delete', methods: ['POST'])]
    public function delete(int $id): Response
    {
        $email = $this->getUserEmail();
        $competence = $this->competenceService->find($id);

        if ($competence && $competence->getEmail() === $email) {
            $this->competenceService->delete($id);
            $this->addFlash('success', 'Compétence supprimée.');
        } else {
            $this->addFlash('error', 'Suppression refusée.');
        }

        return $this->redirectToRoute('competence_front_index');
    }
    #[Route('/chatbot/send', name: 'chatbot_send', methods: ['POST'])]
public function chatbotSend(Request $request, GeminiService $ai): JsonResponse
{
    $email = $this->getUserEmail();

    $data = json_decode($request->getContent(), true);
    $message = trim((string)($data['message'] ?? ''));

    if ($message === '') {
        return new JsonResponse([
    'reply' => "J’ai préparé votre CV professionnel. Téléchargez-le ici : /front/competences/cv/pdf"
]);
    }
 $normalizedMessage = mb_strtolower($message, 'UTF-8');

$isCvRequest =
    str_contains($normalizedMessage, 'cv') &&
    (
        str_contains($normalizedMessage, 'prépare') ||
        str_contains($normalizedMessage, 'prepare') ||
        str_contains($normalizedMessage, 'génère') ||
        str_contains($normalizedMessage, 'genere') ||
        str_contains($normalizedMessage, 'crée') ||
        str_contains($normalizedMessage, 'cree') ||
        str_contains($normalizedMessage, 'fais') ||
        str_contains($normalizedMessage, 'fait') ||
        str_contains($normalizedMessage, 'donne') ||
        str_contains($normalizedMessage, 'donnee') ||
        str_contains($normalizedMessage, 'moi') ||
        str_contains($normalizedMessage, 'créer') ||
        str_contains($normalizedMessage, 'faire')
    );

if ($isCvRequest) {
    return new JsonResponse([
        'reply' => 'C’est prêt : /front/competences/cv/pdf'
    ]);
}


    $allCompetences = $this->competenceService->getAll();
    $competences = array_values(array_filter(
        $allCompetences,
        fn (Competence $c) => $c->getEmail() === $email
    ));

    $profiles = array_map(static function (Competence $c) {
        return [
            'email'            => $c->getEmail(),
            'type'             => $c->getType(),
            'category'         => $c->getCategory(),
            'niveau'           => $c->getNiveau(),
            'statut'           => $c->getStatut(),
            'description'      => $c->getDescription(),
            'certification'    => $c->getCertification(),
            'anneesExperience' => $c->getAnneesExperience(),
        ];
    }, $competences);
    $aiText = $ai->ask(
    "À partir de ces compétences, rédige un titre professionnel court et un résumé CV très professionnel en français. Réponse JSON avec title et summary.",
    $profiles
);

    if (!$profiles) {
        return new JsonResponse([
            'reply' => "Ajoutez quelques compétences et je pourrai vous proposer une idée utile."
        ]);
    }

   $enhancedPrompt = <<<PROMPT
Tu es l’assistant SkillSwap.

Tu dois parler simplement, naturellement et comme une vraie personne.

Règles :
- réponse courte
- maximum 2 phrases
- pas de long paragraphe
- pas de liste
- pas de style robot
- pas de rapport
- si l'utilisateur dit "oui" ou "donne-moi", donne juste une idée simple et directe pour améliorer son profil
- parle comme un ami qui conseille bien

Message utilisateur :
{$message}
PROMPT;

try {
    $reply = $ai->ask($enhancedPrompt, $profiles);
    $reply = trim((string) $reply);

    if ($reply === '') {
        $reply = "Je peux vous proposer une idée utile si vous voulez.";
    }

    return new JsonResponse([
        'reply' => $reply
    ]);
} catch (\Throwable $e) {
    $msg = mb_strtolower($e->getMessage(), 'UTF-8');

    if (
        str_contains($msg, 'quota') ||
        str_contains($msg, 'rate limit') ||
        str_contains($msg, 'exceeded') ||
        str_contains($msg, 'resource exhausted')
    ) {
        return new JsonResponse([
            'reply' => "Je suis un peu occupé pour le moment. Réessayez dans quelques instants."
        ], 200);
    }

    return new JsonResponse([
        'reply' => "Je n’arrive pas à répondre pour le moment. Réessayez un peu plus tard."
    ], 200);
}
}

#[Route('/cv/pdf', name: 'cv_pdf', methods: ['GET'])]
public function generateCvPdf(): Response
{
    $email = $this->getUserEmail();

    $allCompetences = $this->competenceService->getAll();
    $competences = array_values(array_filter(
        $allCompetences,
        fn (Competence $c) => $c->getEmail() === $email
    ));

    if (!$competences) {
        return new Response('Aucune compétence trouvée pour générer le CV.', 404);
    }

    $nom = 'Utilisateur';
    $prenom = 'SkillSwap';

    /** @var Utilisateur|null $user */
    $user = $this->getUser();
    if ($user) {
        if (method_exists($user, 'getNom') && $user->getNom()) {
            $nom = $user->getNom();
        }
        if (method_exists($user, 'getPrenom') && $user->getPrenom()) {
            $prenom = $user->getPrenom();
        }
    }

    $titre = 'Profil professionnel';
    $resume = 'Profil orienté compétences techniques et évolution professionnelle.';

    $competenceLines = '';
    foreach ($competences as $c) {
        $type = htmlspecialchars($c->getType() ?? '—');
        $category = htmlspecialchars($c->getCategory() ?? '—');
        $niveau = htmlspecialchars($c->getNiveau() ?? '—');
        $exp = htmlspecialchars((string)($c->getAnneesExperience() ?? '0'));
        $certif = htmlspecialchars($c->getCertification() ?: 'Non spécifiée');
        $description = htmlspecialchars($c->getDescription() ?: 'Aucune description');

        $competenceLines .= "
            <div class='item'>
                <div class='item-head'>
                    <span class='item-title'>{$type}</span>
                    <span class='item-level'>{$niveau}</span>
                </div>
                <div class='item-sub'>{$category} • {$exp} an(s) d’expérience</div>
                <div class='item-desc'>{$description}</div>
                <div class='item-cert'><strong>Certification :</strong> {$certif}</div>
            </div>
        ";
    }

    $html = "
    <html>
    <head>
        <meta charset='UTF-8'>
        <style>
            body {
                font-family: DejaVu Sans, Arial, sans-serif;
                color: #1b1f2a;
                margin: 0;
                padding: 0;
                font-size: 12px;
            }
            .page {
                padding: 34px;
            }
            .header {
                border-bottom: 3px solid #4f46e5;
                padding-bottom: 18px;
                margin-bottom: 24px;
            }
            .name {
                font-size: 28px;
                font-weight: bold;
                color: #111827;
                margin-bottom: 6px;
            }
            .title {
                font-size: 15px;
                color: #4f46e5;
                font-weight: bold;
                margin-bottom: 10px;
            }
            .contact {
                font-size: 12px;
                color: #374151;
            }
            .section {
                margin-top: 22px;
            }
            .section h2 {
                font-size: 15px;
                color: #111827;
                border-left: 4px solid #4f46e5;
                padding-left: 10px;
                margin-bottom: 12px;
            }
            .resume {
                line-height: 1.6;
                color: #374151;
            }
            .item {
                border: 1px solid #e5e7eb;
                border-radius: 10px;
                padding: 12px;
                margin-bottom: 12px;
                background: #f9fafb;
            }
            .item-head {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 6px;
            }
            .item-title {
                font-size: 14px;
                font-weight: bold;
                color: #111827;
            }
            .item-level {
                font-size: 11px;
                color: white;
                background: #4f46e5;
                padding: 4px 8px;
                border-radius: 999px;
            }
            .item-sub {
                font-size: 11px;
                color: #6b7280;
                margin-bottom: 6px;
            }
            .item-desc {
                font-size: 12px;
                line-height: 1.5;
                color: #374151;
                margin-bottom: 6px;
            }
            .item-cert {
                font-size: 11px;
                color: #1f2937;
            }
        </style>
    </head>
    <body>
        <div class='page'>
            <div class='header'>
                <div class='name'>{$prenom} {$nom}</div>
                <div class='title'>{$titre}</div>
                <div class='contact'>{$email}</div>
            </div>

            <div class='section'>
                <h2>Profil</h2>
                <div class='resume'>{$resume}</div>
            </div>

            <div class='section'>
                <h2>Compétences</h2>
                {$competenceLines}
            </div>
        </div>
    </body>
    </html>
    ";

    $options = new Options();
    $options->set('defaultFont', 'DejaVu Sans');
    $options->set('isRemoteEnabled', false);

    $dompdf = new Dompdf($options);
    $dompdf->loadHtml($html);
    $dompdf->setPaper('A4', 'portrait');
    $dompdf->render();

    $filename = 'cv_professionnel_' . date('Y-m-d_H-i-s') . '.pdf';

    return new Response(
        $dompdf->output(),
        200,
        [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="' . $filename . '"',
        ]
    );
}






}