<?php

namespace App\Controller\suiviTache\front;

use App\Entity\suiviTache\Projet;
use App\Entity\suiviTache\Tache;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/suivi-tache')]
class TacheFrontController extends AbstractController
{
    #[Route('/front', name: 'app_front_suivi_tache')]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $currentUserId = $this->getUserId($user);
        $currentUserEmail = $this->getUserEmail($user);

        $selectedProjetId = $request->query->getInt('projet', 0);
        $selectedTacheId = $request->query->getInt('tache', 0);

        if ($request->isMethod('POST')) {
            $action = $request->request->get('action');

            if ($action === 'add_tache') {
                $this->addTache($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'update_tache') {
                $this->updateTache($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'delete_tache') {
                $this->deleteTache($request, $em, $currentUserId, $currentUserEmail);
            }

            return $this->redirectToRoute('app_front_suivi_tache', [
                'projet' => $request->request->get('selected_projet'),
            ]);
        }

        $projets = $em->getRepository(Projet::class)->findBy([], ['id' => 'DESC']);

        $selectedProjet = null;
        $taches = [];
        $selectedTache = null;

        if ($selectedProjetId > 0) {
            $selectedProjet = $em->getRepository(Projet::class)->find($selectedProjetId);

            if ($selectedProjet) {
                $taches = $em->getRepository(Tache::class)->findBy(
                    ['projetId' => $selectedProjet->getId()],
                    ['id' => 'DESC']
                );
            }
        } elseif (!empty($projets)) {
            $selectedProjet = $projets[0];

            return $this->redirectToRoute('app_front_suivi_tache', [
                'projet' => $selectedProjet->getId(),
            ]);
        }

        if ($selectedTacheId > 0) {
            $selectedTache = $em->getRepository(Tache::class)->find($selectedTacheId);
        }

        return $this->render('suivi_tache/front.html.twig', [
            'projets' => $projets,
            'selectedProjet' => $selectedProjet,
            'taches' => $taches,
            'selectedTache' => $selectedTache,
            'currentUserId' => $currentUserId,
            'currentUserEmail' => $currentUserEmail,
        ]);
    }

    private function addTache(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $projetId = (int) $request->request->get('selected_projet');
        $projet = $em->getRepository(Projet::class)->find($projetId);

        if (!$projet) {
            $this->addFlash('danger', 'Veuillez sélectionner un projet.');
            return;
        }

        $titre = trim((string) $request->request->get('titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre de la tâche est obligatoire.');
            return;
        }

        if (mb_strlen($titre) < 3) {
            $this->addFlash('danger', 'Le titre de la tâche doit contenir au moins 3 caractères.');
            return;
        }

        if (!$request->request->get('statut')) {
            $this->addFlash('danger', 'Veuillez choisir un statut.');
            return;
        }

        if (!$request->request->get('priorite')) {
            $this->addFlash('danger', 'Veuillez choisir une priorité.');
            return;
        }

        $echeance = $request->request->get('echeance');

        if ($echeance && new \DateTime($echeance) < new \DateTime('today')) {
            $this->addFlash('danger', "La date d'échéance ne peut pas être dans le passé.");
            return;
        }

        $tache = new Tache();
        $tache->setTitre($titre);
        $tache->setDescription($this->emptyToNull($request->request->get('description')));
        $tache->setStatut($request->request->get('statut'));
        $tache->setPriorite($request->request->get('priorite'));
        $tache->setEcheance($echeance ? new \DateTime($echeance) : null);
        $tache->setProjetId($projet->getId());
        $tache->setUserId($currentUserId);
        $tache->setMailUser($currentUserEmail);

        $em->persist($tache);
        $em->flush();

        $this->addFlash('success', 'Tâche ajoutée avec succès.');
    }

    private function updateTache(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $id = (int) $request->request->get('tache_id');
        $tache = $em->getRepository(Tache::class)->find($id);

        if (!$tache) {
            $this->addFlash('danger', 'Veuillez sélectionner une tâche à modifier.');
            return;
        }

        if (!$this->isOwnerTache($tache, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous ne pouvez modifier que vos propres tâches.');
            return;
        }

        $titre = trim((string) $request->request->get('titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre de la tâche est obligatoire.');
            return;
        }

        $echeance = $request->request->get('echeance');

        if ($echeance && new \DateTime($echeance) < new \DateTime('today')) {
            $this->addFlash('danger', "La date d'échéance ne peut pas être dans le passé.");
            return;
        }

        $tache->setTitre($titre);
        $tache->setDescription($this->emptyToNull($request->request->get('description')));
        $tache->setStatut($request->request->get('statut'));
        $tache->setPriorite($request->request->get('priorite'));
        $tache->setEcheance($echeance ? new \DateTime($echeance) : null);
        $tache->setMailUser($currentUserEmail);

        $em->flush();

        $this->addFlash('success', 'Tâche modifiée avec succès.');
    }

    private function deleteTache(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $id = (int) $request->request->get('tache_id');
        $tache = $em->getRepository(Tache::class)->find($id);

        if (!$tache) {
            $this->addFlash('danger', 'Veuillez sélectionner une tâche à supprimer.');
            return;
        }

        if (!$this->isOwnerTache($tache, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous ne pouvez supprimer que vos propres tâches.');
            return;
        }

        $em->remove($tache);
        $em->flush();

        $this->addFlash('success', 'Tâche supprimée avec succès.');
    }

    private function isOwnerTache(Tache $tache, int $currentUserId, ?string $currentUserEmail): bool
    {
        if ($tache->getUserId() && $tache->getUserId() === $currentUserId) {
            return true;
        }

        return strtolower((string) $tache->getMailUser()) === strtolower((string) $currentUserEmail);
    }

    private function emptyToNull(?string $value): ?string
    {
        $value = trim((string) $value);
        return $value === '' ? null : $value;
    }

    private function getUserId(object $user): int
    {
        if (method_exists($user, 'getIdUtilisateur')) {
            return (int) $user->getIdUtilisateur();
        }

        if (method_exists($user, 'getId_utilisateur')) {
            return (int) $user->getId_utilisateur();
        }

        if (method_exists($user, 'getId')) {
            return (int) $user->getId();
        }

        return 0;
    }

    private function getUserEmail(object $user): ?string
    {
        if (method_exists($user, 'getEmail')) {
            return $user->getEmail();
        }

        if (method_exists($user, 'getUserIdentifier')) {
            return $user->getUserIdentifier();
        }

        return null;
    }
}