<?php

namespace App\Service\utilisateur;

use App\Entity\Message;
use App\Entity\Utilisateur;
use App\Repository\MessageRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\File\UploadedFile;

/**
 * MessageService — version Symfony finale.
 *
 * Toutes les méthodes correspondent exactement aux méthodes
 * du MessageService PHP procédural (document 5) et aux appels
 * du ProfilController Java (document 1).
 *
 * Schéma respecté à 100 % :
 *   id, id_envoyeur (FK), id_receveur (FK), contenu, date_envoi,
 *   lu, type_message, fichier_path, fichier_nom, fichier_taille,
 *   duree_vocal, audio_data (LONGBLOB)
 */
class MessageService
{
    private string $uploadDir;

    public function __construct(
        private readonly EntityManagerInterface $em,
        private readonly MessageRepository      $messageRepo,
        private readonly UtilisateurRepository  $utilisateurRepo,
        string $uploadDir   // injecté via services.yaml : '%kernel.project_dir%/public/uploads/messages'
    ) {
        $this->uploadDir = rtrim($uploadDir, '/');
        if (!is_dir($this->uploadDir)) {
            mkdir($this->uploadDir, 0775, true);
        }
    }

    // =========================================================
    // ENVOYER TEXTE
    // Équivalent exact de envoyer() PHP procédural :
    //   INSERT INTO message (id_envoyeur, id_receveur, contenu, type_message, lu)
    //   VALUES (?, ?, ?, 'texte', FALSE)
    // =========================================================
    public function envoyer(int $idEnvoyeur, int $idReceveur, string $contenu): int
    {
        $msg = new Message();
        $msg->setEnvoyeur($this->getUtilisateur($idEnvoyeur));
        $msg->setReceveur($this->getUtilisateur($idReceveur));
        $msg->setContenu($contenu);
        $msg->setTypeMessage('texte');
        $msg->setLu(false);
        // dateEnvoi est initialisée dans le constructeur de Message
        // (DEFAULT CURRENT_TIMESTAMP respecté)

        $this->em->persist($msg);
        $this->em->flush();

        return $msg->getId();
    }

    // =========================================================
    // ENVOYER IMAGE
    // Équivalent de envoyerImage() PHP procédural
    // =========================================================
    public function envoyerImage(int $idEnvoyeur, int $idReceveur, UploadedFile $file): Message
    {
        return $this->envoyerFichierGenerique($idEnvoyeur, $idReceveur, $file, 'image', '📷');
    }

    // =========================================================
    // ENVOYER VIDÉO
    // Équivalent de envoyerVideo() PHP procédural
    // =========================================================
    public function envoyerVideo(int $idEnvoyeur, int $idReceveur, UploadedFile $file): Message
    {
        return $this->envoyerFichierGenerique($idEnvoyeur, $idReceveur, $file, 'video', '🎬');
    }

    // =========================================================
    // ENVOYER FICHIER (PDF, Word, Excel…)
    // Équivalent de envoyerFichier() PHP procédural
    // =========================================================
    public function envoyerFichier(int $idEnvoyeur, int $idReceveur, UploadedFile $file): Message
    {
        return $this->envoyerFichierGenerique($idEnvoyeur, $idReceveur, $file, 'fichier', '📎');
    }

    // =========================================================
    // ENVOYER VOCAL
    // Équivalent de envoyerVocal() PHP procédural :
    //   INSERT INTO message
    //   (id_envoyeur, id_receveur, contenu, type_message, audio_data, duree_vocal, lu)
    //   VALUES (?, ?, '[🎤 Message vocal]', 'vocal', ?, ?, FALSE)
    //
    // $audioData : string binaire PCM 16-bit 16kHz mono
    //              (équivalent du byte[] Java / LONGBLOB SQL)
    // =========================================================
    public function envoyerVocal(
        int    $idEnvoyeur,
        int    $idReceveur,
        string $audioData,
        float  $dureeSecondes
    ): Message {
        $msg = new Message();
        $msg->setEnvoyeur($this->getUtilisateur($idEnvoyeur));
        $msg->setReceveur($this->getUtilisateur($idReceveur));
        $msg->setContenu('[🎤 Message vocal]');
        $msg->setTypeMessage('vocal');
        $msg->setAudioData($audioData);      // → colonne audio_data LONGBLOB
        $msg->setDureeVocal($dureeSecondes); // → colonne duree_vocal FLOAT
        $msg->setLu(false);

        $this->em->persist($msg);
        $this->em->flush();

        return $msg;
    }

    // =========================================================
    // CONVERSATION ENTRE DEUX UTILISATEURS
    // Équivalent de getConversation() PHP procédural :
    //   SELECT * FROM message
    //   WHERE (id_envoyeur=? AND id_receveur=?)
    //      OR (id_envoyeur=? AND id_receveur=?)
    //   ORDER BY date_envoi ASC
    // =========================================================
    public function getConversation(int $idA, int $idB): array
    {
        return $this->messageRepo->findConversation($idA, $idB);
    }

    // =========================================================
    // MESSAGES NON LUS
    // Équivalent de getNonLus() PHP procédural :
    //   SELECT COUNT(*) as nb FROM message
    //   WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE
    // =========================================================
    public function getNonLus(int $idEnvoyeur, int $idReceveur): int
    {
        return $this->messageRepo->countNonLus($idEnvoyeur, $idReceveur);
    }

    // =========================================================
    // MARQUER COMME LUS
    // Équivalent de marquerLus() PHP procédural :
    //   UPDATE message SET lu=TRUE
    //   WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE
    // =========================================================
    public function marquerLus(int $idEnvoyeur, int $idReceveur): void
    {
        $this->messageRepo->marquerLus($idEnvoyeur, $idReceveur);
    }

    // =========================================================
    // SUPPRIMER UN MESSAGE
    // Équivalent de supprimer() PHP procédural :
    //   Supprime le fichier physique si image/video/fichier,
    //   puis DELETE FROM message WHERE id=?
    // =========================================================
    public function supprimer(int $idMessage): void
    {
        $msg = $this->messageRepo->find($idMessage);
        if (!$msg) {
            return;
        }

        // Supprimer le fichier physique si applicable
        if (
            in_array($msg->getTypeMessage(), ['image', 'video', 'fichier'], true)
            && $msg->getFichierPath()
            && file_exists($msg->getFichierPath())
        ) {
            unlink($msg->getFichierPath());
        }

        $this->em->remove($msg);
        $this->em->flush();
    }

    // =========================================================
    // HELPER PRIVÉ — upload + persistance
    // Équivalent de envoyerFichierGenerique() PHP procédural :
    //   move_uploaded_file() + INSERT avec fichier_path, fichier_nom,
    //   fichier_taille (BIGINT), type_message, contenu
    //
    // Colonnes SQL utilisées :
    //   contenu        TEXT
    //   type_message   VARCHAR(50)
    //   fichier_path   VARCHAR(500)
    //   fichier_nom    VARCHAR(255)
    //   fichier_taille BIGINT
    //   lu             BOOLEAN DEFAULT FALSE
    // =========================================================
    private function envoyerFichierGenerique(
        int          $idEnvoyeur,
        int          $idReceveur,
        UploadedFile $file,
        string       $type,
        string       $emoji
    ): Message {
        // Sous-dossier par type pour organiser les uploads
        $destDir = $this->uploadDir . '/' . $type . 's';
        if (!is_dir($destDir)) {
            mkdir($destDir, 0775, true);
        }

        $ext     = $file->getClientOriginalExtension();
        $newName = $type . '_' . uniqid() . '.' . $ext;
        $file->move($destDir, $newName);
        $fullPath = $destDir . '/' . $newName;

        // Taille réelle après déplacement (équivalent de $file['size'] procédural)
        $taille = file_exists($fullPath) ? filesize($fullPath) : $file->getSize();

        $msg = new Message();
        $msg->setEnvoyeur($this->getUtilisateur($idEnvoyeur));
        $msg->setReceveur($this->getUtilisateur($idReceveur));
        $msg->setContenu('[' . $emoji . ' ' . $file->getClientOriginalName() . ']');
        $msg->setTypeMessage($type);
        $msg->setFichierPath($fullPath);                      // → fichier_path VARCHAR(500)
        $msg->setFichierNom($file->getClientOriginalName());  // → fichier_nom  VARCHAR(255)
        $msg->setFichierTaille((int) $taille);                // → fichier_taille BIGINT
        $msg->setLu(false);

        $this->em->persist($msg);
        $this->em->flush();

        return $msg;
    }

    // =========================================================
    // HELPER PRIVÉ — charger un Utilisateur par son id
    // Utilisé pour remplir les relations ManyToOne de l'entité.
    // Lance une exception claire si l'id est invalide.
    // =========================================================
    private function getUtilisateur(int $id): Utilisateur
    {
        $u = $this->utilisateurRepo->find($id);
        if (!$u) {
            throw new \InvalidArgumentException(
                sprintf('Utilisateur id=%d introuvable.', $id)
            );
        }
        return $u;
    }

    // =========================================================
    // GETTER uploadDir (équivalent de getUploadDir() procédural)
    // =========================================================
    public function getUploadDir(): string
    {
        return $this->uploadDir;
    }
}