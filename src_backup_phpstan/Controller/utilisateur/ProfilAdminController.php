<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Entity\suiviTache\Projet;
use App\Entity\suiviTache\Tache;
use App\Entity\RendezVous\RendezVous;
use App\Entity\RendezVous\Postulation;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\ResponseHeaderBag;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\JsonResponse;
use App\Repository\ConnexionRepository;

#[Route('/admin/profil')]
class ProfilAdminController extends AbstractController
{
    #[Route('', name: 'app_admin_profil', methods: ['GET'])]
    public function index(
    Request $request,
    UtilisateurRepository $utilisateurRepository,
    ConnexionRepository $connexionRepository,
    EntityManagerInterface $em
): Response {
        /** @var Utilisateur|null $admin */
        $admin = $this->getUser();

        if (!$admin instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $q = trim((string) $request->query->get('q', ''));
        $profilAffiche = $admin;

        if ($q !== '') {
            $found = $utilisateurRepository->createQueryBuilder('u')
                ->where('LOWER(u.nom) LIKE LOWER(:q)')
                ->orWhere('LOWER(u.prenom) LIKE LOWER(:q)')
                ->orWhere('LOWER(u.email) LIKE LOWER(:q)')
                ->setParameter('q', '%' . mb_strtolower($q) . '%')
                ->setMaxResults(1)
                ->getQuery()
                ->getOneOrNullResult();

            if ($found instanceof Utilisateur) {
                $profilAffiche = $found;
            } else {
                $this->addFlash('error', 'Aucun utilisateur trouvé pour : ' . $q);
            }
        }
        $isVisiting = $profilAffiche->getIdUtilisateur() !== $admin->getIdUtilisateur();

$relation = null;
$relationStatut = null;
$estMoiDemandeur = false;

if ($isVisiting) {
    $relation = $connexionRepository->findRelation($admin, $profilAffiche);

    if ($relation) {
        $relationStatut = $relation->getStatut();

        $estMoiDemandeur =
            $relation->getDemandeur()
            && $relation->getDemandeur()->getIdUtilisateur() === $admin->getIdUtilisateur();
    }
}

        // Invitations reçues par admin
$invitations = $connexionRepository->getInvitationsRecues($admin);

// Contacts acceptés seulement
$contactsRelations = $connexionRepository->getContacts($admin);

$contacts = [];

foreach ($contactsRelations as $connexion) {
    $contact = null;

    if (
        $connexion->getDemandeur()
        && $connexion->getDemandeur()->getIdUtilisateur() !== $admin->getIdUtilisateur()
    ) {
        $contact = $connexion->getDemandeur();
    }

    if (
        $connexion->getReceveur()
        && $connexion->getReceveur()->getIdUtilisateur() !== $admin->getIdUtilisateur()
    ) {
        $contact = $connexion->getReceveur();
    }

    if ($contact instanceof Utilisateur) {
        $contacts[] = $contact;
    }
}

        $profilUserId = (int) $profilAffiche->getIdUtilisateur();
        $profilEmail = strtolower((string) $profilAffiche->getEmail());

        $projetsChef = $em->getRepository(Projet::class)
            ->createQueryBuilder('p')
            ->where('p.userId = :uid')
            ->orWhere('LOWER(p.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('p.id', 'DESC')
            ->getQuery()
            ->getResult();

        $projetIds = array_map(static fn (Projet $p) => $p->getId(), $projetsChef);

        $tacheQb = $em->getRepository(Tache::class)
            ->createQueryBuilder('t')
            ->where('t.userId = :uid')
            ->orWhere('LOWER(t.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('t.id', 'DESC');

        if (!empty($projetIds)) {
            $tacheQb
                ->orWhere('t.projetId IN (:projetIds)')
                ->setParameter('projetIds', $projetIds);
        }

        $tachesActivite = $tacheQb->getQuery()->getResult();

        $rdvActivite = $em->getRepository(RendezVous::class)
            ->createQueryBuilder('r')
            ->where('r.idAdminCreateur = :uid')
            ->orWhere('LOWER(r.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('r.dateRendezVous', 'DESC')
            ->getQuery()
            ->getResult();

        $postulationsActivite = $em->getRepository(Postulation::class)
            ->createQueryBuilder('p')
            ->where('p.idPostulant = :uid')
            ->orWhere('LOWER(p.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('p.datePostulation', 'DESC')
            ->getQuery()
            ->getResult();

        return $this->render('utilisateur/ProfilAdmin.html.twig', [
    'admin' => $admin,
    'profil' => $profilAffiche,
    'search' => $q,

    'is_visiting' => $isVisiting,
    'relation' => $relation,
    'relationStatut' => $relationStatut,
    'estMoiDemandeur' => $estMoiDemandeur,

    'invitations' => $invitations,
    'invitationsCount' => count($invitations),
    'contacts' => $contacts,

    'projetsChef' => $projetsChef,
    'tachesActivite' => $tachesActivite,
    'rdvActivite' => $rdvActivite,
    'postulationsActivite' => $postulationsActivite,
]);
    }

    #[Route('/update', name: 'app_admin_profil_update', methods: ['POST'])]
    public function update(Request $request, EntityManagerInterface $em): Response
    {
        /** @var Utilisateur|null $admin */
        $admin = $this->getUser();

        if (!$admin instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $admin->setNom(trim((string) $request->request->get('nom', '')));
        $admin->setPrenom(trim((string) $request->request->get('prenom', '')));
        $admin->setTelephone(trim((string) $request->request->get('telephone', '')));
        $admin->setBio(trim((string) $request->request->get('bio', '')));
        $admin->setLieu(trim((string) $request->request->get('lieu', '')));

        /** @var UploadedFile|null $photoProfil */
        $photoProfil = $request->files->get('photo_profil');

        if ($photoProfil instanceof UploadedFile) {
            try {
                $newFileName = $this->uploadImage(
                    $photoProfil,
                    'uploads/utilisateur',
                    'u_' . $admin->getIdUtilisateur()
                );

                // Nouveau format propre pour Symfony + JavaFX
                $admin->setPhotoProfil('/uploads/utilisateur/' . $newFileName);
            } catch (\Throwable $e) {
                $this->addFlash('error', 'Erreur upload photo profil : ' . $e->getMessage());
                return $this->redirectToRoute('app_admin_profil');
            }
        }

        /** @var UploadedFile|null $photoCouverture */
        $photoCouverture = $request->files->get('photo_couverture');

        if ($photoCouverture instanceof UploadedFile) {
            try {
                $newFileName = $this->uploadImage(
                    $photoCouverture,
                    'uploads/utilisateur/covers',
                    'cover_' . $admin->getIdUtilisateur()
                );

                // Nouveau format propre pour Symfony + JavaFX
                $admin->setPhotoCouverture('/uploads/utilisateur/covers/' . $newFileName);
            } catch (\Throwable $e) {
                $this->addFlash('error', 'Erreur upload couverture : ' . $e->getMessage());
                return $this->redirectToRoute('app_admin_profil');
            }
        }

        $em->persist($admin);
        $em->flush();

        $this->addFlash('success', 'Profil mis à jour avec succès.');

        return $this->redirectToRoute('app_admin_profil');
    }

    #[Route('/delete', name: 'app_admin_profil_delete', methods: ['POST'])]
    public function delete(EntityManagerInterface $em): Response
    {
        /** @var Utilisateur|null $admin */
        $admin = $this->getUser();

        if (!$admin instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $em->remove($admin);
        $em->flush();

        $this->addFlash('success', 'Compte supprimé avec succès.');

        return $this->redirectToRoute('app_logout');
    }

    #[Route('/image/{id}/{type}', name: 'app_admin_profil_image', methods: ['GET'])]
    public function image(
        int $id,
        string $type,
        UtilisateurRepository $utilisateurRepository
    ): Response {
        $user = $utilisateurRepository->find($id);

        if (!$user instanceof Utilisateur) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        if (!in_array($type, ['profil', 'couverture'], true)) {
            throw $this->createNotFoundException('Type image invalide.');
        }

        $dbPath = $type === 'profil'
            ? $user->getPhotoProfil()
            : $user->getPhotoCouverture();

        $filePath = $this->resolveStoredImagePath($dbPath);

        if ($filePath === null || !is_file($filePath)) {
            throw $this->createNotFoundException('Image introuvable. Valeur base : ' . (string) $dbPath);
        }

        $response = new BinaryFileResponse($filePath);
        $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_INLINE);

        $mimeType = @mime_content_type($filePath);
        if ($mimeType) {
            $response->headers->set('Content-Type', $mimeType);
        }

        $response->headers->set('Cache-Control', 'public, max-age=3600');

        return $response;
    }

    private function uploadImage(
        UploadedFile $file,
        string $relativeDirectory,
        string $prefix
    ): string {
        $allowedMimeTypes = [
            'image/jpeg',
            'image/png',
            'image/webp',
            'image/gif',
            'image/jpg',
        ];

        if (!in_array((string) $file->getMimeType(), $allowedMimeTypes, true)) {
            throw new FileException('Format image non autorisé.');
        }

        $extension = $file->guessExtension() ?: $file->getClientOriginalExtension() ?: 'png';
        $extension = strtolower($extension);

        $fileName = $prefix . '_' . uniqid('', true) . '.' . $extension;

        $projectDir = $this->getParameter('kernel.project_dir');
        assert(is_string($projectDir));

        $targetDir = $projectDir . '/public/' . trim($relativeDirectory, '/');

        if (!is_dir($targetDir) && !mkdir($targetDir, 0777, true) && !is_dir($targetDir)) {
            throw new FileException('Impossible de créer le dossier : ' . $targetDir);
        }

        $file->move($targetDir, $fileName);

        return $fileName;
    }

    private function resolveStoredImagePath(?string $dbPath): ?string
    {
        $dbPath = trim((string) $dbPath);

        if ($dbPath === '' || strtoupper($dbPath) === 'NULL') {
            return null;
        }

        $projectDir = $this->getParameter('kernel.project_dir');
        assert(is_string($projectDir));

        $candidates = [];

        // Nettoyer les slashs mais garder valeur originale aussi
        $normalized = str_replace('\\', '/', $dbPath);

        // 1) Chemin absolu Windows ou Linux
        // Exemple : C:\Users\hamza\Documents\SkillSwap\uploads\covers\...
        // Exemple : C:/Users/hamza/Documents/SkillSwap/uploads/covers/...
        if (
            preg_match('/^[A-Za-z]:[\/\\\\]/', $dbPath) ||
            str_starts_with($dbPath, '/home/') ||
            str_starts_with($dbPath, '/var/') ||
            str_starts_with($dbPath, '/mnt/')
        ) {
            $candidates[] = $dbPath;
            $candidates[] = $normalized;
        }

        // 2) Nouveau chemin Symfony : /uploads/...
        // Fichier réel : symfony/public/uploads/...
        if (str_starts_with($normalized, '/uploads/') || str_starts_with($normalized, 'uploads/')) {
            $relative = ltrim($normalized, '/');
            $candidates[] = $projectDir . '/public/' . $relative;
        }

        // 3) Ancien chemin JavaFX : /view/image/...
        // Possibilité A : fichier copié dans symfony/public/view/...
        // Possibilité B : fichier reste dans javafx/src/main/resources/view/...
        if (str_starts_with($normalized, '/view/') || str_starts_with($normalized, 'view/')) {
            $relative = ltrim($normalized, '/');

            $candidates[] = $projectDir . '/public/' . $relative;

            // Chemin JavaFX possible 1
            $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/javafx/src/main/resources/' . $relative;

            // Chemin JavaFX possible 2 si ton dossier s'appelle autrement
            $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/JavaFX/src/main/resources/' . $relative;
        }

        // 4) Ancien dossier covers dans Documents
        // Si la base contient seulement le nom ou une partie du path
        if (str_contains($normalized, 'SkillSwap/uploads/covers/')) {
            $fileName = basename($normalized);
            $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/covers/' . $fileName;
        }

        // 5) Cas où la base contient un chemin relatif sans slash
        // Exemple : uploads/utilisateur/u_1.png
        // Exemple : view/image/utilisateur/uploads/u_1.png
        $relativeClean = ltrim($normalized, '/');
        $candidates[] = $projectDir . '/public/' . $relativeClean;

        // 6) Cas sécurité : chercher aussi dans Symfony uploads par nom de fichier
        $baseName = basename($normalized);
        if ($baseName && $baseName !== '.' && $baseName !== '/') {
            $candidates[] = $projectDir . '/public/uploads/utilisateur/' . $baseName;
            $candidates[] = $projectDir . '/public/uploads/utilisateur/covers/' . $baseName;
            $candidates[] = $projectDir . '/public/uploads/utilisateur/cover/' . $baseName;
            $candidates[] = $projectDir . '/public/view/image/utilisateur/uploads/' . $baseName;
            $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/javafx/src/main/resources/view/image/utilisateur/uploads/' . $baseName;
            $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/covers/' . $baseName;
        }

        foreach ($candidates as $candidate) {
            if (!$candidate) {
                continue;
            }

            $candidate = str_replace('\\', '/', $candidate);

            if (is_file($candidate)) {
                return $candidate;
            }
        }

        return null;
    }




#[Route('/admin/profil/search-users', name: 'app_admin_search_users', methods: ['GET'])]
public function searchUsers(
    Request $request,
    UtilisateurRepository $utilisateurRepository
): JsonResponse {
    $q = trim((string) $request->query->get('q', ''));

    if ($q === '') {
        return $this->json([]);
    }

    $qLower = mb_strtolower($q);

    $users = $utilisateurRepository->createQueryBuilder('u')
        ->where('LOWER(u.nom) LIKE :starts')
        ->orWhere('LOWER(u.prenom) LIKE :starts')
        ->orWhere('LOWER(CONCAT(u.nom, \' \', u.prenom)) LIKE :starts')
        ->orWhere('LOWER(CONCAT(u.prenom, \' \', u.nom)) LIKE :starts')
        ->setParameter('starts', $qLower . '%')
        ->orderBy('u.nom', 'ASC')
        ->setMaxResults(8)
        ->getQuery()
        ->getResult();

    $data = [];

    foreach ($users as $user) {
        $data[] = [
            'id' => $user->getIdUtilisateur(),
            'nom' => $user->getNom(),
            'prenom' => $user->getPrenom(),
            'email' => $user->getEmail(),
            'role' => $user->getRole(),
            'photoUrl' => $user->getPhotoProfil()
                ? $this->generateUrl('app_admin_profil_image', [
                    'id' => $user->getIdUtilisateur(),
                    'type' => 'profil',
                ])
                : null,
            'profileUrl' => $this->generateUrl('app_admin_profil', [
                'q' => $user->getEmail(),
            ]),
        ];
    }

    return $this->json($data);
}
}