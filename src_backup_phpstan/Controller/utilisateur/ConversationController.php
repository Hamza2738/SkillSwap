<?php



namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Message;
use App\Entity\utilisateur\Utilisateur;
use App\Repository\ConnexionRepository;
use App\Repository\MessageRepository;
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
#[Route('/conversation')]
class ConversationController extends AbstractController
{
    #[Route('/{id}', name: 'app_conversation_show', methods: ['GET', 'POST'])]
    public function show(
        int $id,
        Request $request,
        UtilisateurRepository $utilisateurRepository,
        ConnexionRepository $connexionRepository,
        MessageRepository $messageRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var Utilisateur|null $me */
        $me = $this->getUser();

        if (!$me instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $ami = $utilisateurRepository->find($id);

        if (!$ami instanceof Utilisateur) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        if ($ami->getIdUtilisateur() === $me->getIdUtilisateur()) {
            $this->addFlash('error', 'Impossible d’ouvrir une conversation avec vous-même.');
            return $this->redirectToRoute('app_profil');
        }

        $relation = $connexionRepository->findRelation($me, $ami);

        $userRole = strtoupper((string) $me->getRole());

        $isAdmin =
            in_array('ROLE_ADMIN', $me->getRoles(), true)
            || $userRole === 'ADMIN'
            || $userRole === 'ROLE_ADMIN';

        if (!$isAdmin && (!$relation || $relation->getStatut() !== 'accepte')) {
            $this->addFlash('error', 'Vous devez être connectés pour discuter.');

            return $this->redirectToRoute('app_profil', [
                'q' => $ami->getEmail(),
            ]);
        }

        if ($request->isMethod('POST')) {
            $contenu = trim((string) $request->request->get('contenu', ''));

            /** @var UploadedFile|null $media */
            $media = $request->files->get('media');

            $typeMessage = 'texte';
            $fichierPath = null;
            $fichierNom = null;
            $fichierTaille = 0;

            if ($media instanceof UploadedFile) {
                $mediaInfo = $this->uploadMessageMedia($media);

                $typeMessage = $mediaInfo['type'];
                $fichierPath = $mediaInfo['path'];
                $fichierNom = $mediaInfo['name'];
                $fichierTaille = $mediaInfo['size'];

                if ($contenu === '') {
                    $contenu = match ($typeMessage) {
                        'image' => '📷 Image',
                        'video' => '🎬 Vidéo',
                        'vocal' => '🎤 Message vocal',
                        default => '📎 Fichier',
                    };
                }
            }

            // Si pas de texte et pas de fichier, on n’envoie rien.
            if ($contenu !== '' || $media instanceof UploadedFile) {
                $message = new Message();
                $message->setEnvoyeur($me);
                $message->setReceveur($ami);
                $message->setContenu($contenu);
                $message->setTypeMessage($typeMessage);
                $message->setLu(false);
                $message->setDateEnvoi(new \DateTimeImmutable());

                if ($fichierPath !== null) {
                    $message->setFichierPath($fichierPath);
                    $message->setFichierNom($fichierNom);
                    $message->setFichierTaille($fichierTaille);
                }

                // Optionnel pour vocal si ton entity contient dureeVocal
                if ($typeMessage === 'vocal') {
                    $duree = (float) $request->request->get('duree_vocal', 0);
                    if (method_exists($message, 'setDureeVocal')) {
                        $message->setDureeVocal($duree);
                    }
                }

                $em->persist($message);
                $em->flush();
            }

            $params = [
                'id' => $ami->getIdUtilisateur(),
            ];

            if ($request->query->get('mini') === '1') {
                $params['mini'] = 1;
            }

            return $this->redirectToRoute('app_conversation_show', $params);
        }

        $messageRepository->marquerLus($ami, $me);
        $messages = $messageRepository->getConversation($me, $ami);

        return $this->render('utilisateur/conversation.html.twig', [
            'me' => $me,
            'ami' => $ami,
            'messages' => $messages,
        ]);
    }

    private function uploadMessageMedia(UploadedFile $file): array
    {
        $mime = (string) $file->getMimeType();
        $originalName = $file->getClientOriginalName();
        $size = $file->getSize() ?: 0;

        $type = $this->detectMessageType($mime, $originalName);

       $allowedMimeTypes = [
    // images
    'image/jpeg',
    'image/png',
    'image/webp',
    'image/gif',

    // videos
    'video/mp4',
    'video/mpeg',
    'video/quicktime',
    'video/x-msvideo',
    'video/webm',
    'video/x-matroska',

    // audio / vocal
    'audio/mpeg',
    'audio/mp3',
    'audio/wav',
    'audio/x-wav',
    'audio/webm',
    'audio/ogg',
    'audio/mp4',
    'audio/x-m4a',

    // documents
    'application/pdf',
    'text/plain',
    'text/csv',
    'application/json',
    'application/xml',
    'text/xml',

    // archives
    'application/zip',
    'application/x-zip-compressed',
    'application/x-rar-compressed',
    'application/vnd.rar',
    'application/octet-stream',

    // Word
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',

    // Excel
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',

    // PowerPoint
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
];

       $extensionFromName = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

$allowedExtensions = [
    'jpg', 'jpeg', 'png', 'gif', 'webp',
    'mp4', 'avi', 'mkv', 'mov', 'webm', 'flv',
    'mp3', 'wav', 'ogg', 'm4a',
    'pdf', 'txt', 'csv', 'json', 'xml',
    'doc', 'docx',
    'xls', 'xlsx',
    'ppt', 'pptx',
    'zip', 'rar',
];

if (
    !in_array($mime, $allowedMimeTypes, true)
    && !in_array($extensionFromName, $allowedExtensions, true)
) {
    throw new FileException('Type de fichier non autorisé : ' . $mime);
}
        $extension = $file->guessExtension();

        if (!$extension) {
            $extension = pathinfo($originalName, PATHINFO_EXTENSION) ?: 'bin';
        }

        $safeExtension = strtolower($extension);

        $fileName = match ($type) {
            'image' => 'img_',
            'video' => 'vid_',
            'vocal' => 'voc_',
            default => 'file_',
        };

        $fileName .= date('Ymd_His') . '_' . uniqid('', true) . '.' . $safeExtension;

        $relativeDirectory = match ($type) {
            'image' => 'uploads/messages/images',
            'video' => 'uploads/messages/videos',
            'vocal' => 'uploads/messages/vocals',
            default => 'uploads/messages/files',
        };

        $projectDir = $this->getParameter('kernel.project_dir');
        assert(is_string($projectDir));

        $targetDir = $projectDir . '/public/' . $relativeDirectory;

        if (!is_dir($targetDir) && !mkdir($targetDir, 0777, true) && !is_dir($targetDir)) {
            throw new FileException('Impossible de créer le dossier : ' . $targetDir);
        }

        $file->move($targetDir, $fileName);

        return [
            'type' => $type,
            'path' => '/' . $relativeDirectory . '/' . $fileName,
            'name' => $originalName,
            'size' => $size,
        ];
    }

    private function detectMessageType(string $mime, string $originalName): string
    {
        $extension = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

        if (str_starts_with($mime, 'image/')) {
            return 'image';
        }

        if (str_starts_with($mime, 'video/')) {
            return 'video';
        }

        if (str_starts_with($mime, 'audio/')) {
            return 'vocal';
        }

        if (in_array($extension, ['png', 'jpg', 'jpeg', 'gif', 'webp'], true)) {
            return 'image';
        }

        if (in_array($extension, ['mp4', 'avi', 'mkv', 'mov', 'webm', 'flv'], true)) {
            return 'video';
        }

        if (in_array($extension, ['mp3', 'wav', 'ogg', 'webm', 'm4a'], true)) {
            return 'vocal';
        }

        return 'fichier';
 
        }


        #[Route('/media/{id}', name: 'app_conversation_media', methods: ['GET'])]
public function media(
    int $id,
    MessageRepository $messageRepository
): Response {
    $message = $messageRepository->find($id);

    if (!$message instanceof Message) {
        throw $this->createNotFoundException('Message introuvable.');
    }

    $filePath = $this->resolveStoredMessagePath($message->getFichierPath());

    if ($filePath === null || !is_file($filePath)) {
        throw $this->createNotFoundException(
            'Fichier message introuvable. Valeur base : ' . (string) $message->getFichierPath()
        );
    }

    $response = new BinaryFileResponse($filePath);

    $type = $message->getTypeMessage();

    if (in_array($type, ['image', 'video', 'vocal'], true)) {
        $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_INLINE);
    } else {
        $downloadName = $message->getFichierNom() ?: basename($filePath);
        $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_ATTACHMENT, $downloadName);
    }

    $mimeType = @mime_content_type($filePath);
    if ($mimeType) {
        $response->headers->set('Content-Type', $mimeType);
    }

    $response->headers->set('Cache-Control', 'public, max-age=3600');

    return $response;
}

private function resolveStoredMessagePath(?string $dbPath): ?string
{
    $dbPath = trim((string) $dbPath);

    if ($dbPath === '' || strtoupper($dbPath) === 'NULL') {
        return null;
    }

    $projectDir = $this->getParameter('kernel.project_dir');
    assert(is_string($projectDir));

    $candidates = [];
    $normalized = str_replace('\\', '/', $dbPath);

    // 1) Chemin absolu Windows / Linux
    // Exemple : C:\Users\hamza\...
    // Exemple : C:/Users/hamza/...
    if (
        preg_match('/^[A-Za-z]:[\/\\\\]/', $dbPath) ||
        str_starts_with($normalized, '/home/') ||
        str_starts_with($normalized, '/var/') ||
        str_starts_with($normalized, '/mnt/')
    ) {
        $candidates[] = $dbPath;
        $candidates[] = $normalized;
    }

    // 2) Nouveau chemin Symfony : /uploads/messages/...
    if (
        str_starts_with($normalized, '/uploads/') ||
        str_starts_with($normalized, 'uploads/')
    ) {
        $relative = ltrim($normalized, '/');
        $candidates[] = $projectDir . '/public/' . $relative;
    }

    // 3) Ancien chemin JavaFX : /view/image/...
    if (
        str_starts_with($normalized, '/view/') ||
        str_starts_with($normalized, 'view/')
    ) {
        $relative = ltrim($normalized, '/');

        $candidates[] = $projectDir . '/public/' . $relative;
        $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/javafx/src/main/resources/' . $relative;
        $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/JavaFX/src/main/resources/' . $relative;
    }

    // 4) Ancien dossier JavaFX message images
    if (str_contains($normalized, 'SkillSwap/images/')) {
        $fileName = basename($normalized);
        $candidates[] = 'C:/Users/hamza/SkillSwap/images/' . $fileName;
    }

    // 5) Anciens dossiers possibles JavaFX / Documents
    $baseName = basename($normalized);

    if ($baseName && $baseName !== '.' && $baseName !== '/') {
        $candidates[] = $projectDir . '/public/uploads/messages/images/' . $baseName;
        $candidates[] = $projectDir . '/public/uploads/messages/videos/' . $baseName;
        $candidates[] = $projectDir . '/public/uploads/messages/files/' . $baseName;
        $candidates[] = $projectDir . '/public/uploads/messages/vocals/' . $baseName;

        $candidates[] = 'C:/Users/hamza/SkillSwap/images/' . $baseName;
        $candidates[] = 'C:/Users/hamza/SkillSwap/videos/' . $baseName;
        $candidates[] = 'C:/Users/hamza/SkillSwap/files/' . $baseName;
        $candidates[] = 'C:/Users/hamza/SkillSwap/vocals/' . $baseName;

        $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/messages/images/' . $baseName;
        $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/messages/videos/' . $baseName;
        $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/messages/files/' . $baseName;
        $candidates[] = 'C:/Users/hamza/Documents/SkillSwap/uploads/messages/vocals/' . $baseName;

        $candidates[] = 'C:/Users/hamza/Bureau/SkillSwap/javafx/src/main/resources/view/image/utilisateur/uploads/' . $baseName;
    }

    // 6) Chemin relatif direct
    $relativeClean = ltrim($normalized, '/');
    $candidates[] = $projectDir . '/public/' . $relativeClean;

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
}