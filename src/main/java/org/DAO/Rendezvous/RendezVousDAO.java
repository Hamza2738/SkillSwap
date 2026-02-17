package org.DAO.Rendezvous;

import org.model.RendezVous.rendez_vous;
import org.utils.mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    public List<rendez_vous> findAll() {
        String sql = "SELECT * FROM rendez_vous ORDER BY date_creation_rendez_vous DESC";
        List<rendez_vous> list = new ArrayList<>();

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) list.add(mapRDV(rs));
            }
        } catch (SQLException e) {
            System.out.println("findAll RDV: " + e.getMessage());
        }
        return list;
    }

    private rendez_vous mapRDV(ResultSet rs) throws SQLException {
        rendez_vous r = new rendez_vous();
        r.setId_rendez_vous(rs.getInt("id_rendez_vous"));
        r.setId_admin_createur(rs.getInt("id_admin_createur"));
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));
        Timestamp dr = rs.getTimestamp("date_rendez_vous");
        if (dr != null) r.setDate_rendez_vous(dr.toLocalDateTime());
        r.setType(rs.getString("type"));
        r.setCompetence(rs.getString("competence"));
        Timestamp dc = rs.getTimestamp("date_creation_rendez_vous");
        if (dc != null) r.setDate_creation_rendez_vous(dc.toLocalDateTime());
        return r;
    }
}
