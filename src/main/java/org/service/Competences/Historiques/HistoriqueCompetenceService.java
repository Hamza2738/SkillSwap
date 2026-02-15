package org.service.Competences.Historiques;

import org.model.Competences.Competence;
import org.model.Competences.HistoriqueCompetence;
import org.service.Competences.ICRUD;
import org.utils.mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueCompetenceService implements ICRUD<HistoriqueCompetence> {

    private final Connection cnx = mydatabase.getInstance().getConnection();


    @Override
    public void add(HistoriqueCompetence h) throws SQLException {
        if (h == null) return;

        final String sql = """
            INSERT INTO historiques_competences
            (competence_id, category, type, description, niveau, annees_experience,
             certification, statut, email, status_change, date_modification)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (h.getCompetenceId() == 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, h.getCompetenceId());
            }

            ps.setString(2, clean(h.getCategory()));
            ps.setString(3, clean(h.getType()));
            ps.setString(4, clean(h.getDescription()));
            ps.setString(5, clean(h.getNiveau()));
            ps.setInt(6, h.getAnneesExperience());
            ps.setString(7, cleanOrNull(h.getCertification()));
            ps.setString(8, clean(h.getStatut()));
            ps.setString(9, clean(h.getEmail()));
            ps.setString(10, clean(h.getStatusChange()));

            ps.executeUpdate();
        }
    }

    @Override
    public void update(HistoriqueCompetence h) throws SQLException {
        if (h == null) return;

        final String sql = """
            UPDATE historiques_competences
            SET competence_id = ?,
                category = ?,
                type = ?,
                description = ?,
                niveau = ?,
                annees_experience = ?,
                certification = ?,
                statut = ?,
                email = ?,
                status_change = ?,
                date_modification = SYSTIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (h.getCompetenceId() == 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, h.getCompetenceId());
            }

            ps.setString(2, clean(h.getCategory()));
            ps.setString(3, clean(h.getType()));
            ps.setString(4, clean(h.getDescription()));
            ps.setString(5, clean(h.getNiveau()));
            ps.setInt(6, h.getAnneesExperience());
            ps.setString(7, cleanOrNull(h.getCertification()));
            ps.setString(8, clean(h.getStatut()));
            ps.setString(9, clean(h.getEmail()));
            ps.setString(10, clean(h.getStatusChange()));
            ps.setInt(11, h.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        final String sql = "DELETE FROM historiques_competences WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<HistoriqueCompetence> getAll() throws SQLException {
        final String sql = """
            SELECT id, competence_id, category, type, description, niveau, annees_experience,
                   certification, statut, email, status_change, date_modification
            FROM historiques_competences
            ORDER BY date_modification DESC, id DESC
        """;

        List<HistoriqueCompetence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_modification");
                LocalDateTime dt = (ts == null) ? null : ts.toLocalDateTime();

                list.add(new HistoriqueCompetence(
                        rs.getInt("id"),
                        rs.getInt("competence_id"),
                        rs.getString("category"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("niveau"),
                        rs.getInt("annees_experience"),
                        rs.getString("certification"),
                        rs.getString("statut"),
                        rs.getString("email"),
                        rs.getString("status_change"),
                        dt
                ));
            }
        }
        return list;
    }


    public void logAction(Competence c, String action) throws SQLException {
        if (c == null) return;

        final String sql = """
            INSERT INTO historiques_competences
            (competence_id, category, type, description, niveau, annees_experience,
             certification, statut, email, status_change, date_modification)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if ("Supprimée".equalsIgnoreCase(action)) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, c.getId());
            }

            ps.setString(2, clean(c.getCategory()));
            ps.setString(3, clean(c.getType()));
            ps.setString(4, clean(c.getDescription()));
            ps.setString(5, clean(c.getNiveau()));
            ps.setInt(6, c.getAnneesExperience());
            ps.setString(7, cleanOrNull(c.getCertification()));
            ps.setString(8, clean(c.getStatut()));
            ps.setString(9, clean(c.getEmail()));
            ps.setString(10, clean(action));

            ps.executeUpdate();
        }
    }


    public List<HistoriqueCompetence> getByCompetenceId(int competenceId) throws SQLException {
        final String sql = """
            SELECT id, competence_id, category, type, description, niveau, annees_experience,
                   certification, statut, email, status_change, date_modification
            FROM historiques_competences
            WHERE competence_id = ?
            ORDER BY date_modification DESC, id DESC
        """;

        List<HistoriqueCompetence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, competenceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("date_modification");
                    LocalDateTime dt = (ts == null) ? null : ts.toLocalDateTime();

                    list.add(new HistoriqueCompetence(
                            rs.getInt("id"),
                            rs.getInt("competence_id"),
                            rs.getString("category"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getString("niveau"),
                            rs.getInt("annees_experience"),
                            rs.getString("certification"),
                            rs.getString("statut"),
                            rs.getString("email"),
                            rs.getString("status_change"),
                            dt
                    ));
                }
            }
        }
        return list;
    }


    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private static String cleanOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
