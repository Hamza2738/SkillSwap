package org.DAO.Competence;

import org.utils.mydatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CompetenceDAO {


    public void addCompetenceToUser(int idUser, int idComp) {

        String sql = "INSERT INTO utilisateur_competence (id_utilisateur, id_competence) VALUES (?, ?)";

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setInt(1, idUser);
            ps.setInt(2, idComp);

            ps.executeUpdate();

            System.out.println("Compétence ajoutée à l'utilisateur");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ Supprimer une compétence
    public void removeCompetenceFromUser(int idUser, int idComp) {

        String sql = "DELETE FROM utilisateur_competence WHERE id_utilisateur=? AND id_competence=?";

        try {
            Connection cn = mydatabase.getInstance().getConnection();
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setInt(1, idUser);
            ps.setInt(2, idComp);

            ps.executeUpdate();

            System.out.println("Compétence supprimée");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
