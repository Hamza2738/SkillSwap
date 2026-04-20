<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
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

#[Route('/admin/profil')]
class ProfilAdminController extends AbstractController
{
    #[Route('', name: 'app_admin_profil', methods: ['GET'])]
    public function index(
        Request $request,
        UtilisateurRepository $utilisateurRepository
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
                ->setParameter('q', '%' . $q . '%')
                ->setMaxResults(1)
                ->getQuery()
                ->getOneOrNullResult();

            if ($found instanceof Utilisateur) {
                $profilAffiche = $found;
            } else {
                $this->addFlash('error', 'Aucun utilisateur trouvé pour : ' . $q);
            }
        }

        $contacts = $utilisateurRepository->createQueryBuilder('u')
            ->where('u.idUtilisateur != :id')
            ->setParameter('id', $admin->getIdUtilisateur())
            ->orderBy('u.idUtilisateur', 'DESC')
            ->setMaxResults(8)
            ->getQuery()
            ->getResult();

        return $this->render('utilisateur/ProfilAdmin.html.twig', [
            'admin' => $admin,
            'profil' => $profilAffiche,
            'contacts' => $contacts,
            'invitationsCount' => 0,
            'projetsChef' => [],
            'search' => $q,
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

        // Champs texte
        $admin->setNom(trim((string) $request->request->get('nom', '')));
        $admin->setPrenom(trim((string) $request->request->get('prenom', '')));
        $admin->setTelephone(trim((string) $request->request->get('telephone', '')));
        $admin->setBio(trim((string) $request->request->get('bio', '')));
        $admin->setLieu(trim((string) $request->request->get('lieu', '')));

        // Upload photo profil
        /** @var UploadedFile|null $photoProfil */
       $photoProfil = $request->files->get('photo_profil');

if ($photoProfil instanceof UploadedFile) {
    try {
        $newFileName = $this->uploadImage(
            $photoProfil,
            'uploads/utilisateur',
            'u_' . $admin->getIdUtilisateur()
        );

        $admin->setPhotoProfil('/uploads/utilisateur/' . $newFileName);
    } catch (\Throwable $e) {
        $this->addFlash('error', 'Erreur upload photo profil : ' . $e->getMessage());
        return $this->redirectToRoute('app_admin_profil');
    }
}

        // Upload photo couverture
        /** @var UploadedFile|null $photoCouverture */
        $photoCouverture = $request->files->get('photo_couverture');
        if ($photoCouverture instanceof UploadedFile) {
            try {
                $newFileName = $this->uploadImage(
                    $photoCouverture,
                    'view/image/utilisateur/uploads/covers',
                    'cover_' . $admin->getIdUtilisateur()
                );

                $admin->setPhotoCouverture('/view/image/utilisateur/uploads/covers/' . $newFileName);
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
            ? (string) $user->getPhotoProfil()
            : (string) $user->getPhotoCouverture();

        $filePath = $this->resolveStoredImagePath($dbPath);

        if ($filePath === null || !is_file($filePath)) {
            throw $this->createNotFoundException('Image introuvable. Valeur base: ' . $dbPath);
        }

        $response = new BinaryFileResponse($filePath);
        $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_INLINE);

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

        if (!in_array($file->getMimeType(), $allowedMimeTypes, true)) {
            throw new FileException('Format image non autorisé.');
        }

        $extension = $file->guessExtension() ?: 'png';
        $fileName = $prefix . '_' . uniqid('', true) . '.' . $extension;

        $targetDir = $this->getParameter('kernel.project_dir') . '/public/' . trim($relativeDirectory, '/');

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

        // Cas ancien chemin absolu Windows/Linux
        if (
            preg_match('/^[A-Za-z]:\\\\/', $dbPath) ||
            preg_match('/^[A-Za-z]:\//', $dbPath) ||
            str_starts_with($dbPath, DIRECTORY_SEPARATOR)
        ) {
            return is_file($dbPath) ? $dbPath : null;
        }

        // Cas chemin relatif web : /view/image/utilisateur/uploads/xxx.png
        $clean = ltrim(str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $dbPath), DIRECTORY_SEPARATOR);

        $fullPath = $this->getParameter('kernel.project_dir') . '/public/' . $clean;

        return is_file($fullPath) ? $fullPath : null;
    }
    
}