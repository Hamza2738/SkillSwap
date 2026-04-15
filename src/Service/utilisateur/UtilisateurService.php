<?php
require_once 'Database.php'; // Singleton PDO comme mydatabase

class UtilisateurService {
    private $pdo;

  

    // =========================
    // FIND BY ID
    // =========================
    public function findById($id) {
        $stmt = $this->pdo->prepare("SELECT * FROM utilisateur WHERE id_utilisateur=?");
        $stmt->execute([$id]);
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // =========================
    // FIND BY EMAIL
    // =========================
    public function findByEmail($email) {
        $stmt = $this->pdo->prepare("SELECT * FROM utilisateur WHERE email=?");
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

    // Comparaison directe sans hash
    if ($password !== $user['mot_de_passe']) {
        return null;
    }

    return $user;
}
    // =========================
    // INSCRIPTION
    // =========================
    public function inscrire($user) {
        $stmt = $this->pdo->prepare(
            "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role, statut, cle_acces)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        $stmt->execute([
            $user['nom'], $user['prenom'], $user['email'], $user['mot_de_passe'],
            $user['telephone'], $user['role'], $user['statut'] ?? 'actif', $user['cle_acces']
        ]);
        return $this->pdo->lastInsertId();
    }

    // =========================
    // SEARCH SUGGESTIONS (autocomplete)
    // =========================
    public function searchSuggestions($q, $limit = 10) {
        if (!$q || strlen(trim($q)) < 2) return [];

        $q = strtolower(trim($q));
        $safeLimit = max(1, min($limit, 15));
        $parts = explode(' ', $q);
        $p1 = "%{$parts[0]}%";
        $p2 = isset($parts[1]) ? "%{$parts[1]}%" : $p1;

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
        $stmt->execute([$q, $q, $q, $p1, $p2, $p2, $p1]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // =========================
    // UPDATE PROFIL SIMPLE
    // =========================
    public function updateProfil($user) {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur SET nom=?, prenom=?, telephone=? WHERE id_utilisateur=?"
        );
        $stmt->execute([$user['nom'], $user['prenom'], $user['telephone'], $user['id_utilisateur']]);
    }

    // =========================
    // UPDATE PROFIL AVEC PHOTO
    // =========================
    public function updateProfilAvecPhoto($user) {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur SET nom=?, prenom=?, telephone=?, photo_profil=? WHERE id_utilisateur=?"
        );
        $stmt->execute([$user['nom'], $user['prenom'], $user['telephone'], $user['photo_profil'], $user['id_utilisateur']]);
    }

    public function updatePhoto($idUtilisateur, $photoPath) {
        $stmt = $this->pdo->prepare("UPDATE utilisateur SET photo_profil=? WHERE id_utilisateur=?");
        $stmt->execute([$photoPath, $idUtilisateur]);
    }

    // =========================
    // UPDATE PROFIL PRO (bio/cover/lieu)
    // =========================
    public function updateProfilPro($user) {
        $stmt = $this->pdo->prepare(
            "UPDATE utilisateur SET bio=?, photo_couverture=?, lieu=? WHERE id_utilisateur=?"
        );
        $stmt->execute([$user['bio'], $user['photo_couverture'], $user['lieu'], $user['id_utilisateur']]);
    }

    // =========================
    // UPDATE PASSWORD
    // =========================
    public function updatePassword($email, $newPassword) {
    $stmt = $this->pdo->prepare("UPDATE utilisateur SET mot_de_passe=? WHERE email=?");
    $stmt->execute([$newPassword, $email]);
}

    // =========================
    // DELETE UTILISATEUR
    // =========================
    public function delete($id) {
        $stmt = $this->pdo->prepare("DELETE FROM utilisateur WHERE id_utilisateur=?");
        $stmt->execute([$id]);
    }
}
?>