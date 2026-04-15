<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Repository\UtilisateurRepository;
use App\Repository\CompetenceRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/profil')]
class ProfilController extends AbstractController
{
    #[Route('', name: 'app_profil', methods: ['GET'])]
    public function index(
        Request $request,
        UtilisateurRepository $utilisateurRepository,
        CompetenceRepository $competenceRepository
    ): Response {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $q = trim((string) $request->query->get('q', ''));
        $profilAffiche = $user;

        if ($q !== '') {
            $qb = $utilisateurRepository->createQueryBuilder('u');
            $qb
                ->where('LOWER(u.nom) LIKE LOWER(:q)')
                ->orWhere('LOWER(u.prenom) LIKE LOWER(:q)')
                ->orWhere('LOWER(u.email) LIKE LOWER(:q)')
                ->setParameter('q', '%' . $q . '%')
                ->setMaxResults(1);

            $found = $qb->getQuery()->getOneOrNullResult();
            if ($found instanceof Utilisateur) {
                $profilAffiche = $found;
            } else {
                $this->addFlash('error', 'Aucun utilisateur trouvé pour : ' . $q);
            }
        }

        $competences = [];
        if ($profilAffiche->getEmail()) {
            $competences = $competenceRepository->findBy(
                ['email' => $profilAffiche->getEmail()],
                ['id' => 'ASC']
            );
        }

        $recentUsers = $utilisateurRepository->createQueryBuilder('u')
            ->where('u.idUtilisateur != :me')
            ->setParameter('me', $user->getIdUtilisateur())
            ->orderBy('u.idUtilisateur', 'DESC')
            ->setMaxResults(8)
            ->getQuery()
            ->getResult();

        return $this->render('utilisateur/profil.html.twig', [
            'user'             => $user,
            'profil'           => $profilAffiche,
            'competences'      => $competences,
            'recentUsers'      => $recentUsers,
            'contacts'         => [],
            'invitationsCount' => 0,
            'taches'           => [],
            'rendezVous'       => [],
            'postulations'     => [],
            'search'           => $q,
            'is_visiting'      => $profilAffiche->getIdUtilisateur() !== $user->getIdUtilisateur(),
        ]);
    }

    #[Route('/update', name: 'app_profil_update', methods: ['POST'])]
    public function update(Request $request, EntityManagerInterface $em): Response
    {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $user->setNom(trim((string) $request->request->get('nom', '')));
        $user->setPrenom(trim((string) $request->request->get('prenom', '')));
        $user->setTelephone(trim((string) $request->request->get('telephone', '')));
        $user->setBio(trim((string) $request->request->get('bio', '')));
        $user->setLieu(trim((string) $request->request->get('lieu', '')));

        $baseUploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/utilisateur';
        $profilDir = $baseUploadDir . '/profil';
        $coverDir  = $baseUploadDir . '/cover';

        if (!is_dir($profilDir)) {
            @mkdir($profilDir, 0777, true);
        }

        if (!is_dir($coverDir)) {
            @mkdir($coverDir, 0777, true);
        }

        $photoProfil = $request->files->get('photo_profil');
        if ($photoProfil) {
            $ext = $photoProfil->guessExtension() ?: 'jpg';
            $fileName = 'profil_' . $user->getIdUtilisateur() . '_' . uniqid() . '.' . $ext;

            try {
                $photoProfil->move($profilDir, $fileName);
                $user->setPhotoProfil('/uploads/utilisateur/profil/' . $fileName);
            } catch (FileException $e) {
                $this->addFlash('error', 'Erreur lors de l’upload de la photo de profil.');
                return $this->redirectToRoute('app_profil');
            }
        }

        $photoCouverture = $request->files->get('photo_couverture');
        if ($photoCouverture) {
            $ext = $photoCouverture->guessExtension() ?: 'jpg';
            $fileName = 'cover_' . $user->getIdUtilisateur() . '_' . uniqid() . '.' . $ext;

            try {
                $photoCouverture->move($coverDir, $fileName);
                $user->setPhotoCouverture('/uploads/utilisateur/cover/' . $fileName);
            } catch (FileException $e) {
                $this->addFlash('error', 'Erreur lors de l’upload de la photo de couverture.');
                return $this->redirectToRoute('app_profil');
            }
        }

        $em->flush();

        $this->addFlash('success', 'Profil mis à jour avec succès.');
        return $this->redirectToRoute('app_profil');
    }

    #[Route('/delete', name: 'app_profil_delete', methods: ['POST'])]
    public function deleteAccount(EntityManagerInterface $em): Response
    {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $em->remove($user);
        $em->flush();

        return $this->redirectToRoute('app_logout');
    }
}