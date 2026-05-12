<?php

namespace App\Controller\Offres\front;

use App\Entity\Offres\Candidature;
use App\Entity\Offres\Commentaire;
use App\Entity\Offres\Offre;
use Doctrine\DBAL\Connection;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;




#[Route('/offres')]
class OffreFrontController extends AbstractController
{
    #[Route('/front', name: 'app_front_offres')]
    public function index(
        Request $request,
        EntityManagerInterface $em,
        Connection $connection
    ): Response {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $currentUserId = $this->getUserId($user);
        $currentUserEmail = $this->getUserEmail($user);

        if ($request->isMethod('POST')) {
            $action = $request->request->get('action');

            if ($action === 'postuler') {
                $this->postuler($request, $em, $currentUserId, $currentUserEmail);
            }

            if ($action === 'commenter') {
                $this->commenter($request, $em, $currentUserId);
            }

            if ($action === 'like') {
                $this->toggleLike($request, $em, $connection, $currentUserId);
            }

            return $this->redirectToRoute('app_front_offres');
        }

        $offres = $em->getRepository(Offre::class)->findBy(
            ['statut' => 'ouverte'],
            ['datePublication' => 'DESC']
        );

      $abonnements = $em->getRepository(Candidature::class)->findBy(
    ['utilisateur' => $user],
    ['dateCandidature' => 'DESC']
);

        $commentairesParOffre = [];

        foreach ($offres as $offre) {
            $commentairesParOffre[$offre->getIdOffre()] = $em->getRepository(Commentaire::class)->findBy(
                ['idOffre' => $offre->getIdOffre()],
                ['dateCommentaire' => 'DESC']
            );
        }
$dejaCandidat = [];

foreach ($abonnements as $abonnement) {
    if ($abonnement->getOffre()) {
        $dejaCandidat[$abonnement->getOffre()->getIdOffre()] = true;
    }
}
        $likedByUser = [];

        foreach ($offres as $offre) {
            $likedByUser[$offre->getIdOffre()] = $this->hasLiked(
                $connection,
                $offre->getIdOffre(),
                $currentUserId
            );
        }

        return $this->render('Offres/front/index.html.twig', [
            'offres' => $offres,
            'abonnements' => $abonnements,
            'commentairesParOffre' => $commentairesParOffre,
            'dejaCandidat' => $dejaCandidat,
            'likedByUser' => $likedByUser,
            'currentUserId' => $currentUserId,
            'currentUserEmail' => $currentUserEmail,
        ]);
    }

    private function postuler(
        Request $request,
        EntityManagerInterface $em,
        int $currentUserId,
        ?string $currentUserEmail
    ): void {
        $idOffre = (int) $request->request->get('id_offre');
        $offre = $em->getRepository(Offre::class)->find($idOffre);

        if (!$offre) {
            $this->addFlash('danger', 'Offre introuvable.');
            return;
        }

       $exists = $em->getRepository(Candidature::class)->findOneBy([
    'utilisateur' => $this->getUser(),
    'offre' => $offre,
]);
        if ($exists) {
            $this->addFlash('danger', 'Vous avez déjà postulé à cette offre.');
            return;
        }

        $cvPath = null;
        $cvFile = $request->files->get('cv');

        if ($cvFile) {
            $newName = uniqid('cv_', true) . '.' . $cvFile->guessExtension();
            $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/cv';

            if (!is_dir($uploadDir)) {
                mkdir($uploadDir, 0777, true);
            }

            $cvFile->move($uploadDir, $newName);
            $cvPath = 'uploads/cv/' . $newName;
        }

        $now = new \DateTime();
        $expiration = (clone $now)->modify('+' . max(1, (int) $offre->getDuree()) . ' days');

        $candidature = new Candidature();
        $candidature->setDateCandidature($now);
        $candidature->setDateAbonnement($now);
        $candidature->setDateExpiration($expiration);
        $candidature->setMatriculeOffre($offre->getMatricule());
        $candidature->setMailAcheteur($currentUserEmail);
        $candidature->setPrixPaye($offre->getPrix() ?: '0.00');
        $candidature->setModePaiement($request->request->get('mode_paiement') ?: 'gratuit');
        $candidature->setReferencePaiement('REF-' . strtoupper(substr(uniqid(), -8)));
        $candidature->setStatut('en_attente');
        $candidature->setMessage($request->request->get('message'));
        $candidature->setLettreMotivation($request->request->get('lettre_motivation'));
        $candidature->setCv($cvPath);
        $candidature->setIsActive(true);
        $candidature->setUtilisateur($this->getUser());
$candidature->setOffre($offre);

        $em->persist($candidature);
        $em->flush();

        $this->addFlash('success', 'Souscription envoyée avec succès.');
    }

   private function commenter(Request $request, EntityManagerInterface $em, ?int $currentUserId): void
{
    $idOffre = $request->request->get('id_offre');
    $contenu = trim((string) $request->request->get('contenu'));

    if (!$currentUserId) {
        $this->addFlash('danger', 'Vous devez être connecté pour commenter.');
        return;
    }

    if (!$idOffre || $contenu === '') {
        $this->addFlash('danger', 'Commentaire invalide.');
        return;
    }

    $offre = $em->getRepository(Offre::class)->find($idOffre);

    if (!$offre) {
        $this->addFlash('danger', 'Offre introuvable.');
        return;
    }

    $commentaire = new Commentaire();

$commentaire->setIdOffre($offre->getIdOffre());
$commentaire->setIdUtilisateur($currentUserId);
$commentaire->setContenu($contenu);
$commentaire->setDateCommentaire(new \DateTime());

$em->persist($commentaire);
$em->flush();

$this->addFlash('success', 'Commentaire publié.');
}

    private function toggleLike(
        Request $request,
        EntityManagerInterface $em,
        Connection $connection,
        int $currentUserId
    ): void {
        $idOffre = (int) $request->request->get('id_offre');
        $offre = $em->getRepository(Offre::class)->find($idOffre);

        if (!$offre) {
            $this->addFlash('danger', 'Offre introuvable.');
            return;
        }

        try {
            if ($this->hasLiked($connection, $idOffre, $currentUserId)) {
                $connection->delete('offre_like', [
                    'id_offre' => $idOffre,
                    'id_utilisateur' => $currentUserId,
                ]);

                $offre->setNbLikes(max(0, (int) $offre->getNbLikes() - 1));
            } else {
                $connection->insert('offre_like', [
                    'id_offre' => $idOffre,
                    'id_utilisateur' => $currentUserId,
                ]);

                $offre->setNbLikes((int) $offre->getNbLikes() + 1);
            }

            $em->flush();
        } catch (\Throwable $e) {
            $this->addFlash('danger', "Table offre_like introuvable. Crée-la ou supprime le bouton like.");
        }
    }

    private function hasLiked(Connection $connection, int $idOffre, int $currentUserId): bool
    {
        try {
            $result = $connection->fetchOne(
                'SELECT 1 FROM offre_like WHERE id_offre = ? AND id_utilisateur = ?',
                [$idOffre, $currentUserId]
            );

            return (bool) $result;
        } catch (\Throwable) {
            return false;
        }
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