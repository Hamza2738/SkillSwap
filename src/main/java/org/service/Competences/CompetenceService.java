package org.service.Competences;

import org.model.Competence;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompetenceService implements ICRUD<Competence> {

    private final Connection cnx = mydatabase.getInstance().getConnection();

    @Override
    public List<Competence> getAll() throws SQLException {
        final String sql = """
            SELECT id, category, type, description, niveau, annees_experience, certification, statut
            FROM competences
            ORDER BY id DESC
        """;

        List<Competence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Competence(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("niveau"),
                        rs.getInt("annees_experience"),
                        rs.getString("certification"),
                        rs.getString("statut")
                ));
            }
        }
        return list;
    }

    @Override
    public void add(Competence c) throws SQLException {
        addAndReturnId(c);
    }

    public int addAndReturnId(Competence c) throws SQLException {
        final String sql = """
            INSERT INTO competences(category, type, description, niveau, annees_experience, certification, statut)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clean(c.getCategory()));
            ps.setString(2, clean(c.getType()));
            ps.setString(3, clean(c.getDescription()));
            ps.setString(4, clean(c.getNiveau()));
            ps.setInt(5, c.getAnneesExperience());
            ps.setString(6, cleanOrNull(c.getCertification()));
            ps.setString(7, clean(c.getStatut()));

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insertion échouée : aucune ligne insérée.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }

        throw new SQLException("Insertion OK mais impossible de récupérer l'id généré.");
    }

    @Override
    public void update(Competence c) throws SQLException {
        if (c.getId() <= 0) throw new SQLException("Update impossible : id invalide (" + c.getId() + ")");

        final String sql = """
            UPDATE competences
            SET category=?, type=?, description=?, niveau=?, annees_experience=?, certification=?, statut=?
            WHERE id=?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, clean(c.getCategory()));
            ps.setString(2, clean(c.getType()));
            ps.setString(3, clean(c.getDescription()));
            ps.setString(4, clean(c.getNiveau()));
            ps.setInt(5, c.getAnneesExperience());
            ps.setString(6, cleanOrNull(c.getCertification()));
            ps.setString(7, clean(c.getStatut()));
            ps.setInt(8, c.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne modifiée. ID introuvable : " + c.getId());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        if (id <= 0) throw new SQLException("Delete impossible : id invalide (" + id + ")");

        final String sql = "DELETE FROM competences WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne supprimée. ID introuvable : " + id);
        }
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
