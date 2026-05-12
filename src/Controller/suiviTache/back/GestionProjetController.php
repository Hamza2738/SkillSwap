<?php

namespace App\Controller\suiviTache\back;

use App\Entity\suiviTache\Projet;
use App\Entity\suiviTache\Tache;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/suivi-tache')]
class GestionProjetController extends AbstractController
{
    #[Route('/gestion', name: 'app_admin_suivi_tache_gestion')]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $currentUserId = $this->getUserId($user);
        $currentUserEmail = $this->getUserEmail($user);

        $selectedProjetId = $request->query->getInt('projet', 0);

        if ($request->isMethod('POST')) {
            $action = $request->request->get('action');

            if ($action === 'add_projet') {
                $this->addProjet($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'update_projet') {
                $this->updateProjet($request, $em, $currentUserId);
            }

            if ($action === 'delete_projet') {
                $this->deleteProjet($request, $em, $currentUserId);
            }

            if ($action === 'add_tache') {
                $this->addTache($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'update_tache') {
                $this->updateTache($request, $em, $currentUserId);
            }

            if ($action === 'delete_tache') {
                $this->deleteTache($request, $em, $currentUserId);
            }

            return $this->redirectToRoute('app_admin_suivi_tache_gestion', [
                'projet' => $request->request->get('selected_projet'),
            ]);
        }

        $projets = $em->getRepository(Projet::class)->findBy([], ['id' => 'DESC']);

        $selectedProjet = null;
        $taches = [];

        if ($selectedProjetId > 0) {
            $selectedProjet = $em->getRepository(Projet::class)->find($selectedProjetId);

            if ($selectedProjet) {
                $taches = $em->getRepository(Tache::class)->findBy(
                    ['projetId' => $selectedProjet->getId()],
                    ['id' => 'DESC']
                );
            }
        }

        return $this->render('suivi_tache/gestion.html.twig', [
            'projets' => $projets,
            'selectedProjet' => $selectedProjet,
            'taches' => $taches,
            'currentUserId' => $currentUserId,
            'currentUserEmail' => $currentUserEmail,
        ]);
    }

    private function addProjet(Request $request, EntityManagerInterface $em, int $currentUserId, ?string $currentUserEmail): void
    {
        $titre = trim((string) $request->request->get('projet_titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre du projet est obligatoire.');
            return;
        }

        if (mb_strlen($titre) < 3) {
            $this->addFlash('danger', 'Le titre du projet doit contenir au moins 3 caractères.');
            return;
        }

        $projet = new Projet();
        $projet->setTitre($titre);
        $projet->setDescription($this->emptyToNull($request->request->get('projet_description')));
        $projet->setStatut($request->request->get('projet_statut'));
        $projet->setUserId($currentUserId);
        $projet->setMailUser($currentUserEmail);

        $dateDebut = $request->request->get('date_debut');
        $dateFin = $request->request->get('date_fin');

        $projet->setDateDebut($dateDebut ? new \DateTime($dateDebut) : null);
        $projet->setDateFin($dateFin ? new \DateTime($dateFin) : null);

        if (!$projet->getStatut()) {
            $this->addFlash('danger', 'Veuillez choisir un statut du projet.');
            return;
        }

        if ($projet->getDateDebut() && $projet->getDateFin() && $projet->getDateFin() < $projet->getDateDebut()) {
            $this->addFlash('danger', 'La date de fin ne peut pas être avant la date début.');
            return;
        }

        $em->persist($projet);
        $em->flush();

        $this->addFlash('success', 'Projet ajouté avec succès.');
    }

    private function updateProjet(Request $request, EntityManagerInterface $em, int $currentUserId): void
    {
        $id = (int) $request->request->get('projet_id');
        $projet = $em->getRepository(Projet::class)->find($id);

        if (!$projet) {
            $this->addFlash('danger', 'Projet introuvable.');
            return;
        }

        if (!$this->isOwnerProjet($projet, $currentUserId)) {
            $this->addFlash('danger', 'Vous pouvez modifier uniquement vos projets.');
            return;
        }

        $titre = trim((string) $request->request->get('projet_titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre du projet est obligatoire.');
            return;
        }

        $projet->setTitre($titre);
        $projet->setDescription($this->emptyToNull($request->request->get('projet_description')));
        $projet->setStatut($request->request->get('projet_statut'));

        $dateDebut = $request->request->get('date_debut');
        $dateFin = $request->request->get('date_fin');

        $projet->setDateDebut($dateDebut ? new \DateTime($dateDebut) : null);
        $projet->setDateFin($dateFin ? new \DateTime($dateFin) : null);

        if ($projet->getDateDebut() && $projet->getDateFin() && $projet->getDateFin() < $projet->getDateDebut()) {
            $this->addFlash('danger', 'La date de fin ne peut pas être avant la date début.');
            return;
        }

        $em->flush();

        $this->addFlash('success', 'Projet modifié avec succès.');
    }

    private function deleteProjet(Request $request, EntityManagerInterface $em, int $currentUserId): void
    {
        $id = (int) $request->request->get('projet_id');
        $projet = $em->getRepository(Projet::class)->find($id);

        if (!$projet) {
            $this->addFlash('danger', 'Projet introuvable.');
            return;
        }

        if (!$this->isOwnerProjet($projet, $currentUserId)) {
            $this->addFlash('danger', 'Vous pouvez supprimer uniquement vos projets.');
            return;
        }

        $taches = $em->getRepository(Tache::class)->findBy([
            'projetId' => $projet->getId(),
        ]);

        foreach ($taches as $tache) {
            $em->remove($tache);
        }

        $em->remove($projet);
        $em->flush();

        $this->addFlash('success', 'Projet supprimé avec succès.');
    }

    private function addTache(Request $request, EntityManagerInterface $em, int $currentUserId, ?string $currentUserEmail): void
    {
        $projetId = (int) $request->request->get('selected_projet');
        $projet = $em->getRepository(Projet::class)->find($projetId);

        if (!$projet) {
            $this->addFlash('danger', 'Sélectionnez un projet.');
            return;
        }

        $titre = trim((string) $request->request->get('tache_titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre de la tâche est obligatoire.');
            return;
        }

        if (mb_strlen($titre) < 3) {
            $this->addFlash('danger', 'Le titre de la tâche doit contenir au moins 3 caractères.');
            return;
        }

        if (!$request->request->get('tache_statut')) {
            $this->addFlash('danger', 'Veuillez choisir un statut.');
            return;
        }

        if (!$request->request->get('priorite')) {
            $this->addFlash('danger', 'Veuillez choisir une priorité.');
            return;
        }

        $tache = new Tache();
        $tache->setTitre($titre);
        $tache->setDescription($this->emptyToNull($request->request->get('tache_description')));
        $tache->setStatut($request->request->get('tache_statut'));
        $tache->setPriorite($request->request->get('priorite'));
        $tache->setProjetId($projet->getId());
        $tache->setUserId($currentUserId);
        $tache->setMailUser($currentUserEmail);
        $tache->setEvaluation($request->request->get('evaluation'));

        $echeance = $request->request->get('echeance');
        $tache->setEcheance($echeance ? new \DateTime($echeance) : null);

        $em->persist($tache);
        $em->flush();

        $this->addFlash('success', 'Tâche ajoutée avec succès.');
    }

    private function updateTache(Request $request, EntityManagerInterface $em, int $currentUserId): void
    {
        $id = (int) $request->request->get('tache_id');
        $tache = $em->getRepository(Tache::class)->find($id);

        if (!$tache) {
            $this->addFlash('danger', 'Sélectionnez une tâche à modifier.');
            return;
        }

        $projet = $em->getRepository(Projet::class)->find($tache->getProjetId());

        if (!$projet || !$this->isOwnerProjet($projet, $currentUserId)) {
            $this->addFlash('danger', 'Vous pouvez modifier les tâches uniquement dans vos projets.');
            return;
        }

        $titre = trim((string) $request->request->get('tache_titre'));

        if ($titre === '') {
            $this->addFlash('danger', 'Le titre de la tâche est obligatoire.');
            return;
        }

        $tache->setTitre($titre);
        $tache->setDescription($this->emptyToNull($request->request->get('tache_description')));
        $tache->setStatut($request->request->get('tache_statut'));
        $tache->setPriorite($request->request->get('priorite'));
        $tache->setEvaluation($request->request->get('evaluation'));

        $echeance = $request->request->get('echeance');
        $tache->setEcheance($echeance ? new \DateTime($echeance) : null);

        $em->flush();

        $this->addFlash('success', 'Tâche modifiée avec succès.');
    }

    private function deleteTache(Request $request, EntityManagerInterface $em, int $currentUserId): void
    {
        $id = (int) $request->request->get('tache_id');
        $tache = $em->getRepository(Tache::class)->find($id);

        if (!$tache) {
            $this->addFlash('danger', 'Sélectionnez une tâche à supprimer.');
            return;
        }

        $projet = $em->getRepository(Projet::class)->find($tache->getProjetId());

        if (!$projet || !$this->isOwnerProjet($projet, $currentUserId)) {
            $this->addFlash('danger', 'Vous pouvez supprimer les tâches uniquement dans vos projets.');
            return;
        }

        $em->remove($tache);
        $em->flush();

        $this->addFlash('success', 'Tâche supprimée avec succès.');
    }

    private function isOwnerProjet(Projet $projet, int $currentUserId): bool
    {
        return $projet->getUserId() === $currentUserId;
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