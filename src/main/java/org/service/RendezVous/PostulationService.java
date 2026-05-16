package org.service.RendezVous;

import org.model.RendezVous.postulation;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulationService {

    private final Connection cnx;

    public PostulationService() {
        cnx = mydatabase.getInstance().getConnection();
    }

    // Correction de la méthode countPostulationsForRendezVous
    public int countPostulationsForRendezVous(int idRendezVous) throws SQLException {
        String query = "SELECT COUNT(*) FROM postulation WHERE id_rendez_vous = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) { // Utilisation de `cnx` au lieu de `connection`
            ps.setInt(1, idRendezVous);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);  // Retourne le nombre de postulations
            }
        }
        return 0;
    }

    // ================= AJOUT =================
    public void ajouter(postulation p) throws SQLException {
        String sql = "INSERT INTO postulation " +
                "(id_rendez_vous, id_postulant, mail_user, message, cv, status, role, nom, prenom, titre) " +  // Ajout du champ 'titre'
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getId_rendez_vous());
            ps.setInt(2, p.getId_postulant());
            ps.setString(3, p.getMail_user());
            ps.setString(4, p.getMessage());
            ps.setString(5, p.getCv());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getRole());
            ps.setString(8, p.getNom());
            ps.setString(9, p.getPrenom());
            ps.setString(10, p.getTitre());  // Insertion du titre dans la base de données

            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM postulation WHERE id_postulation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<postulation> afficherParUser(int idUser) throws SQLException {
        String sql = "SELECT * FROM postulation WHERE id_postulant=? ORDER BY date_postulation DESC";
        List<postulation> list = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUser);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    private postulation map(ResultSet rs) throws SQLException {
        postulation p = new postulation();

        p.setId_postulation(rs.getInt("id_postulation"));
        p.setId_rendez_vous(rs.getInt("id_rendez_vous"));
        p.setId_postulant(rs.getInt("id_postulant"));
        p.setMail_user(rs.getString("mail_user"));
        p.setMessage(rs.getString("message"));
        p.setCv(rs.getString("cv"));
        p.setStatus(rs.getString("status"));
        p.setRole(rs.getString("role"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));

        Timestamp t = rs.getTimestamp("date_postulation");
        if (t != null)
            p.setDate_postulation(t.toLocalDateTime());

        return p;
    }

    // ================= AFFICHER PAR RENDEZ-VOUS =================
    public List<postulation> afficherParRendezVous(int idRv) throws SQLException {
        String sql = "SELECT * FROM postulation WHERE id_rendez_vous=? ORDER BY date_postulation DESC";
        List<postulation> list = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idRv);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    // ================= CHANGER STATUS =================
    public void changerStatus(int idPostulation, String status) throws SQLException {
        String sql = "UPDATE postulation SET status=? WHERE id_postulation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, idPostulation);
            ps.executeUpdate();
        }
    }
}