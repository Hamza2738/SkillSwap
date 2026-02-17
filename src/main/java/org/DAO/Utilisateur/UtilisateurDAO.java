package org.DAO.Utilisateur;

import org.model.Utilisateurs.utilisateur;
import org.utils.mydatabase;

import java.sql.*;

public class UtilisateurDAO {

    // ✅ Login (simple). En vrai on hash le mot de passe, mais je reste sur ta DB actuelle.
    public utilisateur findByEmailAndPassword(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=? AND statut <> 'supprimé' LIMIT 1";

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, motDePasse);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapUtilisateur(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("findByEmailAndPassword: " + e.getMessage());
        }
        return null;
    }

    public utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur=? LIMIT 1";

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapUtilisateur(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("findById: " + e.getMessage());
        }
        return null;
    }

    public int insert(utilisateur u) {
        String sql = """
            INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, photo_profil, role, statut)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNom());
                ps.setString(2, u.getPrenom());
                ps.setString(3, u.getEmail());
                ps.setString(4, u.getMot_de_passe());
                ps.setString(5, u.getTelephone());
                ps.setString(6, u.getPhoto_profil());
                ps.setString(7, u.getRole());
                ps.setString(8, u.getStatut() == null ? "actif" : u.getStatut());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("insert utilisateur: " + e.getMessage());
        }
        return -1;
    }

    // ✅ Mapping ResultSet -> Model
    private utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        utilisateur u = new utilisateur();

        u.setId_utilisateur(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMot_de_passe(rs.getString("mot_de_passe"));
        u.setTelephone(rs.getString("telephone"));
        u.setPhoto_profil(rs.getString("photo_profil"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));

        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) u.setDate_inscription(ts.toLocalDateTime());

        return u;
    }
}
