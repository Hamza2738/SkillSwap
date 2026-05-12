<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Entity\suiviTache\Projet;
use App\Entity\suiviTache\Tache;
use App\Entity\RendezVous\RendezVous;
use App\Entity\RendezVous\Postulation;
use App\Repository\UtilisateurRepository;
use App\Repository\CompetenceRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\ConnexionRepository;
use App\Repository\MessageRepository;
use App\Entity\utilisateur\Connexion;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\ResponseHeaderBag;
use Symfony\Component\HttpFoundation\JsonResponse;
#[Route('/profil')]
class ProfilController extends AbstractController
{
    #[Route('', name: 'app_profil', methods: ['GET'])]
    public function index(
        Request $request,
        UtilisateurRepository $utilisateurRepository,
        ConnexionRepository $connexionRepository,
MessageRepository $messageRepository,
        CompetenceRepository $competenceRepository,
        EntityManagerInterface $em
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
                ->setParameter('q', '%' . mb_strtolower($q) . '%')
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

        // =====================================================
        // ACTIVITÉ PROFIL FRONT
        // Même logique JavaFX :
        // - Mes tâches de projet
        // - Mes rendez-vous
        // - Mes postulations
        // =====================================================

        $profilUserId = (int) $profilAffiche->getIdUtilisateur();
        $profilEmail = strtolower((string) $profilAffiche->getEmail());

        // ================= PROJETS CHEF =================
        $projetsChef = $em->getRepository(Projet::class)
            ->createQueryBuilder('p')
            ->where('p.userId = :uid')
            ->orWhere('LOWER(p.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('p.id', 'DESC')
            ->getQuery()
            ->getResult();

        $projetIds = array_map(
            fn (Projet $projet) => $projet->getId(),
            $projetsChef
        );

        // ================= MES TÂCHES =================
        // tâche créée par moi OU mail_user = mon email OU tâche dans mes projets
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

        $taches = $tacheQb->getQuery()->getResult();

        // ================= MES RENDEZ-VOUS =================
        $rendezVous = $em->getRepository(RendezVous::class)
            ->createQueryBuilder('r')
            ->where('r.idAdminCreateur = :uid')
            ->orWhere('LOWER(r.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('r.dateRendezVous', 'DESC')
            ->getQuery()
            ->getResult();

        // ================= MES POSTULATIONS =================
        $postulations = $em->getRepository(Postulation::class)
            ->createQueryBuilder('p')
            ->where('p.idPostulant = :uid')
            ->orWhere('LOWER(p.mailUser) = :email')
            ->setParameter('uid', $profilUserId)
            ->setParameter('email', $profilEmail)
            ->orderBy('p.datePostulation', 'DESC')
            ->getQuery()
            ->getResult();
            $relation = null;
$relationStatut = null;
$estMoiDemandeur = false;

if ($profilAffiche->getIdUtilisateur() !== $user->getIdUtilisateur()) {
    $relation = $connexionRepository->findRelation($user, $profilAffiche);

    if ($relation instanceof Connexion) {
        $relationStatut = $relation->getStatut();
        $estMoiDemandeur = $relation->getDemandeur()?->getIdUtilisateur() === $user->getIdUtilisateur();
    }
}

$invitations = $connexionRepository->getInvitationsRecues($user);
$contacts = $connexionRepository->getContactUsers($user);

$contactsWithUnread = [];

foreach ($contacts as $contact) {
    $contactsWithUnread[] = [
        'user' => $contact,
        'nonLus' => $messageRepository->countNonLus($contact, $user),
    ];
}

        return $this->render('utilisateur/profil.html.twig', [
            'user'             => $user,
            'profil'           => $profilAffiche,
            'competences'      => $competences,
            'recentUsers'      => $recentUsers,

            // Sidebar / activité
            'contacts' => $contactsWithUnread,
'invitations' => $invitations,
'invitationsCount' => count($invitations),
'relation' => $relation,
'relationStatut' => $relationStatut,
'estMoiDemandeur' => $estMoiDemandeur,
            'projetsChef'      => $projetsChef,
            'taches'           => $taches,
            'rendezVous'       => $rendezVous,
            'postulations'     => $postulations,

            // Alias si ton Twig utilise les mêmes noms que ProfilAdmin
            'tachesActivite'           => $taches,
            'rdvActivite'              => $rendezVous,
            'postulationsActivite'     => $postulations,

            'search'           => $q,
            'is_visiting'      => $profilAffiche->getIdUtilisateur() !== $user->getIdUtilisateur(),
        ]);
    }

  #[Route('/update', name: 'app_profil_update', methods: ['POST'])]
public function update(Request $request, EntityManagerInterface $em): Response
{
    /** @var Utilisateur|null $user */
    $user = $this->getUser();

    if (!$user instanceof Utilisateur) {
        return $this->redirectToRoute('app_login');
    }

    $user->setNom(trim((string) $request->request->get('nom', $user->getNom())));
    $user->setPrenom(trim((string) $request->request->get('prenom', $user->getPrenom())));
    $user->setTelephone(trim((string) $request->request->get('telephone', $user->getTelephone())));
    $user->setBio(trim((string) $request->request->get('bio', $user->getBio())));
    $user->setLieu(trim((string) $request->request->get('lieu', $user->getLieu())));

    $baseUploadDir = $this->getParameter('kernel.project_dir');
    assert(is_string($baseUploadDir));

    $baseUploadDir = $baseUploadDir . '/public/uploads/utilisateur';
    $profilDir = $baseUploadDir . '/profil';
    $coverDir  = $baseUploadDir . '/cover';

    if (!is_dir($profilDir)) {
        mkdir($profilDir, 0777, true);
    }

    if (!is_dir($coverDir)) {
        mkdir($coverDir, 0777, true);
    }

    $photoProfil = $request->files->get('photo_profil');

    if ($photoProfil) {
        $ext = $photoProfil->guessExtension() ?: 'jpg';
        $fileName = 'profil_' . $user->getIdUtilisateur() . '_' . uniqid('', true) . '.' . $ext;

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
        $fileName = 'cover_' . $user->getIdUtilisateur() . '_' . uniqid('', true) . '.' . $ext;

        try {
            $photoCouverture->move($coverDir, $fileName);
            $user->setPhotoCouverture('/uploads/utilisateur/cover/' . $fileName);
        } catch (FileException $e) {
            $this->addFlash('error', 'Erreur lors de l’upload de la photo de couverture.');
            return $this->redirectToRoute('app_profil');
        }
    }

    $em->persist($user);
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
#[Route('/profil/image/{id}/{type}', name: 'app_profil_image', methods: ['GET'])]
public function image(
    int $id,
    string $type,
    \App\Repository\UtilisateurRepository $utilisateurRepository
): Response {
    $user = $utilisateurRepository->find($id);

    if (!$user instanceof \App\Entity\utilisateur\Utilisateur) {
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

private function resolveStoredImagePath(?string $dbPath): ?string
{
    $dbPath = trim((string) $dbPath);

    if ($dbPath === '' || strtoupper($dbPath) === 'NULL') {
        return null;
    }

    $projectDir = $this->getParameter('kernel.project_dir');
    assert(is_string($projectDir));

    $candidates = [];
    $normalized = str_replace('\\', '/', $dbPath);

    // 1) Chemin absolu Windows ou Linux
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
    if (str_starts_with($normalized, '/uploads/') || str_starts_with($normalized, 'uploads/')) {
        $relative = ltrim($normalized, '/');
        $candidates[] = $projectDir . '/public/' . $relative;
    }

    // 3) Ancien chemin JavaFX : /view/image/...
    if (str_starts_with($normalized, '/view/') || str_starts_with($normalized, 'view/')) {
        $relative = ltrim($normalized, '/');

        $candidates[] = $projectDir . '/public/' . $relative;
        $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/javafx/src/main/resources/' . $relative;
        $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/JavaFX/src/main/resources/' . $relative;
    }

    // 4) Ancien dossier covers dans Documents
    if (str_contains($normalized, 'SkillSwap/uploads/covers/')) {
        $fileName = basename($normalized);
        $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/covers/' . $fileName;
    }

    // 5) Chemin relatif
    $relativeClean = ltrim($normalized, '/');
    $candidates[] = $projectDir . '/public/' . $relativeClean;

    // 6) Recherche par nom fichier
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




#[Route('/profil/search-users', name: 'app_profil_search_users', methods: ['GET'])]
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
                ? $this->generateUrl('app_profil_image', [
                    'id' => $user->getIdUtilisateur(),
                    'type' => 'profil',
                ])
                : null,
            'profileUrl' => $this->generateUrl('app_profil', [
                'q' => $user->getEmail(),
            ]),
        ];
    }

    return $this->json($data);
}

    
}