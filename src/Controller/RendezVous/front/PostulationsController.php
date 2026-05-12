<?php

namespace App\Controller\RendezVous\front;

use App\Entity\RendezVous\Postulation;
use App\Entity\RendezVous\RendezVous;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/rendez-vous')]
class PostulationsController extends AbstractController
{
    #[Route('/postulations', name: 'app_front_postulations')]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $currentUserId = $this->getUserId($user);
        $currentUserEmail = $this->getUserEmail($user);

        $selectedId = $request->query->getInt('rv', 0);
        $search = trim((string) $request->query->get('search', ''));
        $filter = (string) $request->query->get('filter', 'Tous');

        if ($request->isMethod('POST')) {
            $action = $request->request->get('action');

            if ($action === 'add') {
                $this->addRendezVous($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'update') {
                $this->updateRendezVous($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'delete') {
                $this->deleteRendezVous($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'postuler') {
                $this->postuler($request, $em, $user, $currentUserId, $currentUserEmail);
            }

            if ($action === 'annuler_postulation') {
                $this->annulerPostulation($request, $em, $currentUserId);
            }

            if ($action === 'accept') {
                $this->changeStatus($request, $em, 'accepte', $currentUserId, $currentUserEmail);
            }

            if ($action === 'refuse') {
                $this->changeStatus($request, $em, 'refuse', $currentUserId, $currentUserEmail);
            }

            return $this->redirectToRoute('app_front_postulations', [
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
            } elseif ($filter === 'Competence' || $filter === 'Compétence') {
                $qb->andWhere('LOWER(r.competence) LIKE :s');
            } elseif ($filter === 'Type') {
                $qb->andWhere('LOWER(r.type) LIKE :s');
            } elseif ($filter === 'Email') {
                $qb->andWhere('LOWER(r.mailUser) LIKE :s');
            } else {
                $qb->andWhere('LOWER(r.titre) LIKE :s OR LOWER(r.competence) LIKE :s OR LOWER(r.type) LIKE :s OR LOWER(r.mailUser) LIKE :s');
            }

            $qb->setParameter('s', '%' . mb_strtolower($search) . '%');
        }

        $rendezVous = $qb->getQuery()->getResult();

        usort($rendezVous, function (RendezVous $a, RendezVous $b) use ($currentUserId, $currentUserEmail) {
            $aOwner = $this->isOwner($a, $currentUserId, $currentUserEmail);
            $bOwner = $this->isOwner($b, $currentUserId, $currentUserEmail);

            if ($aOwner && !$bOwner) {
                return -1;
            }

            if (!$aOwner && $bOwner) {
                return 1;
            }

            return 0;
        });

        $selectedRv = null;
        $postulations = [];

        if ($selectedId > 0) {
            $selectedRv = $rvRepo->find($selectedId);

            if ($selectedRv) {
                $postulations = $postRepo->findBy(
                    ['idRendezVous' => $selectedRv->getIdRendezVous()],
                    ['datePostulation' => 'DESC']
                );

                usort($postulations, function (Postulation $a, Postulation $b) use ($currentUserId) {
                    $aMe = $a->getIdPostulant() === $currentUserId;
                    $bMe = $b->getIdPostulant() === $currentUserId;

                    if ($aMe && !$bMe) {
                        return -1;
                    }

                    if (!$aMe && $bMe) {
                        return 1;
                    }

                    return 0;
                });
            }
        }

        return $this->render('RendezVous/front/postulations.html.twig', [
            'rendezVous' => $rendezVous,
            'selectedRv' => $selectedRv,
            'postulations' => $postulations,
            'search' => $search,
            'filter' => $filter,
            'currentUserId' => $currentUserId,
            'currentUserEmail' => $currentUserEmail,
        ]);
    }

    private function addRendezVous(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $rv = new RendezVous();

        $rv->setIdAdminCreateur($currentUserId);
        $rv->setMailUser($currentUserEmail);
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

    private function updateRendezVous(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $id = (int) $request->request->get('id_rendez_vous');
        $rv = $em->getRepository(RendezVous::class)->find($id);

        if (!$rv) {
            $this->addFlash('danger', 'Rendez-vous introuvable.');
            return;
        }

        if (!$this->isOwner($rv, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous pouvez modifier uniquement vos rendez-vous.');
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

    private function deleteRendezVous(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $id = (int) $request->request->get('id_rendez_vous');
        $rv = $em->getRepository(RendezVous::class)->find($id);

        if (!$rv) {
            $this->addFlash('danger', 'Rendez-vous introuvable.');
            return;
        }

        if (!$this->isOwner($rv, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous pouvez supprimer uniquement vos rendez-vous.');
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

    private function postuler(
        Request $request,
        EntityManagerInterface $em,
        object $user,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $idRv = (int) $request->request->get('selected_rv');
        $rv = $em->getRepository(RendezVous::class)->find($idRv);

        if (!$rv) {
            $this->addFlash('danger', 'Rendez-vous introuvable.');
            return;
        }

        if ($this->isOwner($rv, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous ne pouvez pas postuler à votre propre rendez-vous.');
            return;
        }

        $count = $em->getRepository(Postulation::class)->count([
            'idRendezVous' => $rv->getIdRendezVous(),
        ]);

        if ($count >= $rv->getNombrePlaces()) {
            $this->addFlash('danger', 'Le nombre maximum de postulations est atteint.');
            return;
        }

        $cvPath = null;
        $cvFile = $request->files->get('cv');

        if ($cvFile) {
            $newName = uniqid('cv_', true) . '.' . $cvFile->guessExtension();
            $cvFile->move($this->getParameter('kernel.project_dir') . '/public/uploads/cv', $newName);
            $cvPath = 'uploads/cv/' . $newName;
        }

        $post = new Postulation();
        $post->setIdRendezVous($rv->getIdRendezVous());
        $post->setIdPostulant($currentUserId);
        $post->setMailUser($currentUserEmail);
        $post->setMessage($request->request->get('message'));
        $post->setCv($cvPath);
        $post->setStatus('en_attente');
        $post->setDatePostulation(new \DateTime());
        $post->setRole($this->callGetter($user, ['getRole']));
        $post->setNom($this->callGetter($user, ['getNom']));
        $post->setPrenom($this->callGetter($user, ['getPrenom']));
        $post->setTitre($rv->getTitre());

        $em->persist($post);
        $em->flush();

        $this->addFlash('success', 'Postulation envoyée avec succès.');
    }

    private function annulerPostulation(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId
    ): void {
        $id = (int) $request->request->get('id_postulation');
        $post = $em->getRepository(Postulation::class)->find($id);

        if (!$post) {
            $this->addFlash('danger', 'Postulation introuvable.');
            return;
        }

        if ($post->getIdPostulant() !== $currentUserId) {
            $this->addFlash('danger', 'Vous pouvez annuler uniquement vos postulations.');
            return;
        }

        if ($post->getStatus() !== 'en_attente') {
            $this->addFlash('danger', 'Vous pouvez annuler uniquement une postulation en attente.');
            return;
        }

        $em->remove($post);
        $em->flush();

        $this->addFlash('success', 'Postulation annulée.');
    }

    private function changeStatus(
        Request $request,
        EntityManagerInterface $em,
        string $status,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $idPost = (int) $request->request->get('id_postulation');
        $post = $em->getRepository(Postulation::class)->find($idPost);

        if (!$post) {
            $this->addFlash('danger', 'Postulation introuvable.');
            return;
        }

        $rv = $em->getRepository(RendezVous::class)->find($post->getIdRendezVous());

        if (!$rv || !$this->isOwner($rv, $currentUserId, $currentUserEmail)) {
            $this->addFlash('danger', 'Vous ne pouvez gérer que les postulations de vos rendez-vous.');
            return;
        }

        $post->setStatus($status);
        $em->flush();

        $this->addFlash('success', $status === 'accepte' ? 'Postulation acceptée.' : 'Postulation refusée.');
    }

    private function isOwner(RendezVous $rv, int $currentUserId, ?string $currentUserEmail): bool
    {
        return $rv->getIdAdminCreateur() === $currentUserId
            && strtolower((string) $rv->getMailUser()) === strtolower((string) $currentUserEmail);
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

    private function callGetter(object $object, array $methods): ?string
    {
        foreach ($methods as $method) {
            if (method_exists($object, $method)) {
                return $object->$method();
            }
        }

        return null;
    }
}