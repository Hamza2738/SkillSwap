package org.DAO.Rendezvous;

import org.model.RendezVous.postulation;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulationDAO {

    public int insert(postulation p) {
        String sql = """
            INSERT INTO postulation (id_rendez_vous, id_postulant, message, status, competence_offerte, date_postulation)
            VALUES (?, ?, ?, ?, ?, NOW())
        """;

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getId_rendez_vous());
                ps.setInt(2, p.getId_postulant());
                ps.setString(3, p.getMessage());
                ps.setString(4, p.getStatus() == null ? "en_attente" : p.getStatus());
                ps.setString(5, p.getCompetence_offerte());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("insert postulation: " + e.getMessage());
        }
        return -1;
    }

    public List<postulation> findByRendezVous(int idRdv) {
        String sql = "SELECT * FROM postulation WHERE id_rendez_vous=? ORDER BY date_postulation DESC";
        List<postulation> list = new ArrayList<>();

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idRdv);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapPostulation(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("findByRendezVous: " + e.getMessage());
        }
        return list;
    }

    private postulation mapPostulation(ResultSet rs) throws SQLException {
        postulation p = new postulation();
        p.setId_postulation(rs.getInt("id_postulation"));
        p.setId_rendez_vous(rs.getInt("id_rendez_vous"));
        p.setId_postulant(rs.getInt("id_postulant"));
        p.setMessage(rs.getString("message"));
        p.setStatus(rs.getString("status"));
        p.setCompetence_offerte(rs.getString("competence_offerte"));
        Timestamp ts = rs.getTimestamp("date_postulation");
        if (ts != null) p.setDate_postulation(ts.toLocalDateTime());
        return p;
    }
}
