<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Connexion;
use App\Entity\utilisateur\Utilisateur;
use App\Repository\ConnexionRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/reseau')]
class ReseauController extends AbstractController
{
    #[Route('/inviter/{id}', name: 'app_reseau_inviter', methods: ['POST'])]
    public function inviter(
        int $id,
        UtilisateurRepository $utilisateurRepository,
        ConnexionRepository $connexionRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var Utilisateur|null $me */
        $me = $this->getUser();

        if (!$me instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $receveur = $utilisateurRepository->find($id);

        if (!$receveur instanceof Utilisateur) {
            $this->addFlash('error', 'Utilisateur introuvable.');
            return $this->redirectToRoute('app_profil');
        }

        if ($receveur->getIdUtilisateur() === $me->getIdUtilisateur()) {
            $this->addFlash('error', 'Impossible de vous inviter vous-même.');
            return $this->redirectToRoute('app_profil');
        }

        $relation = $connexionRepository->findRelation($me, $receveur);

        if ($relation instanceof Connexion) {
            if ($relation->getStatut() === 'en_attente') {
                $this->addFlash('error', 'Invitation déjà envoyée.');
            } elseif ($relation->getStatut() === 'accepte') {
                $this->addFlash('error', 'Vous êtes déjà connectés.');
            }

            return $this->redirectToRoute('app_profil', [
                'q' => $receveur->getEmail(),
            ]);
        }

        $connexion = new Connexion();
        $connexion->setDemandeur($me);
        $connexion->setReceveur($receveur);
        $connexion->setStatut('en_attente');

        $em->persist($connexion);
        $em->flush();

        $this->addFlash('success', 'Invitation envoyée à ' . $receveur->getPrenom() . ' ✅');

        return $this->redirectToRoute('app_profil', [
            'q' => $receveur->getEmail(),
        ]);
    }

    #[Route('/accepter/{id}', name: 'app_reseau_accepter', methods: ['POST'])]
    public function accepter(
        int $id,
        ConnexionRepository $connexionRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var Utilisateur|null $me */
        $me = $this->getUser();

        if (!$me instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $connexion = $connexionRepository->find($id);

        if (!$connexion instanceof Connexion) {
            $this->addFlash('error', 'Invitation introuvable.');
            return $this->redirectToRoute('app_profil');
        }

        if ($connexion->getReceveur()?->getIdUtilisateur() !== $me->getIdUtilisateur()) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('app_profil');
        }

        $connexion->setStatut('accepte');
        $em->flush();

        $this->addFlash('success', 'Connexion acceptée ✅');

        return $this->redirectToRoute('app_profil');
    }

    #[Route('/refuser/{id}', name: 'app_reseau_refuser', methods: ['POST'])]
    public function refuser(
        int $id,
        ConnexionRepository $connexionRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var Utilisateur|null $me */
        $me = $this->getUser();

        if (!$me instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $connexion = $connexionRepository->find($id);

        if (!$connexion instanceof Connexion) {
            $this->addFlash('error', 'Invitation introuvable.');
            return $this->redirectToRoute('app_profil');
        }

        if (
            $connexion->getReceveur()?->getIdUtilisateur() !== $me->getIdUtilisateur()
            && $connexion->getDemandeur()?->getIdUtilisateur() !== $me->getIdUtilisateur()
        ) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('app_profil');
        }

        $em->remove($connexion);
        $em->flush();

        $this->addFlash('success', 'Invitation supprimée.');

        return $this->redirectToRoute('app_profil');
    }
#[Route('/reseau/retirer/{id}', name: 'app_reseau_retirer', methods: ['POST'])]
public function retirer(
    int $id,
    UtilisateurRepository $utilisateurRepository,
    ConnexionRepository $connexionRepository,
    EntityManagerInterface $em
): Response {
    /** @var Utilisateur|null $me */
    $me = $this->getUser();

    if (!$me instanceof Utilisateur) {
        return $this->redirectToRoute('app_login');
    }

    $autreUtilisateur = $utilisateurRepository->find($id);

    if (!$autreUtilisateur instanceof Utilisateur) {
        throw $this->createNotFoundException('Utilisateur introuvable.');
    }

    $relation = $connexionRepository->findRelation($me, $autreUtilisateur);

    if (!$relation instanceof Connexion) {
        $this->addFlash('error', 'Aucune relation trouvée.');

        return $this->redirectToRoute('app_profil', [
            'q' => $autreUtilisateur->getEmail(),
        ]);
    }

    $em->remove($relation);
    $em->flush();

    $this->addFlash('success', 'Relation retirée.');

    return $this->redirectToRoute('app_profil', [
        'q' => $autreUtilisateur->getEmail(),
    ]);
}
}