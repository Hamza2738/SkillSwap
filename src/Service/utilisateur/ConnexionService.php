<?php
require_once 'Database.php';

class ConnexionService {
    private $pdo;

 
    // =========================
    // ENVOYER UNE INVITATION
    // =========================
    public function envoyerInvitation($idDemandeur, $idReceveur) {
        $stmt = $this->pdo->prepare(
            "INSERT INTO connexion (id_demandeur, id_receveur, statut) VALUES (?, ?, 'en_attente')"
        );
        $stmt->execute([$idDemandeur, $idReceveur]);
    }

    // =========================
    // ACCEPTER UNE INVITATION
    // =========================
    public function accepterInvitation($idDemandeur, $idReceveur) {
        $stmt = $this->pdo->prepare(
            "UPDATE connexion SET statut='accepte' WHERE id_demandeur=? AND id_receveur=?"
        );
        $stmt->execute([$idDemandeur, $idReceveur]);
    }

    // =========================
    // REFUSER OU SUPPRIMER UNE CONNEXION
    // =========================
    public function supprimerConnexion($idA, $idB) {
        $stmt = $this->pdo->prepare(
            "DELETE FROM connexion 
             WHERE (id_demandeur=? AND id_receveur=?) OR (id_demandeur=? AND id_receveur=?)"
        );
        $stmt->execute([$idA, $idB, $idB, $idA]);
    }

    // =========================
    // STATUT ENTRE DEUX UTILISATEURS
    // Retourne : null, "en_attente", "accepte", "refuse"
    // =========================
    public function getStatut($idA, $idB) {
        $stmt = $this->pdo->prepare(
            "SELECT statut 
             FROM connexion 
             WHERE (id_demandeur=? AND id_receveur=?) OR (id_demandeur=? AND id_receveur=?) 
             LIMIT 1"
        );
        $stmt->execute([$idA, $idB, $idB, $idA]);
        $res = $stmt->fetch(PDO::FETCH_ASSOC);
        return $res['statut'] ?? null;
    }

    // =========================
    // EST-CE MOI QUI AI ENVOYÉ L’INVITATION ?
    // =========================
    public function estDemandeur($idDemandeur, $idReceveur) {
        $stmt = $this->pdo->prepare(
            "SELECT id FROM connexion WHERE id_demandeur=? AND id_receveur=? LIMIT 1"
        );
        $stmt->execute([$idDemandeur, $idReceveur]);
        return $stmt->fetch(PDO::FETCH_ASSOC) !== false;
    }

    // =========================
    // LISTE DES INVITATIONS REÇUES EN ATTENTE
    // =========================
    public function getInvitationsRecues($idReceveur) {
        $stmt = $this->pdo->prepare(
            "SELECT * FROM connexion WHERE id_receveur=? AND statut='en_attente'"
        );
        $stmt->execute([$idReceveur]);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // =========================
    // LISTE DES CONNEXIONS ACCEPTÉES
    // =========================
    public function getConnexions($idUtilisateur) {
        $stmt = $this->pdo->prepare(
            "SELECT * FROM connexion 
             WHERE (id_demandeur=? OR id_receveur=?) AND statut='accepte'"
        );
        $stmt->execute([$idUtilisateur, $idUtilisateur]);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>