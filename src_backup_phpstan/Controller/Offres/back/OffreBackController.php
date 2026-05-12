<?php

namespace App\Controller\Offres\back;

use App\Entity\Offres\Candidature;
use App\Entity\Offres\Commentaire;
use App\Entity\Offres\Offre;
use App\Entity\Offres\OffreLike;
use App\Entity\utilisateur\Utilisateur;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class OffreBackController extends AbstractController
{
    #[Route('/admin/offres', name: 'app_admin_offres', methods: ['GET', 'POST'])]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        $currentUserId = null;
        $currentUserEmail = null;

        if ($user) {
            if (method_exists($user, 'getIdUtilisateur')) {
                $currentUserId = $user->getIdUtilisateur();
            } elseif (method_exists($user, 'getId')) {
                $currentUserId = $user->getId();
            }

            if (method_exists($user, 'getEmail')) {
                $currentUserEmail = $user->getEmail();
            } elseif (method_exists($user, 'getUserIdentifier')) {
                $currentUserEmail = $user->getUserIdentifier();
            }
        }

        $offreRepository = $em->getRepository(Offre::class);
        $candidatureRepository = $em->getRepository(Candidature::class);
        $commentaireRepository = $em->getRepository(Commentaire::class);
        $offreLikeRepository = $em->getRepository(OffreLike::class);
        $utilisateurRepository = $em->getRepository(Utilisateur::class);

        $action = $request->request->get('action');

        if ($request->isMethod('POST')) {
            if ($action === 'add_offre') {
                $offre = new Offre();

                $offre->setTitre($request->request->get('titre'));
                $offre->setDescription($request->request->get('description'));
                $offre->setTypeOffre($request->request->get('type_offre'));
                $offre->setPrix($this->formatDecimal($request->request->get('prix')));
                $offre->setBudget($this->formatDecimal($request->request->get('budget')));
                $offre->setDuree((int) $request->request->get('duree'));
                $offre->setStatut($request->request->get('statut', 'ouverte'));
                $offre->setLocalisation($request->request->get('localisation'));
                $offre->setTags($request->request->get('tags'));
                $offre->setDatePublication(new \DateTime());
                $offre->setVues(0);

                if ($request->request->get('date_limite')) {
                    $offre->setDateLimite(new \DateTime($request->request->get('date_limite')));
                }

                if ($currentUserId) {
                    $offre->setIdUtilisateur($currentUserId);
                }

                if ($currentUserEmail) {
                    $offre->setMailUser($currentUserEmail);
                }

                if (!$offre->getMatricule()) {
                    $offre->setMatricule('OFF-' . strtoupper(substr(uniqid(), -6)));
                }

                $em->persist($offre);
                $em->flush();

                $this->addFlash('success', 'Offre ajoutée avec succès.');

                return $this->redirectToRoute('app_admin_offres', [
                    'offre' => $offre->getIdOffre(),
                ]);
            }

            if ($action === 'update_offre') {
                $idOffre = $request->request->get('id_offre');
                $offre = $offreRepository->find($idOffre);

                if (!$offre) {
                    $this->addFlash('danger', 'Offre introuvable.');
                    return $this->redirectToRoute('app_admin_offres');
                }

                if ($currentUserId && $offre->getIdUtilisateur() !== $currentUserId) {
                    $this->addFlash('danger', 'Vous ne pouvez modifier que vos propres offres.');

                    return $this->redirectToRoute('app_admin_offres', [
                        'offre' => $offre->getIdOffre(),
                    ]);
                }

                $offre->setTitre($request->request->get('titre'));
                $offre->setDescription($request->request->get('description'));
                $offre->setTypeOffre($request->request->get('type_offre'));
                $offre->setPrix($this->formatDecimal($request->request->get('prix')));
                $offre->setBudget($this->formatDecimal($request->request->get('budget')));
                $offre->setDuree((int) $request->request->get('duree'));
                $offre->setStatut($request->request->get('statut', 'ouverte'));
                $offre->setLocalisation($request->request->get('localisation'));
                $offre->setTags($request->request->get('tags'));

                if ($request->request->get('date_limite')) {
                    $offre->setDateLimite(new \DateTime($request->request->get('date_limite')));
                } else {
                    $offre->setDateLimite(null);
                }

                $em->flush();

                $this->addFlash('success', 'Offre modifiée avec succès.');

                return $this->redirectToRoute('app_admin_offres', [
                    'offre' => $offre->getIdOffre(),
                ]);
            }

            if ($action === 'delete_offre') {
                $idOffre = $request->request->get('id_offre');
                $offre = $offreRepository->find($idOffre);

                if ($offre) {
                    /*
                     * Suppression sécurisée des données liées.
                     * Utile si ta base n'a pas ON DELETE CASCADE partout.
                     */

                    $likes = $offreLikeRepository->findBy([
                        'idOffre' => $offre->getIdOffre(),
                    ]);

                    foreach ($likes as $like) {
                        $em->remove($like);
                    }

                    $commentaires = $commentaireRepository->findBy([
                        'idOffre' => $offre->getIdOffre(),
                    ]);

                    foreach ($commentaires as $commentaire) {
                        $em->remove($commentaire);
                    }

                    $candidatures = $candidatureRepository->findBy([
                        'offre' => $offre,
                    ]);

                    foreach ($candidatures as $candidature) {
                        $em->remove($candidature);
                    }

                    $em->remove($offre);
                    $em->flush();

                    $this->addFlash('success', 'Offre supprimée avec succès.');
                }

                return $this->redirectToRoute('app_admin_offres');
            }

            if (in_array($action, ['accept_candidature', 'refuse_candidature', 'note_candidature'], true)) {
                $idCandidature = $request->request->get('id_candidature');
                $candidature = $candidatureRepository->find($idCandidature);

                if (!$candidature) {
                    $this->addFlash('danger', 'Candidature introuvable.');
                    return $this->redirectToRoute('app_admin_offres');
                }

                if ($action === 'accept_candidature') {
                    $candidature->setStatut('acceptee');
                    $candidature->setIsActive(true);
                    $this->addFlash('success', 'Candidature acceptée.');
                }

                if ($action === 'refuse_candidature') {
                    $candidature->setStatut('refusee');
                    $candidature->setIsActive(false);
                    $this->addFlash('success', 'Candidature refusée.');
                }

                if ($action === 'note_candidature') {
                    $candidature->setNoteRecruteur($request->request->get('note_recruteur'));
                    $this->addFlash('success', 'Note enregistrée.');
                }

                $em->flush();

                return $this->redirectToRoute('app_admin_offres', [
                    'offre' => $request->request->get('selected_offre'),
                ]);
            }

            if ($action === 'delete_comment') {
                $idCommentaire = $request->request->get('id_commentaire');
                $commentaire = $commentaireRepository->find($idCommentaire);

                if ($commentaire) {
                    $selectedOffreId = $commentaire->getIdOffre();

                    $em->remove($commentaire);
                    $em->flush();

                    $this->addFlash('success', 'Commentaire supprimé.');

                    return $this->redirectToRoute('app_admin_offres', [
                        'offre' => $selectedOffreId,
                    ]);
                }

                return $this->redirectToRoute('app_admin_offres', [
                    'offre' => $request->request->get('selected_offre'),
                ]);
            }
        }

        $search = $request->query->get('q', '');
        $type = $request->query->get('type', 'Tous');

        $qb = $offreRepository->createQueryBuilder('o');

        if ($search) {
            $qb->andWhere('o.titre LIKE :search OR o.description LIKE :search OR o.matricule LIKE :search')
                ->setParameter('search', '%' . $search . '%');
        }

        if ($type && $type !== 'Tous') {
            $qb->andWhere('o.typeOffre = :type')
                ->setParameter('type', $type);
        }

        $offres = $qb
            ->orderBy('o.datePublication', 'DESC')
            ->getQuery()
            ->getResult();

        /*
         * Ici on calcule les vrais nombres depuis les tables :
         * - offre_like pour les likes
         * - commentaire pour les commentaires
         * Ne pas utiliser getNbLikes() et getNbCommentaires()
         * parce que dans ton Entity Offre ils retournent toujours 0.
         */
        $offresData = [];

        foreach ($offres as $offre) {
            $offresData[] = [
                'offre' => $offre,
                'nbLikes' => $offreLikeRepository->count([
                    'idOffre' => $offre->getIdOffre(),
                ]),
                'nbCommentaires' => $commentaireRepository->count([
                    'idOffre' => $offre->getIdOffre(),
                ]),
            ];
        }

        $selectedOffre = null;
        $candidatures = [];
        $commentaires = [];

        $selectedOffreId = $request->query->get('offre') ?: $request->request->get('selected_offre');

        if ($selectedOffreId) {
            $selectedOffre = $offreRepository->find($selectedOffreId);

            if ($selectedOffre) {
                $candidatures = $candidatureRepository->findBy(
                    ['offre' => $selectedOffre],
                    ['dateCandidature' => 'DESC']
                );

                $commentairesEntity = $commentaireRepository->findBy(
                    ['idOffre' => $selectedOffre->getIdOffre()],
                    ['dateCommentaire' => 'DESC']
                );

                $commentairesData = [];

                foreach ($commentairesEntity as $commentaire) {
                    $utilisateur = $utilisateurRepository->find($commentaire->getIdUtilisateur());

                    $commentairesData[] = [
                        'commentaire' => $commentaire,
                        'utilisateur' => $utilisateur,
                    ];
                }

                $commentaires = $commentairesData;
            }
        }

        /*
         * Statistiques globales depuis les vraies tables.
         */
        $stats = [
            'total' => $offreRepository->count([]),
            'ouvertes' => $offreRepository->count([
                'statut' => 'ouverte',
            ]),
            'likes' => $offreLikeRepository->count([]),
            'comments' => $commentaireRepository->count([]),
        ];

        return $this->render('Offres/back/index.html.twig', [
            'offres' => $offresData,
            'selectedOffre' => $selectedOffre,
            'candidatures' => $candidatures,
            'commentaires' => $commentaires,
            'stats' => $stats,
            'search' => $search,
            'type' => $type,
            'currentUserId' => $currentUserId,
            'currentUserEmail' => $currentUserEmail,
        ]);
    }

    private function formatDecimal(?string $value): string
    {
        if ($value === null || trim($value) === '') {
            return '0.00';
        }

        $value = str_replace(',', '.', $value);

        return number_format((float) $value, 2, '.', '');
    }
}