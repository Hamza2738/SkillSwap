package org.service.utilisateur;

import org.model.Utilisateurs.Connexion;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConnexionService {

    private final Connection cnx;

    public ConnexionService() {
        this.cnx = mydatabase.getInstance().getConnection();
    }

    // =========================
    // ENVOYER UNE INVITATION
    // =========================
    public void envoyerInvitation(int idDemandeur, int idReceveur) throws SQLException {
        String sql = "INSERT INTO connexion (id_demandeur, id_receveur, statut) VALUES (?, ?, 'en_attente')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idDemandeur);
            ps.setInt(2, idReceveur);
            ps.executeUpdate();
        }
    }

    // =========================
    // ACCEPTER UNE INVITATION
    // =========================
    public void accepterInvitation(int idDemandeur, int idReceveur) throws SQLException {
        String sql = "UPDATE connexion SET statut='accepte' WHERE id_demandeur=? AND id_receveur=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idDemandeur);
            ps.setInt(2, idReceveur);
            ps.executeUpdate();
        }
    }

    // =========================
    // REFUSER / SUPPRIMER
    // =========================
    public void supprimerConnexion(int idA, int idB) throws SQLException {
        String sql = "DELETE FROM connexion WHERE (id_demandeur=? AND id_receveur=?) " +
                "OR (id_demandeur=? AND id_receveur=?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idA); ps.setInt(2, idB);
            ps.setInt(3, idB); ps.setInt(4, idA);
            ps.executeUpdate();
        }
    }

    // =========================
    // STATUT ENTRE DEUX UTILISATEURS
    // Retourne : null, "en_attente", "accepte", "refuse"
    // =========================
    public String getStatut(int idA, int idB) throws SQLException {
        String sql = "SELECT statut FROM connexion " +
                "WHERE (id_demandeur=? AND id_receveur=?) " +
                "OR    (id_demandeur=? AND id_receveur=?) LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idA); ps.setInt(2, idB);
            ps.setInt(3, idB); ps.setInt(4, idA);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("statut");
            }
        }
        return null; // aucune relation
    }

    // =========================
    // EST-CE MOI QUI AI ENVOYÉ ?
    // =========================
    public boolean estDemandeur(int idDemandeur, int idReceveur) throws SQLException {
        String sql = "SELECT id FROM connexion WHERE id_demandeur=? AND id_receveur=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idDemandeur);
            ps.setInt(2, idReceveur);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =========================
    // LISTE DES INVITATIONS REÇUES EN ATTENTE
    // =========================
    public List<Connexion> getInvitationsRecues(int idReceveur) throws SQLException {
        String sql = "SELECT * FROM connexion WHERE id_receveur=? AND statut='en_attente'";
        List<Connexion> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idReceveur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // =========================
    // LISTE DES CONNEXIONS ACCEPTÉES
    // =========================
    public List<Connexion> getConnexions(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM connexion WHERE (id_demandeur=? OR id_receveur=?) AND statut='accepte'";
        List<Connexion> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // =========================
    // MAPPER
    // =========================
    private Connexion map(ResultSet rs) throws SQLException {
        Connexion c = new Connexion();
        c.setId(rs.getInt("id"));
        c.setIdDemandeur(rs.getInt("id_demandeur"));
        c.setIdReceveur(rs.getInt("id_receveur"));
        c.setStatut(rs.getString("statut"));
        Timestamp ts = rs.getTimestamp("date_demande");
        if (ts != null) c.setDateDemande(ts.toLocalDateTime());
        return c;
    }
}