package org.service.utilisateur;

import org.model.Utilisateurs.Message;
import org.utils.mydatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageService {

    private final Connection cnx;

    // Dossier de stockage des fichiers image/vidéo
    private static final String UPLOAD_DIR = System.getProperty("user.home")
            + File.separator + "Documents"
            + File.separator + "SkillSwap"
            + File.separator + "chat_uploads";

    public MessageService() {
        this.cnx = mydatabase.getInstance().getConnection();
        // Créer le dossier si inexistant
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    //  ENVOYER — texte simple (rétrocompatible)
    // ================================================================
    public void envoyer(int idEnvoyeur, int idReceveur, String contenu) throws SQLException {
        String sql = "INSERT INTO message (id_envoyeur, id_receveur, contenu, type_message, lu) " +
                "VALUES (?, ?, ?, 'texte', FALSE)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.setString(3, contenu);
            ps.executeUpdate();
        }
    }

    // ================================================================
    //  ENVOYER — image (copie le fichier dans UPLOAD_DIR)
    // ================================================================
    public Message envoyerImage(int idEnvoyeur, int idReceveur, File imageFile) throws SQLException, IOException {
        // Copier le fichier dans le dossier uploads
        String ext      = getExtension(imageFile.getName());
        String newName  = "img_" + UUID.randomUUID() + ext;
        Path   dest     = Paths.get(UPLOAD_DIR, newName);
        Files.copy(imageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        String sql = "INSERT INTO message " +
                "(id_envoyeur, id_receveur, contenu, type_message, fichier_path, fichier_nom, fichier_taille, lu) " +
                "VALUES (?, ?, ?, 'image', ?, ?, ?, FALSE)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.setString(3, "[📷 " + imageFile.getName() + "]");
            ps.setString(4, dest.toAbsolutePath().toString());
            ps.setString(5, imageFile.getName());
            ps.setLong(6, imageFile.length());
            ps.executeUpdate();

            Message m = new Message();
            m.setIdEnvoyeur(idEnvoyeur);
            m.setIdReceveur(idReceveur);
            m.setTypeMessage("image");
            m.setFichierPath(dest.toAbsolutePath().toString());
            m.setFichierNom(imageFile.getName());
            m.setFichierTaille(imageFile.length());
            m.setContenu("[📷 " + imageFile.getName() + "]");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        }
    }

    // ================================================================
    //  ENVOYER — vidéo (copie le fichier dans UPLOAD_DIR)
    // ================================================================
    public Message envoyerVideo(int idEnvoyeur, int idReceveur, File videoFile) throws SQLException, IOException {
        String ext     = getExtension(videoFile.getName());
        String newName = "vid_" + UUID.randomUUID() + ext;
        Path   dest    = Paths.get(UPLOAD_DIR, newName);
        Files.copy(videoFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        String sql = "INSERT INTO message " +
                "(id_envoyeur, id_receveur, contenu, type_message, fichier_path, fichier_nom, fichier_taille, lu) " +
                "VALUES (?, ?, ?, 'video', ?, ?, ?, FALSE)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.setString(3, "[🎬 " + videoFile.getName() + "]");
            ps.setString(4, dest.toAbsolutePath().toString());
            ps.setString(5, videoFile.getName());
            ps.setLong(6, videoFile.length());
            ps.executeUpdate();

            Message m = new Message();
            m.setIdEnvoyeur(idEnvoyeur);
            m.setIdReceveur(idReceveur);
            m.setTypeMessage("video");
            m.setFichierPath(dest.toAbsolutePath().toString());
            m.setFichierNom(videoFile.getName());
            m.setFichierTaille(videoFile.length());
            m.setContenu("[🎬 " + videoFile.getName() + "]");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        }
    }

    // ================================================================
    //  ENVOYER — message vocal (stocké en BLOB dans la BDD)
    // ================================================================
    public Message envoyerVocal(int idEnvoyeur, int idReceveur,
                                byte[] audioData, float dureeSecondes) throws SQLException {
        String sql = "INSERT INTO message " +
                "(id_envoyeur, id_receveur, contenu, type_message, audio_data, duree_vocal, lu) " +
                "VALUES (?, ?, ?, 'vocal', ?, ?, FALSE)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.setString(3, "[🎤 Message vocal]");
            ps.setBytes(4, audioData);
            ps.setFloat(5, dureeSecondes);
            ps.executeUpdate();

            Message m = new Message();
            m.setIdEnvoyeur(idEnvoyeur);
            m.setIdReceveur(idReceveur);
            m.setTypeMessage("vocal");
            m.setAudioData(audioData);
            m.setDureeVocal(dureeSecondes);
            m.setContenu("[🎤 Message vocal]");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        }
    }

    // ================================================================
    //  CONVERSATION entre deux utilisateurs
    // ================================================================
    public List<Message> getConversation(int idA, int idB) throws SQLException {
        String sql = "SELECT * FROM message " +
                "WHERE (id_envoyeur=? AND id_receveur=?) " +
                "OR    (id_envoyeur=? AND id_receveur=?) " +
                "ORDER BY date_envoi ASC";
        List<Message> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idA); ps.setInt(2, idB);
            ps.setInt(3, idB); ps.setInt(4, idA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ================================================================
    //  MESSAGES NON LUS
    // ================================================================
    public int getNonLus(int idEnvoyeur, int idReceveur) throws SQLException {
        String sql = "SELECT COUNT(*) FROM message " +
                "WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ================================================================
    //  MARQUER COMME LUS
    // ================================================================
    public void marquerLus(int idEnvoyeur, int idReceveur) throws SQLException {
        String sql = "UPDATE message SET lu=TRUE " +
                "WHERE id_envoyeur=? AND id_receveur=? AND lu=FALSE";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.executeUpdate();
        }
    }

    // ================================================================
    //  SUPPRIMER un message
    // ================================================================
    public void supprimer(int idMessage) throws SQLException {
        // Supprimer le fichier physique si présent
        try {
            String sql = "SELECT fichier_path, type_message FROM message WHERE id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, idMessage);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String path = rs.getString("fichier_path");
                        String type = rs.getString("type_message");
                        // APRÈS
                        if (("image".equals(type) || "video".equals(type) || "fichier".equals(type))
                                && path != null && !path.isBlank()) {
                            Files.deleteIfExists(Paths.get(path));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sql = "DELETE FROM message WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idMessage);
            ps.executeUpdate();
        }
    }

    // ================================================================
    //  MAPPER ResultSet → Message
    // ================================================================
    private Message map(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setIdEnvoyeur(rs.getInt("id_envoyeur"));
        m.setIdReceveur(rs.getInt("id_receveur"));
        m.setContenu(rs.getString("contenu"));
        m.setLu(rs.getBoolean("lu"));

        Timestamp ts = rs.getTimestamp("date_envoi");
        if (ts != null) m.setDateEnvoi(ts.toLocalDateTime());

        // Colonnes média (peuvent être NULL si anciens messages)
        try {
            String type = rs.getString("type_message");
            m.setTypeMessage(type != null ? type : "texte");

            m.setFichierPath(rs.getString("fichier_path"));
            m.setFichierNom(rs.getString("fichier_nom"));
            m.setFichierTaille(rs.getLong("fichier_taille"));
            m.setDureeVocal(rs.getFloat("duree_vocal"));

            // Charger le BLOB audio seulement pour les messages vocaux
            if ("vocal".equals(type)) {
                byte[] audio = rs.getBytes("audio_data");
                m.setAudioData(audio);
            }
        } catch (SQLException ignored) {
            // Colonnes pas encore présentes → rétrocompatibilité
        }

        return m;
    }
    // ================================================================
//  ENVOYER — fichier (PDF, Word, Excel, PPTX...)
// ================================================================
    public Message envoyerFichier(int idEnvoyeur, int idReceveur, File fichier) throws SQLException, IOException {
        String ext     = getExtension(fichier.getName());
        String newName = "file_" + UUID.randomUUID() + ext;
        Path   dest    = Paths.get(UPLOAD_DIR, newName);
        Files.copy(fichier.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        String sql = "INSERT INTO message " +
                "(id_envoyeur, id_receveur, contenu, type_message, fichier_path, fichier_nom, fichier_taille, lu) " +
                "VALUES (?, ?, ?, 'fichier', ?, ?, ?, FALSE)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEnvoyeur);
            ps.setInt(2, idReceveur);
            ps.setString(3, "[📎 " + fichier.getName() + "]");
            ps.setString(4, dest.toAbsolutePath().toString());
            ps.setString(5, fichier.getName());
            ps.setLong(6, fichier.length());
            ps.executeUpdate();

            Message m = new Message();
            m.setIdEnvoyeur(idEnvoyeur);
            m.setIdReceveur(idReceveur);
            m.setTypeMessage("fichier");
            m.setFichierPath(dest.toAbsolutePath().toString());
            m.setFichierNom(fichier.getName());
            m.setFichierTaille(fichier.length());
            m.setContenu("[📎 " + fichier.getName() + "]");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        }
    }

    // ================================================================
    //  HELPER
    // ================================================================
    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i == -1 ? ".bin" : fileName.substring(i).toLowerCase();
    }

    public String getUploadDir() {
        return UPLOAD_DIR;
    }
}