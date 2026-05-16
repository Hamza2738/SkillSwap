package org.service.Competences;

import org.model.Competences.Competence;
import org.service.Competences.Historiques.HistoriqueCompetenceLogger;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompetenceService implements ICRUD<Competence> {

    private final Connection cnx = mydatabase.getInstance().getConnection();
    private final HistoriqueCompetenceLogger histLogger = new HistoriqueCompetenceLogger();

    // =========================
    // ADMIN/BACK: Tout afficher
    // =========================
    @Override
    public List<Competence> getAll() throws SQLException {
        final String sql = """
            SELECT id, category, type, description, niveau, annees_experience, certification, statut, email
            FROM competences
            ORDER BY id DESC
        """;

        List<Competence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    // =========================
    // FRONT: Compétences du user connecté
    // =========================
    public List<Competence> findByUserId(int userId) throws SQLException {
        if (userId <= 0) throw new SQLException("UserId invalide : " + userId);

        final String sql = """
            SELECT c.id, c.category, c.type, c.description, c.niveau, c.annees_experience, c.certification, c.statut, c.email
            FROM competences c
            JOIN utilisateur_competence uc ON uc.id_competence = c.id
            WHERE uc.id_utilisateur = ?
            ORDER BY c.id DESC
        """;

        List<Competence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    // =========================
    // ADD (ancienne méthode)
    // =========================
    @Override
    public void add(Competence c) throws SQLException {
        addAndReturnId(c);
    }

    // =========================
    // ADD simple (sans lien user_competence)
    // (utile pour admin/back)
    // =========================
    public int addAndReturnId(Competence c) throws SQLException {
        final String sql = """
            INSERT INTO competences(category, type, description, niveau, annees_experience, certification, statut, email)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clean(c.getCategory()));
            ps.setString(2, clean(c.getType()));
            ps.setString(3, clean(c.getDescription()));
            ps.setString(4, clean(c.getNiveau()));
            ps.setInt(5, c.getAnneesExperience());
            ps.setString(6, cleanOrNull(c.getCertification()));
            ps.setString(7, clean(c.getStatut()));
            ps.setString(8, clean(c.getEmail()));

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insertion échouée : aucune ligne insérée.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);

                    c.setId(newId);
                    histLogger.logAction(c, "Ajoutée");

                    return newId;
                }
            }
        }

        throw new SQLException("Insertion OK mais impossible de récupérer l'id généré.");
    }

    // =========================
    // ✅ ADD FRONT (avec lien utilisateur_competence)
    // =========================
    public int addForUserAndReturnId(int userId, Competence c) throws SQLException {
        if (userId <= 0) throw new SQLException("UserId invalide : " + userId);

        final String sqlInsert = """
            INSERT INTO competences(category, type, description, niveau, annees_experience, certification, statut, email)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        final String sqlLink = """
            INSERT INTO utilisateur_competence(id_utilisateur, id_competence)
            VALUES (?, ?)
        """;

        boolean oldAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);

        try (PreparedStatement ps = cnx.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, clean(c.getCategory()));
            ps.setString(2, clean(c.getType()));
            ps.setString(3, clean(c.getDescription()));
            ps.setString(4, clean(c.getNiveau()));
            ps.setInt(5, c.getAnneesExperience());
            ps.setString(6, cleanOrNull(c.getCertification()));
            ps.setString(7, clean(c.getStatut()));
            ps.setString(8, clean(c.getEmail())); // email auto depuis Session dans le controller

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insertion échouée : aucune ligne insérée.");

            int newId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Impossible de récupérer l'id généré.");
                newId = keys.getInt(1);
            }

            try (PreparedStatement ps2 = cnx.prepareStatement(sqlLink)) {
                ps2.setInt(1, userId);
                ps2.setInt(2, newId);
                ps2.executeUpdate();
            }

            cnx.commit();

            c.setId(newId);
            histLogger.logAction(c, "Ajoutée");

            return newId;

        } catch (SQLException ex) {
            cnx.rollback();
            throw ex;
        } finally {
            cnx.setAutoCommit(oldAutoCommit);
        }
    }

    // =========================
    // UPDATE
    // =========================
    @Override
    public void update(Competence c) throws SQLException {
        if (c.getId() <= 0) throw new SQLException("Update impossible : id invalide (" + c.getId() + ")");

        final String sql = """
            UPDATE competences
            SET category=?, type=?, description=?, niveau=?, annees_experience=?, certification=?, statut=?, email=?
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
            ps.setString(8, clean(c.getEmail()));
            ps.setInt(9, c.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne modifiée. ID introuvable : " + c.getId());
        }

        histLogger.logAction(c, "Modifiée");
    }

    // =========================
    // DELETE
    // =========================
    @Override
    public void delete(int id) throws SQLException {
        if (id <= 0) throw new SQLException("Delete impossible : id invalide (" + id + ")");

        Competence beforeDelete = getById(id);

        final String sql = "DELETE FROM competences WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne supprimée. ID introuvable : " + id);
        }

        if (beforeDelete != null) {
            histLogger.logAction(beforeDelete, "Supprimée");
        }
    }

    // =========================
    // GET BY ID
    // =========================
    private Competence getById(int id) throws SQLException {
        final String sql = """
            SELECT id, category, type, description, niveau, annees_experience, certification, statut, email
            FROM competences
            WHERE id=?
            LIMIT 1
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Competence> getByEmail(String email) throws SQLException {
        final String sql = """
        SELECT id, category, type, description, niveau, annees_experience, certification, statut, email
        FROM competences
        WHERE email = ?
        ORDER BY id DESC
    """;

        List<Competence> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Competence(
                            rs.getInt("id"),
                            rs.getString("category"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getString("niveau"),
                            rs.getInt("annees_experience"),
                            rs.getString("certification"),
                            rs.getString("statut"),
                            rs.getString("email")
                    ));
                }
            }
        }
        return list;
    }

    // =========================
    // MAPPER
    // =========================
    private Competence map(ResultSet rs) throws SQLException {
        return new Competence(
                rs.getInt("id"),
                rs.getString("category"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getString("niveau"),
                rs.getInt("annees_experience"),
                rs.getString("certification"),
                rs.getString("statut"),
                rs.getString("email")
        );
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