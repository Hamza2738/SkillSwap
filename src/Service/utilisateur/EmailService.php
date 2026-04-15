<?php
require_once 'Database.php';

class EmailHistoryService {
    private $pdo;

  

    // Récupérer tous les emails envoyés
    public function getAllEmails() {
        $stmt = $this->pdo->query("SELECT * FROM email_history ORDER BY date_envoi DESC");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // Rechercher les emails par destinataire
    public function getEmailsByDestinataire($email) {
        $stmt = $this->pdo->prepare("SELECT * FROM email_history WHERE destinataire=? ORDER BY date_envoi DESC");
        $stmt->execute([$email]);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
