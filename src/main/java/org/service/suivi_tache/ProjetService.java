package org.service.suivi_tache;

import org.model.suiviTache.Projet;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjetService {

    private final Connection cnx;

    public ProjetService() {
        cnx = mydatabase.getInstance().getConnection();
    }

    // 🔹 Ajouter
    public void ajouter(Projet p) throws SQLException {

        String sql = "INSERT INTO projet(titre, description, date_debut, date_fin, statut, user_id, mail_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());

            if (p.getDateDebut() != null) ps.setDate(3, Date.valueOf(p.getDateDebut()));
            else ps.setNull(3, Types.DATE);

            if (p.getDateFin() != null) ps.setDate(4, Date.valueOf(p.getDateFin()));
            else ps.setNull(4, Types.DATE);

            ps.setString(5, p.getStatut());
            ps.setInt(6, p.getUserId());
            ps.setString(7, p.getMailUser());

            ps.executeUpdate();
        }
    }

    // 🔹 Afficher tous les projets d’un utilisateur
    public List<Projet> afficherParUtilisateur(int userId) throws SQLException {

        List<Projet> list = new ArrayList<>();
        String sql = "SELECT * FROM projet WHERE user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Projet p = new Projet();
                    p.setId(rs.getInt("id"));
                    p.setTitre(rs.getString("titre"));
                    p.setDescription(rs.getString("description"));
                    p.setMailUser(rs.getString("mail_user"));

                    Date d1 = rs.getDate("date_debut");
                    if (d1 != null) p.setDateDebut(d1.toLocalDate());

                    Date d2 = rs.getDate("date_fin");
                    if (d2 != null) p.setDateFin(d2.toLocalDate());

                    p.setStatut(rs.getString("statut"));
                    p.setUserId(rs.getInt("user_id"));

                    list.add(p);
                }
            }
        }

        return list;
    }

    // 🔹 Trouver un projet par ID (pour charger toutes les infos au clic)
    public Projet findById(int id) throws SQLException {

        String sql = "SELECT * FROM projet WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Projet p = new Projet();
                    p.setId(rs.getInt("id"));
                    p.setTitre(rs.getString("titre"));
                    p.setDescription(rs.getString("description"));
                    p.setMailUser(rs.getString("mail_user"));

                    Date d1 = rs.getDate("date_debut");
                    if (d1 != null) p.setDateDebut(d1.toLocalDate());

                    Date d2 = rs.getDate("date_fin");
                    if (d2 != null) p.setDateFin(d2.toLocalDate());

                    p.setStatut(rs.getString("statut"));
                    p.setUserId(rs.getInt("user_id"));

                    return p;
                }
            }
        }

        return null;
    }
    // ✅ Ajouter cette méthode dans ProjetService.java
    public List<Projet> afficherParEmail(String email) throws SQLException {
        List<Projet> list = new ArrayList<>();
        String sql = "SELECT * FROM projet WHERE LOWER(mail_user) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Projet p = new Projet();
                    p.setId(rs.getInt("id"));
                    p.setTitre(rs.getString("titre"));
                    p.setDescription(rs.getString("description"));
                    p.setUserId(rs.getInt("user_id"));
                    p.setMailUser(rs.getString("mail_user"));
                    Date d1 = rs.getDate("date_debut");
                    if (d1 != null) p.setDateDebut(d1.toLocalDate());
                    Date d2 = rs.getDate("date_fin");
                    if (d2 != null) p.setDateFin(d2.toLocalDate());
                    p.setStatut(rs.getString("statut"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    // 🔹 Modifier
    public void modifier(Projet p) throws SQLException {

        String sql = "UPDATE projet SET titre=?, description=?, date_debut=?, date_fin=?, statut=?, mail_user=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());

            if (p.getDateDebut() != null) ps.setDate(3, Date.valueOf(p.getDateDebut()));
            else ps.setNull(3, Types.DATE);

            if (p.getDateFin() != null) ps.setDate(4, Date.valueOf(p.getDateFin()));
            else ps.setNull(4, Types.DATE);

            ps.setString(5, p.getStatut());
            ps.setString(6, p.getMailUser());  // ✅ 6 = mail_user
            ps.setInt(7, p.getId());           // ✅ 7 = id

            ps.executeUpdate();
        }
    }

    // 🔹 Supprimer (et ses tâches)
    public void supprimer(int id) throws SQLException {

        // supprimer d'abord les tâches liées
        try (PreparedStatement ps1 = cnx.prepareStatement("DELETE FROM tache WHERE projet_id=?")) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
        }

        // puis le projet
        try (PreparedStatement ps2 = cnx.prepareStatement("DELETE FROM projet WHERE id=?")) {
            ps2.setInt(1, id);
            ps2.executeUpdate();
        }
    }
    public List<Projet> afficherTous() throws SQLException {
        List<Projet> list = new ArrayList<>();
        String sql = "SELECT * FROM projet";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Projet p = new Projet();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setDescription(rs.getString("description"));
                p.setUserId(rs.getInt("user_id"));
                p.setMailUser(rs.getString("mail_user"));
                Date d1 = rs.getDate("date_debut");
                if (d1 != null) p.setDateDebut(d1.toLocalDate());
                Date d2 = rs.getDate("date_fin");
                if (d2 != null) p.setDateFin(d2.toLocalDate());
                p.setStatut(rs.getString("statut"));
                list.add(p);
            }
        }
        return list;
    }
}