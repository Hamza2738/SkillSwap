<?php

namespace App\Controller\Offres\front;

use App\Entity\Offres\Banque;
use App\Entity\Offres\Candidature;
use App\Repository\OffreRepository;
use App\Repository\CandidatureRepository;
use App\Service\offres\BanqueService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;


class BanqueController extends AbstractController
{
    #[Route('/offres/{id}/banque', name: 'app_offre_banque', methods: ['GET', 'POST'])]
    public function paiement(
        int $id,
        Request $request,
        OffreRepository $offreRepository,
        CandidatureRepository $candidatureRepository,
        BanqueService $banqueService,
        EntityManagerInterface $entityManager
    ): Response {
        $offre = $offreRepository->find($id);

        if (!$offre) {
            throw $this->createNotFoundException('Offre introuvable.');
        }

        if ($request->isMethod('POST')) {
            $user = $this->getUser();

            if (!$user) {
                $this->addFlash('error', 'Vous devez être connecté pour confirmer le paiement.');
                return $this->redirectToRoute('app_login');
            }

            /*
             * 1) Enregistrer le paiement dans la table banque
             */
            $banque = new Banque();

            $cardNumber = $banqueService->nettoyerNumeroCarte(
                $request->request->get('card_number', '')
            );

            $referencePaiement = $banqueService->genererOrderNumber();

            $banque->setOffre($offre);
            $banque->setCardNumber($cardNumber);
            $banque->setCardHolder($request->request->get('card_holder'));
            $banque->setCvv($request->request->get('cvv'));
            $banque->setExpiryMonth($request->request->get('expiry_month'));
            $banque->setExpiryYear($request->request->get('expiry_year'));
            $banque->setDynamicPassword($request->request->get('dynamic_password'));

            $banque->setCompany($request->request->get('company', 'SkillSwap'));
            $banque->setOrderNumber($referencePaiement);
            $banque->setProduct($offre->getTitre());
            $banque->setAmount((string) $offre->getPrix());
            $banque->setCurrency('TND');
            $banque->setVatRate('20.00');

            $banqueService->payer($banque);

            /*
             * 2) Vérifier si l'utilisateur est déjà candidat à cette offre
             */
            $ancienneCandidature = $candidatureRepository->findOneBy([
                'offre' => $offre,
                'utilisateur' => $user,
            ]);

            if ($ancienneCandidature) {
                $this->addFlash('success', '✅ Paiement confirmé. Vous êtes déjà candidat à cette offre.');
                return $this->redirect('/offres/front');
            }

            /*
             * 3) Créer une candidature après paiement confirmé
             */
            $candidature = new Candidature();

            $now = new \DateTime();

            $duree = $offre->getDuree();
            if (!$duree || $duree <= 0) {
                $duree = 30;
            }

            $dateExpiration = new \DateTime();
            $dateExpiration->modify('+' . $duree . ' days');

            $candidature->setDateCandidature($now);
            $candidature->setDateAbonnement($now);
            $candidature->setDateExpiration($dateExpiration);

            if (method_exists($offre, 'getMatricule') && $offre->getMatricule()) {
                $candidature->setMatriculeOffre($offre->getMatricule());
            } else {
                $candidature->setMatriculeOffre('OFFRE-' . $offre->getIdOffre());
            }

            if (method_exists($user, 'getEmail')) {
                $candidature->setMailAcheteur($user->getEmail());
            } else {
                $candidature->setMailAcheteur($user->getUserIdentifier());
            }

            $candidature->setPrixPaye($offre->getPrix());
            $candidature->setModePaiement($request->request->get('mode_paiement', 'carte'));
            $candidature->setReferencePaiement($referencePaiement);

            $candidature->setStatut('payee');
            $candidature->setMessage($request->request->get('message'));
            $candidature->setLettreMotivation($request->request->get('lettre_motivation'));
            $candidature->setCv(null);

            $candidature->setNoteRecruteur(null);
            $candidature->setNoteCandidat(null);
            $candidature->setIsActive(true);

            $candidature->setUtilisateur($user);
            $candidature->setOffre($offre);

            $entityManager->persist($candidature);
            $entityManager->flush();

            $this->addFlash('success', '✅ Paiement confirmé avec succès. Vous êtes maintenant candidat à cette offre.');

            return $this->redirect('/offres/front');
        }

        return $this->render('Offres/front/banque.html.twig', [
            'offre' => $offre,
        ]);
    }
}