<?php

namespace App\Controller\competence;

use App\Entity\competence\Competence;
use App\Entity\utilisateur\Utilisateur;
use App\Form\CompetenceType;
use App\Service\competence\CompetenceService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/front/competences', name: 'competence_front_')]
class CompetenceFrontController extends AbstractController
{
    public function __construct(private CompetenceService $competenceService) {}

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
            fn(Competence $c) => $c->getEmail() === $email
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
            fn(Competence $c) => $c->getEmail() === $email
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

    #[Route('/calendar', name: 'calendar', methods: ['GET'])]
    public function calendar(): Response
    {
        return $this->render('Competences/front/Calendar.html.twig');
    }

    #[Route('/chatbot', name: 'chatbot', methods: ['GET'])]
    public function chatbot(): Response
    {
        return $this->render('Competences/front/ChatBoat.html.twig');
    }

    #[Route('/chatbot/send', name: 'chatbot_send', methods: ['POST'])]
    public function chatbotSend(Request $request): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $input = trim($data['message'] ?? '');

        if ($input === '') {
            return new JsonResponse(['reply' => ''], 400);
        }

        return new JsonResponse(['reply' => 'Je peux t’aider sur les compétences, le CV, le stress et l’entretien.']);
    }
}