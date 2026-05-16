package org.service.suivi_tache;

import org.model.suiviTache.Tache;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TacheService {

    private final Connection cnx;

    public TacheService() {
        cnx = mydatabase.getInstance().getConnection();
    }

    // ================= AJOUTER =================
    public void ajouter(Tache t) throws SQLException {

        String sql = """
                INSERT INTO tache
                (titre, description, statut, priorite, echeance, projet_id, user_id, mail_user, evaluation)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getStatut());
            ps.setString(4, t.getPriorite());

            if (t.getEcheance() != null)
                ps.setDate(5, Date.valueOf(t.getEcheance()));
            else
                ps.setNull(5, Types.DATE);

            ps.setInt(6, t.getProjetId());
            ps.setInt(7, t.getUserId());
            ps.setString(8, t.getMailUser());
            ps.setString(9, t.getEvaluation()); // ✅ NEW

            ps.executeUpdate();
        }
    }

    // ================= AFFICHER PAR PROJET =================
    public List<Tache> afficherParProjet(int projetId) throws SQLException {

        List<Tache> list = new ArrayList<>();

        String sql = "SELECT * FROM tache WHERE projet_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, projetId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Tache t = new Tache();

                    t.setId(rs.getInt("id"));
                    t.setTitre(rs.getString("titre"));
                    t.setDescription(rs.getString("description"));
                    t.setStatut(rs.getString("statut"));
                    t.setPriorite(rs.getString("priorite"));
                    t.setMailUser(rs.getString("mail_user"));
                    t.setEvaluation(rs.getString("evaluation")); // ✅ NEW

                    Date d = rs.getDate("echeance");
                    if (d != null)
                        t.setEcheance(d.toLocalDate());

                    t.setProjetId(rs.getInt("projet_id"));
                    t.setUserId(rs.getInt("user_id"));

                    list.add(t);
                }
            }
        }

        return list;
    }

    // ================= MODIFIER =================
    public void modifier(Tache t) throws SQLException {

        String sql = """
                UPDATE tache
                SET titre=?, description=?, statut=?, priorite=?, echeance=?, mail_user=?, evaluation=?
                WHERE id=?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getStatut());
            ps.setString(4, t.getPriorite());

            if (t.getEcheance() != null)
                ps.setDate(5, Date.valueOf(t.getEcheance()));
            else
                ps.setNull(5, Types.DATE);

            ps.setString(6, t.getMailUser());
            ps.setString(7, t.getEvaluation()); // ✅ NEW
            ps.setInt(8, t.getId());

            ps.executeUpdate();
        }
    }

    // ================= SUPPRIMER =================
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM tache WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================= AFFICHER TOUT =================
    public List<Tache> afficherToutes() throws SQLException {

        List<Tache> list = new ArrayList<>();

        String sql = "SELECT * FROM tache";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tache t = new Tache();

                t.setId(rs.getInt("id"));
                t.setTitre(rs.getString("titre"));
                t.setDescription(rs.getString("description"));
                t.setStatut(rs.getString("statut"));
                t.setPriorite(rs.getString("priorite"));
                t.setMailUser(rs.getString("mail_user"));
                t.setEvaluation(rs.getString("evaluation")); // ✅ NEW

                Date d = rs.getDate("echeance");
                if (d != null)
                    t.setEcheance(d.toLocalDate());

                t.setProjetId(rs.getInt("projet_id"));
                t.setUserId(rs.getInt("user_id"));

                list.add(t);
            }
        }

        return list;
    }
}