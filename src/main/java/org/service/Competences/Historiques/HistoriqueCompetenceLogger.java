package org.service.Competences.Historiques;

import org.model.Competences.Competence;
import org.utils.mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class HistoriqueCompetenceLogger {

    private final Connection cnx = mydatabase.getInstance().getConnection();

    public void logAction(Competence c, String action) throws SQLException {
        if (c == null) return;

        final String sql = """
        INSERT INTO historiques_competences
        (competence_id, category, type, description, niveau, annees_experience,
         certification, statut, email, status_change, date_modification)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {


            if ("Supprimée".equals(action)) {
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
            ps.setString(10, action);

            ps.executeUpdate();
        }
    }


    private static String clean(String s) { return s == null ? "" : s.trim(); }

    private static String cleanOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
