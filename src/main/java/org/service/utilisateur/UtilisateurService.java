package org.service.utilisateur;

import org.model.Utilisateurs.utilisateur;
import org.utils.mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private final Connection cnx;

    public UtilisateurService() {
        this.cnx = mydatabase.getInstance().getConnection();
    }

    public utilisateur findById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        }
        return null;
    }
    // =========================
    // INSCRIPTION (inchangé)
    // =========================
    public void inscrire(utilisateur u) throws SQLException {

        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role, statut, cle_acces) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMot_de_passe());
            ps.setString(5, u.getTelephone());
            ps.setString(6, u.getRole());
            ps.setString(7, (u.getStatut() == null || u.getStatut().isBlank()) ? "actif" : u.getStatut());
            ps.setString(8, u.getCle_acces());

            ps.executeUpdate();
        }
    }

    // =========================
    // ✅ SUGGESTIONS (pour autocomplete)
    // =========================
    public List<utilisateur> searchSuggestions(String q, int limit) throws SQLException {

        if (q == null) return List.of();
        q = q.trim();
        if (q.length() < 2) return List.of();

        int safeLimit = Math.max(1, Math.min(limit, 15)); // borné 1..15

        String qLower = q.toLowerCase();
        String[] parts = qLower.split("\\s+");
        String p1 = "%" + parts[0] + "%";
        String p2 = parts.length > 1 ? "%" + parts[1] + "%" : p1;

        String sql =
                "SELECT * FROM utilisateur " +
                        "WHERE statut <> 'supprime' " +
                        "AND (" +
                        "   LOWER(nom) LIKE ? " +
                        "   OR LOWER(prenom) LIKE ? " +
                        "   OR LOWER(email) LIKE ? " +
                        "   OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?) " +
                        "   OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?) " +
                        ") " +
                        "ORDER BY nom ASC, prenom ASC " +
                        "LIMIT " + safeLimit;

        List<utilisateur> out = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + qLower + "%"); // nom contient q
            ps.setString(2, "%" + qLower + "%"); // prenom contient q
            ps.setString(3, "%" + qLower + "%"); // email contient q

            ps.setString(4, p1); // nom ~ part1
            ps.setString(5, p2); // prenom ~ part2

            ps.setString(6, p2); // nom ~ part2
            ps.setString(7, p1); // prenom ~ part1

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapResultSetToUser(rs));
                }
            }
        }

        return out;
    }

    // =========================
    // SEARCH BY NOM/PRENOM (retourne 1 seul)
    // =========================
    public utilisateur findByNomOrPrenom(String q) throws SQLException {

        if (q == null) return null;
        q = q.trim();
        if (q.isEmpty()) return null;

        String qLower = q.toLowerCase();
        String[] parts = qLower.split("\\s+");

        String p1 = "%" + parts[0] + "%";
        String p2 = parts.length > 1 ? "%" + parts[1] + "%" : p1;

        String sql =
                "SELECT * FROM utilisateur " +
                        "WHERE statut <> 'supprime' " +
                        "AND (" +
                        "   LOWER(nom) LIKE ? " +
                        "   OR LOWER(prenom) LIKE ? " +
                        "   OR LOWER(email) LIKE ? " +
                        "   OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?) " +
                        "   OR (LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?) " +
                        ") " +
                        "ORDER BY nom ASC, prenom ASC " +
                        "LIMIT 1";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + qLower + "%");
            ps.setString(2, "%" + qLower + "%");
            ps.setString(3, "%" + qLower + "%");

            ps.setString(4, p1);
            ps.setString(5, p2);

            ps.setString(6, p2);
            ps.setString(7, p1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // =========================
    // FIND BY EMAIL
    // =========================
    public utilisateur findByEmail(String email) throws SQLException {

        String sql = "SELECT * FROM utilisateur WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // =========================
    // UPDATE PROFIL AVEC PHOTO
    // =========================
    public void updateProfilAvecPhoto(utilisateur u) throws SQLException {

        String sql = "UPDATE utilisateur SET nom=?, prenom=?, telephone=?, photo_profil=? WHERE id_utilisateur=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getTelephone());
            ps.setString(4, u.getPhoto_profil());
            ps.setInt(5, u.getId_utilisateur());

            ps.executeUpdate();
        }
    }

    public void updatePhoto(int idUtilisateur, String photoPath) throws SQLException {
        String sql = "UPDATE utilisateur SET photo_profil=? WHERE id_utilisateur=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, photoPath);
            ps.setInt(2, idUtilisateur);
            ps.executeUpdate();
        }
    }

    // =========================
    // ✅ UPDATE PROFIL PRO (bio/cover/lieu)
    // =========================
    public void updateProfilPro(utilisateur u) throws SQLException {

        String sql = "UPDATE utilisateur SET bio=?, photo_couverture=?, lieu=? WHERE id_utilisateur=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getBio());
            ps.setString(2, u.getPhoto_couverture());
            ps.setString(3, u.getLieu());
            ps.setInt(4, u.getId_utilisateur());
            ps.executeUpdate();
        }
    }

    // =========================
    // LOGIN
    // =========================
    public utilisateur login(String email, String motDePasse) throws SQLException {

        String sql = "SELECT * FROM utilisateur " +
                "WHERE email=? AND mot_de_passe=? AND statut <> 'supprime'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, motDePasse);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // =========================
    // UPDATE PROFIL (Nom, Prenom, Telephone)
    // =========================
    public void update(utilisateur u) throws SQLException {

        String sql = "UPDATE utilisateur SET nom=?, prenom=?, telephone=? WHERE id_utilisateur=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getTelephone());
            ps.setInt(4, u.getId_utilisateur());

            ps.executeUpdate();
        }
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE email=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    // =========================
    // DELETE
    // =========================
    public void delete(int idUtilisateur) throws SQLException {

        String sql = "DELETE FROM utilisateur WHERE id_utilisateur=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.executeUpdate();
        }
    }

    // =========================
    // MAPPER RESULTSET -> UTILISATEUR
    // =========================
    private utilisateur mapResultSetToUser(ResultSet rs) throws SQLException {

        utilisateur u = new utilisateur();
        u.setCle_acces(rs.getString("cle_acces"));
        u.setId_utilisateur(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMot_de_passe(rs.getString("mot_de_passe"));
        u.setTelephone(rs.getString("telephone"));
        u.setPhoto_profil(rs.getString("photo_profil"));

        u.setBio(rs.getString("bio"));
        u.setPhoto_couverture(rs.getString("photo_couverture"));
        u.setLieu(rs.getString("lieu"));

        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));

        return u;
    }
}