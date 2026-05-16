package org.service.RendezVous;

import org.model.RendezVous.rendez_vous;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService {

    private final Connection cnx;

    public RendezVousService() {
        cnx = mydatabase.getInstance().getConnection();  // Assuming `mydatabase` handles DB connection pooling
    }

    // Add a new rendez-vous
    public void ajouter(rendez_vous r) throws SQLException {
        String sql = "INSERT INTO rendez_vous (id_admin_createur, mail_user, titre, description, date_rendez_vous, type, competence, nombre_places) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getId_admin_createur());
            ps.setString(2, r.getMail_user());
            ps.setString(3, r.getTitre());
            ps.setString(4, r.getDescription());
            ps.setTimestamp(5, r.getDate_rendez_vous() == null ? null : Timestamp.valueOf(r.getDate_rendez_vous()));
            ps.setString(6, r.getType());
            ps.setString(7, r.getCompetence());
            ps.setInt(8, r.getNombre_places());  // Nouvelle valeur
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding rendez-vous: " + e.getMessage());
            throw e;  // Re-throw the exception after logging
        }
    }

    // Modify an existing rendez-vous
    public void modifier(rendez_vous r) throws SQLException {
        String sql = "UPDATE rendez_vous SET mail_user=?, titre=?, description=?, date_rendez_vous=?, type=?, competence=?, nombre_places=? " +
                "WHERE id_rendez_vous=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getMail_user());
            ps.setString(2, r.getTitre());
            ps.setString(3, r.getDescription());
            ps.setTimestamp(4, r.getDate_rendez_vous() == null ? null : Timestamp.valueOf(r.getDate_rendez_vous()));
            ps.setString(5, r.getType());
            ps.setString(6, r.getCompetence());
            ps.setInt(7, r.getNombre_places());  // Nouvelle valeur
            ps.setInt(8, r.getId_rendez_vous());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error modifying rendez-vous: " + e.getMessage());
            throw e;
        }
    }

    // Delete a rendez-vous
    public void supprimer(int idRv) throws SQLException {
        String sql = "DELETE FROM rendez_vous WHERE id_rendez_vous=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idRv);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting rendez-vous: " + e.getMessage());
            throw e;
        }
    }

    // Find a rendez-vous by its ID
    public rendez_vous findById(int idRv) throws SQLException {
        String sql = "SELECT * FROM rendez_vous WHERE id_rendez_vous=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idRv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error finding rendez-vous by ID: " + e.getMessage());
            throw e;
        }
    }

    // Get all rendez-vous
    public List<rendez_vous> afficherTous() throws SQLException {
        return getRendezVousList("SELECT * FROM rendez_vous ORDER BY date_rendez_vous DESC");
    }

    // Get all rendez-vous for a specific admin
    public List<rendez_vous> afficherParAdmin(int adminId) throws SQLException {
        String sql = "SELECT * FROM rendez_vous WHERE id_admin_createur=? ORDER BY date_rendez_vous DESC";
        return getRendezVousList(sql, adminId);
    }

    // Helper method to retrieve list of rendez_vous
    private List<rendez_vous> getRendezVousList(String sql, Object... params) throws SQLException {
        List<rendez_vous> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            // Set parameters for the query if there are any
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rendez-vous: " + e.getMessage());
            throw e;
        }
        return list;
    }

    // Map a ResultSet row to a rendez_vous object
    private rendez_vous map(ResultSet rs) throws SQLException {
        rendez_vous r = new rendez_vous();
        r.setId_rendez_vous(rs.getInt("id_rendez_vous"));
        r.setId_admin_createur(rs.getInt("id_admin_createur"));
        r.setMail_user(rs.getString("mail_user"));
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));

        Timestamp t1 = rs.getTimestamp("date_rendez_vous");
        r.setDate_rendez_vous(t1 == null ? null : t1.toLocalDateTime());

        r.setType(rs.getString("type"));
        r.setCompetence(rs.getString("competence"));
        r.setNombre_places(rs.getInt("nombre_places"));  // Nouvelle colonne
        Timestamp t2 = rs.getTimestamp("date_creation_rendez_vous");
        r.setDate_creation_rendez_vous(t2 == null ? null : t2.toLocalDateTime());

        return r;
    }
}