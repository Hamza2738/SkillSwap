<?php

namespace App\Service;

use PDO;

class UtilisateurService
{
    private PDO $pdo;

    public function __construct(PDO $pdo)
    {
        $this->pdo = $pdo;
    }

    // =========================
    // FIND BY ID
    // =========================
    public function findById(int $id): array|false
    {
        $stmt = $this->pdo->prepare("SELECT * FROM utilisateur WHERE id_utilisateur = ?");
        $stmt->execute([$id]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // =========================
    // FIND BY EMAIL
    // =========================
    public function findByEmail(string $email): array|false
    {
        $stmt = $this->pdo->prepare("SELECT * FROM utilisateur WHERE email = ?");
        $stmt->execute([$email]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // =========================
    // LOGIN
    // =========================
    public function login(string $email, string $password): ?array
    {
        $stmt = $this->pdo->prepare(
            "SELECT * FROM utilisateur WHERE email = ? AND statut <> 'supprime'"
        );
        $stmt->execute([$email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            return null;
        }

        // Si mot de passe non hashé
        if ($password !== $user['mot_de_passe']) {
            return null;
        }

        return $user;
    }

    // =========================
    // INSCRIPTION
    // =========================
    public function inscrire(array $user): string|false
    {
        $stmt = $this->pdo->prepare(
            "INSERT INTO utilisateur 
            (nom, prenom, email, mot_de_passe, telephone, role, statut, cle_acces)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );

        $stmt->execute([
            $user['nom'],
            $user['prenom'],
            $user['email'],
            $user['mot_de_passe'],
            $user['telephone'],
            $user['role'],
            $user['statut'] ?? 'actif',
            $user['cle_acces']
        ]);

        return $this->pdo->lastInsertId();
    }

    // =========================
    // SEARCH SUGGESTIONS
    // =========================
    public function searchSuggestions(string $q, int $limit = 10): array
    {
        if (!$q || mb_strlen(trim($q)) < 2) {
            return [];
        }

        $q = mb_strtolower(trim($q));
        $safeLimit = max(1, min($limit, 15));
        $parts = preg_split('/\s+/', $q);

        $p1 = '%' . ($parts[0] ?? '') . '%';
        $p2 = '%' . ($parts[1] ?? ($parts[0] ?? '')) . '%';
        $full = '%' . $q . '%';

        $sql = "SELECT * FROM utilisateur
                WHERE statut <> 'supprime'
                AND (
                    LOWER(nom) LIKE ?
                    OR LOWER(prenom) LIKE ?
                    OR LOWER(email) LIKE ?
                    OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?)
                    OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?)
                )
                ORDER BY nom ASC, prenom ASC
                LIMIT {$safeLimit}";

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([$full, $full, $full, $p1, $p2, $p2, $p1]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // =========================
    // UPDATE PROFIL SIMPLE
    // =========================
    public function updateProfil(array $user): bool
    {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur 
             SET nom = ?, prenom = ?, telephone = ? 
             WHERE id_utilisateur = ?"
        );

        return $stmt->execute([
            $user['nom'],
            $user['prenom'],
            $user['telephone'],
            $user['id_utilisateur']
        ]);
    }

    // =========================
    // UPDATE PROFIL AVEC PHOTO
    // =========================
    public function updateProfilAvecPhoto(array $user): bool
    {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur 
             SET nom = ?, prenom = ?, telephone = ?, photo_profil = ? 
             WHERE id_utilisateur = ?"
        );

        return $stmt->execute([
            $user['nom'],
            $user['prenom'],
            $user['telephone'],
            $user['photo_profil'],
            $user['id_utilisateur']
        ]);
    }

    public function updatePhoto(int $idUtilisateur, string $photoPath): bool
    {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur SET photo_profil = ? WHERE id_utilisateur = ?"
        );

        return $stmt->execute([$photoPath, $idUtilisateur]);
    }

    // =========================
    // UPDATE PROFIL PRO
    // =========================
    public function updateProfilPro(array $user): bool
    {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur 
             SET bio = ?, photo_couverture = ?, lieu = ? 
             WHERE id_utilisateur = ?"
        );

        return $stmt->execute([
            $user['bio'],
            $user['photo_couverture'],
            $user['lieu'],
            $user['id_utilisateur']
        ]);
    }

    // =========================
    // UPDATE PASSWORD
    // =========================
    public function updatePassword(string $email, string $newPassword): bool
    {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur SET mot_de_passe = ? WHERE email = ?"
        );

        return $stmt->execute([$newPassword, $email]);
    }

    // =========================
    // DELETE UTILISATEUR
    // =========================
    public function delete(int $id): bool
    {
        $stmt = $this->pdo->prepare(
            "DELETE FROM utilisateur WHERE id_utilisateur = ?"
        );

        return $stmt->execute([$id]);
    }
}