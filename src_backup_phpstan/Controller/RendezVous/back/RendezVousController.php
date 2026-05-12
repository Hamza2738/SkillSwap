<?php

namespace App\Controller\RendezVous\back;

use App\Entity\RendezVous\Postulation;
use App\Entity\RendezVous\RendezVous;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Service\RendezVous\GeminiPostulationService;
use Symfony\Component\HttpFoundation\JsonResponse;
use App\Repository\PostulationRepository;
use App\Repository\RendezVousRepository;



#[Route('/admin/rendez-vous')]
class RendezVousController extends AbstractController
{
    #[Route('/', name: 'app_admin_rendez_vous')]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $selectedId = $request->query->getInt('rv', 0);
        $search = trim((string) $request->query->get('search', ''));
        $filter = (string) $request->query->get('filter', 'Tous');

        if ($request->isMethod('POST')) {
            $action = $request->request->get('action');

            if ($action === 'add') {
                $this->addRendezVous($request, $em, $user);
            }

            if ($action === 'update') {
                $this->updateRendezVous($request, $em, $user);
            }

            if ($action === 'delete') {
                $this->deleteRendezVous($request, $em, $user);
            }

            if ($action === 'accept') {
                $this->changePostulationStatus($request, $em, 'accepte');
            }

            if ($action === 'refuse') {
                $this->changePostulationStatus($request, $em, 'refuse');
            }

            if ($action === 'delete_postulation') {
                $this->deletePostulation($request, $em);
            }

            return $this->redirectToRoute('app_admin_rendez_vous', [
                'rv' => $request->request->get('selected_rv'),
            ]);
        }

        $rvRepo = $em->getRepository(RendezVous::class);
        $postRepo = $em->getRepository(Postulation::class);

        $qb = $rvRepo->createQueryBuilder('r')
            ->orderBy('r.dateRendezVous', 'DESC');

        if ($search !== '') {
            if ($filter === 'Titre') {
                $qb->andWhere('LOWER(r.titre) LIKE :s');
            } elseif ($filter === 'Compétence') {
                $qb->andWhere('LOWER(r.competence) LIKE :s');
            } elseif ($filter === 'Type') {
                $qb->andWhere('LOWER(r.type) LIKE :s');
            } else {
                $qb->andWhere('LOWER(r.titre) LIKE :s OR LOWER(r.competence) LIKE :s OR LOWER(r.type) LIKE :s');
            }

            $qb->setParameter('s', '%' . mb_strtolower($search) . '%');
        }

        $rendezVous = $qb->getQuery()->getResult();

        $selectedRv = null;
        $postulations = [];

        if ($selectedId > 0) {
            $selectedRv = $rvRepo->find($selectedId);

            if ($selectedRv) {
                $postulations = $postRepo->findBy(
                    ['idRendezVous' => $selectedRv->getIdRendezVous()],
                    ['datePostulation' => 'DESC']
                );
            }
        }

        return $this->render('RendezVous/back/index.html.twig', [
            'rendezVous' => $rendezVous,
            'selectedRv' => $selectedRv,
            'postulations' => $postulations,
            'search' => $search,
            'filter' => $filter,
            'currentUser' => $user,
        ]);
    }

    private function addRendezVous(Request $request, EntityManagerInterface $em, object $user): void
    {
        $rv = new RendezVous();

        $rv->setIdAdminCreateur($this->getUserId($user));
        $rv->setMailUser($this->getUserEmail($user));
        $rv->setTitre($request->request->get('titre'));
        $rv->setDescription($request->request->get('description'));
        $rv->setCompetence($request->request->get('competence'));
        $rv->setType($request->request->get('type'));
        $rv->setNombrePlaces((int) $request->request->get('nombre_places'));
        $rv->setDateCreationRendezVous(new \DateTime());

        $date = $request->request->get('date_rendez_vous');
        if ($date) {
            $rv->setDateRendezVous(new \DateTime($date));
        }

        $em->persist($rv);
        $em->flush();

        $this->addFlash('success', 'Rendez-vous ajouté avec succès.');
    }

    private function updateRendezVous(Request $request, EntityManagerInterface $em, object $user): void
    {
        $id = (int) $request->request->get('id_rendez_vous');
        $rv = $em->getRepository(RendezVous::class)->find($id);

        if (!$rv) {
            $this->addFlash('danger', 'Rendez-vous introuvable.');
            return;
        }

        if ($rv->getIdAdminCreateur() !== $this->getUserId($user)) {
            $this->addFlash('danger', 'Vous ne pouvez modifier que vos rendez-vous.');
            return;
        }

        $rv->setTitre($request->request->get('titre'));
        $rv->setDescription($request->request->get('description'));
        $rv->setCompetence($request->request->get('competence'));
        $rv->setType($request->request->get('type'));
        $rv->setNombrePlaces((int) $request->request->get('nombre_places'));

        $date = $request->request->get('date_rendez_vous');
        if ($date) {
            $rv->setDateRendezVous(new \DateTime($date));
        }

        $em->flush();

        $this->addFlash('success', 'Rendez-vous modifié avec succès.');
    }

    private function deleteRendezVous(Request $request, EntityManagerInterface $em, object $user): void
    {
        $id = (int) $request->request->get('id_rendez_vous');
        $rv = $em->getRepository(RendezVous::class)->find($id);

        if (!$rv) {
            $this->addFlash('danger', 'Rendez-vous introuvable.');
            return;
        }

        if ($rv->getIdAdminCreateur() !== $this->getUserId($user)) {
            $this->addFlash('danger', 'Vous ne pouvez supprimer que vos rendez-vous.');
            return;
        }

        $posts = $em->getRepository(Postulation::class)->findBy([
            'idRendezVous' => $rv->getIdRendezVous(),
        ]);

        foreach ($posts as $post) {
            $em->remove($post);
        }

        $em->remove($rv);
        $em->flush();

        $this->addFlash('success', 'Rendez-vous supprimé avec succès.');
    }

    private function changePostulationStatus(Request $request, EntityManagerInterface $em, string $status): void
    {
        $id = (int) $request->request->get('id_postulation');
        $postulation = $em->getRepository(Postulation::class)->find($id);

        if (!$postulation) {
            $this->addFlash('danger', 'Postulation introuvable.');
            return;
        }

        $postulation->setStatus($status);
        $em->flush();

        $message = $status === 'accepte'
            ? 'Postulation acceptée.'
            : 'Postulation refusée.';

        $this->addFlash('success', $message);
    }

    private function deletePostulation(Request $request, EntityManagerInterface $em): void
    {
        $id = (int) $request->request->get('id_postulation');
        $postulation = $em->getRepository(Postulation::class)->find($id);

        if (!$postulation) {
            $this->addFlash('danger', 'Postulation introuvable.');
            return;
        }

        $em->remove($postulation);
        $em->flush();

        $this->addFlash('success', 'Postulation supprimée.');
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


#[Route('/analyse-ia/{id}', name: 'app_admin_postulation_analyse_ia', methods: ['GET'])]
public function analyseIaPostulation(
    int $id,
    PostulationRepository $postulationRepository,
    RendezVousRepository $rendezVousRepository,
    GeminiPostulationService $geminiPostulationService
): JsonResponse {
    try {
        $postulation = $postulationRepository->find($id);

        if (!$postulation) {
            return new JsonResponse([
                'success' => false,
                'message' => 'Postulation introuvable.'
            ]);
        }

        $rendezVous = $rendezVousRepository->find($postulation->getIdRendezVous());

        if (!$rendezVous) {
            return new JsonResponse([
                'success' => false,
                'message' => 'Rendez-vous introuvable.'
            ]);
        }

        $analyse = $geminiPostulationService->analyserPostulation($rendezVous, $postulation);

        return new JsonResponse([
            'success' => true,
            'analyse' => $analyse
        ]);
    } catch (\Throwable $e) {
        return new JsonResponse([
            'success' => false,
            'message' => 'Erreur Symfony : ' . $e->getMessage()
        ]);
    }
}
}