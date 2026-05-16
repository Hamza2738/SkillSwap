package org.service.Offres;

import org.model.Offres.Candidature;
import org.model.Offres.Commentaire;
import org.model.Offres.Offre;
import org.utils.mydatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OffreService {

    private final Connection conn = mydatabase.getInstance().getConnection();

    // ════════════════════════════════════════════════════════════
    //  OFFRES — CRUD
    // ════════════════════════════════════════════════════════════

    /** Ajouter une offre (matricule généré automatiquement) */
    public void ajouter(Offre o) throws SQLException {
        String sql = """
            INSERT INTO offre
              (titre, description, type_offre, budget, prix, duree,
               localisation, date_publication, date_limite, statut,
               id_utilisateur, tags)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  o.getTitre());
            ps.setString(2,  o.getDescription());
            ps.setString(3,  o.getTypeOffre());
            ps.setBigDecimal(4, o.getBudget());
            ps.setBigDecimal(5, o.getPrix());
            ps.setInt(6,     o.getDuree());
            ps.setString(7,  o.getLocalisation());
            ps.setDate(8,    o.getDatePublication() != null
                    ? Date.valueOf(o.getDatePublication()) : Date.valueOf(LocalDate.now()));
            ps.setDate(9,    o.getDateLimite() != null
                    ? Date.valueOf(o.getDateLimite()) : null);
            ps.setString(10, o.getStatut() != null ? o.getStatut() : "ouverte");
            ps.setInt(11,    o.getIdUtilisateur());
            ps.setString(12, o.getTags());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                o.setIdOffre(id);
                String mat = genererMatricule(id, o.getDatePublication());
                o.setMatricule(mat);
                majMatricule(id, mat);
            }
        }
    }

    /** Mettre à jour une offre */
    public void modifier(Offre o) throws SQLException {
        String sql = """
            UPDATE offre SET
              titre=?, description=?, type_offre=?, budget=?, prix=?,
              duree=?, localisation=?, date_publication=?,
              date_limite=?, statut=?, tags=?
            WHERE id_offre=?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  o.getTitre());
            ps.setString(2,  o.getDescription());
            ps.setString(3,  o.getTypeOffre());
            ps.setBigDecimal(4, o.getBudget());
            ps.setBigDecimal(5, o.getPrix());
            ps.setInt(6,     o.getDuree());
            ps.setString(7,  o.getLocalisation());
            ps.setDate(8,    o.getDatePublication() != null
                    ? Date.valueOf(o.getDatePublication()) : null);
            ps.setDate(9,    o.getDateLimite() != null
                    ? Date.valueOf(o.getDateLimite()) : null);
            ps.setString(10, o.getStatut());
            ps.setString(11, o.getTags());
            ps.setInt(12,    o.getIdOffre());
            ps.executeUpdate();
        }
    }

    /** Supprimer une offre */
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM offre WHERE id_offre=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Incrémenter les vues */
    public void incrementerVues(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE offre SET vues = vues + 1 WHERE id_offre=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /** Changer le statut ouverte/fermee */
    public void changerStatut(int id, String statut) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE offre SET statut=? WHERE id_offre=?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ── Listes ──────────────────────────────────────────────────

    public List<Offre> afficherToutes() throws SQLException {
        return lire("SELECT * FROM offre ORDER BY date_publication DESC");
    }

    public List<Offre> afficherOuvertes() throws SQLException {
        return lire("SELECT * FROM offre WHERE statut='ouverte' ORDER BY date_publication DESC");
    }

    public List<Offre> afficherParType(String type) throws SQLException {
        String sql = "SELECT * FROM offre WHERE type_offre=? ORDER BY date_publication DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            return lirePs(ps);
        }
    }

    public List<Offre> rechercherParTitre(String q) throws SQLException {
        String sql = "SELECT * FROM offre WHERE titre LIKE ? OR tags LIKE ? OR description LIKE ? ORDER BY date_publication DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            return lirePs(ps);
        }
    }

    public Offre findById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM offre WHERE id_offre=?")) {
            ps.setInt(1, id);
            List<Offre> list = lirePs(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  LIKES
    // ════════════════════════════════════════════════════════════

    /**
     * Ajoute un like si pas encore liké, retire le like sinon (toggle).
     * @return true si l'offre est maintenant likée, false si le like a été retiré.
     */
    public boolean toggleLike(int idOffre, int idUtilisateur) throws SQLException {
        if (aDejaLike(idOffre, idUtilisateur)) {
            // Retirer le like
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM offre_like WHERE id_offre=? AND id_utilisateur=?")) {
                ps.setInt(1, idOffre);
                ps.setInt(2, idUtilisateur);
                ps.executeUpdate();
            }
            // Décrémenter nb_likes (minimum 0)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE offre SET nb_likes = GREATEST(nb_likes - 1, 0) WHERE id_offre=?")) {
                ps.setInt(1, idOffre);
                ps.executeUpdate();
            }
            return false;
        } else {
            // Ajouter le like
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO offre_like (id_offre, id_utilisateur) VALUES (?,?)")) {
                ps.setInt(1, idOffre);
                ps.setInt(2, idUtilisateur);
                ps.executeUpdate();
            }
            // Incrémenter nb_likes
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE offre SET nb_likes = nb_likes + 1 WHERE id_offre=?")) {
                ps.setInt(1, idOffre);
                ps.executeUpdate();
            }
            return true;
        }
    }

    /** Vérifie si un utilisateur a déjà liké une offre */
    public boolean aDejaLike(int idOffre, int idUtilisateur) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM offre_like WHERE id_offre=? AND id_utilisateur=?")) {
            ps.setInt(1, idOffre);
            ps.setInt(2, idUtilisateur);
            return ps.executeQuery().next();
        }
    }

    /** Retourne le nombre de likes d'une offre */
    public int getNbLikes(int idOffre) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nb_likes FROM offre WHERE id_offre=?")) {
            ps.setInt(1, idOffre);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
// ════════════════════════════════════════════════════════════
//  COMMENTAIRES
// ════════════════════════════════════════════════════════════

    /** Ajouter un commentaire sur une offre */
    public Commentaire ajouterCommentaire(int idOffre, int idUtilisateur,
                                          String contenu) throws SQLException {
        if (contenu == null || contenu.isBlank())
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide.");

        LocalDateTime now = LocalDateTime.now();

        String sql = """
        INSERT INTO commentaire (id_offre, id_utilisateur, contenu, date_commentaire)
        VALUES (?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idOffre);
            ps.setInt(2, idUtilisateur);
            ps.setString(3, contenu.trim());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            ps.executeUpdate();

            // Incrémenter nb_commentaires si la colonne existe
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE offre SET nb_commentaires = nb_commentaires + 1 WHERE id_offre=?")) {
                ps2.setInt(1, idOffre);
                ps2.executeUpdate();
            } catch (SQLException ignored) {}

            Commentaire c = new Commentaire();
            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                c.setIdCommentaire(rs.getInt(1));
            }

            c.setIdOffre(idOffre);
            c.setIdUtilisateur(idUtilisateur);
            c.setContenu(contenu.trim());
            c.setDateCommentaire(now);

            return c;
        }
    }

    /** Récupérer tous les commentaires d'une offre avec nom auteur */
    public List<Commentaire> getCommentairesParOffre(int idOffre) throws SQLException {
        String sql = """
        SELECT c.*, u.nom AS nom_auteur, u.prenom AS prenom_auteur
        FROM commentaire c
        JOIN utilisateur u ON c.id_utilisateur = u.id_utilisateur
        WHERE c.id_offre = ?
        ORDER BY c.date_commentaire DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOffre);
            return lireCommentaires(ps);
        }
    }

    /** Supprimer un commentaire */
    public void supprimerCommentaire(int idCommentaire, int idOffre) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM commentaire WHERE id_commentaire=?")) {
            ps.setInt(1, idCommentaire);
            ps.executeUpdate();
        }

        // Recalculer nb_commentaires si la colonne existe
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE offre SET nb_commentaires = (SELECT COUNT(*) FROM commentaire WHERE id_offre=?) WHERE id_offre=?")) {
            ps.setInt(1, idOffre);
            ps.setInt(2, idOffre);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
    // ════════════════════════════════════════════════════════════
    //  CANDIDATURES
    // ════════════════════════════════════════════════════════════

    public Candidature souscrire(int idUtilisateur, int idOffre,
                                 String mailAcheteur, String message,
                                 String lettreMotivation, String cvPath,
                                 String modePaiement) throws SQLException {

        Offre offre = findById(idOffre);
        if (offre == null) throw new IllegalArgumentException("Offre introuvable : " + idOffre);

        LocalDateTime now        = LocalDateTime.now();
        LocalDateTime expiration = now.plusDays(offre.getDuree());
        String refPaiement       = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String sql = """
            INSERT INTO candidature
              (date_candidature, date_abonnement, date_expiration, matricule_offre,
               mail_acheteur, prix_paye, mode_paiement, reference_paiement,
               statut, message, cv, lettre_motivation, is_active,
               id_utilisateur, id_offre)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1,  Timestamp.valueOf(now));
            ps.setTimestamp(2,  Timestamp.valueOf(now));
            ps.setTimestamp(3,  Timestamp.valueOf(expiration));
            ps.setString(4,     offre.getMatricule());
            ps.setString(5,     mailAcheteur);
            ps.setBigDecimal(6, offre.getPrix());
            ps.setString(7,     modePaiement);
            ps.setString(8,     refPaiement);
            ps.setString(9,     "en_attente");
            ps.setString(10,    message);
            ps.setString(11,    cvPath);
            ps.setString(12,    lettreMotivation);
            ps.setBoolean(13,   true);
            ps.setInt(14,       idUtilisateur);
            ps.setInt(15,       idOffre);
            ps.executeUpdate();

            Candidature c = new Candidature();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) c.setIdCandidature(rs.getInt(1));
            c.setDateCandidature(now);
            c.setDateAbonnement(now);
            c.setDateExpiration(expiration);
            c.setMatriculeOffre(offre.getMatricule());
            c.setMailAcheteur(mailAcheteur);
            c.setPrixPaye(offre.getPrix());
            c.setModePaiement(modePaiement);
            c.setReferencePaiement(refPaiement);
            c.setStatut("en_attente");
            c.setIdUtilisateur(idUtilisateur);
            c.setIdOffre(idOffre);
            c.setTitreOffre(offre.getTitre());
            c.setActive(true);
            return c;
        }
    }

    public void changerStatutCandidature(int id, String statut) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE candidature SET statut=? WHERE id_candidature=?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void ajouterNoteRecruteur(int id, String note) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE candidature SET note_recruteur=? WHERE id_candidature=?")) {
            ps.setString(1, note);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Candidature> getCandidaturesParOffre(int idOffre) throws SQLException {
        String sql = """
            SELECT c.*, o.titre AS titre_offre,
                   u.nom AS nom_candidat, u.prenom AS prenom_candidat
            FROM candidature c
            JOIN offre o ON c.id_offre = o.id_offre
            JOIN utilisateur u ON c.id_utilisateur = u.id_utilisateur
            WHERE c.id_offre=?
            ORDER BY c.date_candidature DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOffre);
            return lireCandidatures(ps);
        }
    }

    public List<Candidature> getCandidaturesParUser(int idUser) throws SQLException {
        String sql = """
            SELECT c.*, o.titre AS titre_offre,
                   u.nom AS nom_candidat, u.prenom AS prenom_candidat
            FROM candidature c
            JOIN offre o ON c.id_offre = o.id_offre
            JOIN utilisateur u ON c.id_utilisateur = u.id_utilisateur
            WHERE c.id_utilisateur=?
            ORDER BY c.date_candidature DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            return lireCandidatures(ps);
        }
    }

    public boolean dejaCandidat(int idUser, int idOffre) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM candidature WHERE id_utilisateur=? AND id_offre=?")) {
            ps.setInt(1, idUser);
            ps.setInt(2, idOffre);
            return ps.executeQuery().next();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS PRIVÉS
    // ════════════════════════════════════════════════════════════

    private String genererMatricule(int id, LocalDate date) {
        int annee = (date != null ? date : LocalDate.now()).getYear();
        return String.format("OFF-%d-%05d", annee, id);
    }

    private void majMatricule(int id, String mat) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE offre SET matricule=? WHERE id_offre=?")) {
            ps.setString(1, mat);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private List<Offre> lire(String sql) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return mapper(rs);
        }
    }

    private List<Offre> lirePs(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return mapper(rs);
        }
    }

    /** Mapper aligné sur la table offre (avec nb_likes et nb_commentaires) */
    private List<Offre> mapper(ResultSet rs) throws SQLException {
        List<Offre> list = new ArrayList<>();
        while (rs.next()) {
            Offre o = new Offre();
            o.setIdOffre(rs.getInt("id_offre"));
            o.setTitre(rs.getString("titre"));
            o.setDescription(rs.getString("description"));
            o.setTypeOffre(rs.getString("type_offre"));
            o.setBudget(rs.getBigDecimal("budget"));
            o.setPrix(rs.getBigDecimal("prix"));
            o.setDuree(rs.getInt("duree"));
            o.setMatricule(rs.getString("matricule"));
            o.setLocalisation(rs.getString("localisation"));
            o.setVues(rs.getInt("vues"));
            o.setTags(rs.getString("tags"));

            // nb_likes et nb_commentaires (colonnes ajoutées par migration)
            try { o.setNbLikes(rs.getInt("nb_likes")); }         catch (SQLException ignored) {}
            try { o.setNbCommentaires(rs.getInt("nb_commentaires")); } catch (SQLException ignored) {}

            // dates
            Date dp = rs.getDate("date_publication");
            if (dp != null) o.setDatePublication(dp.toLocalDate());
            Date dl = rs.getDate("date_limite");
            if (dl != null) o.setDateLimite(dl.toLocalDate());

            o.setStatut(rs.getString("statut"));
            o.setIdUtilisateur(rs.getInt("id_utilisateur"));

            // mail_user — colonne optionnelle
            try { o.setMailUser(rs.getString("mail_user")); }
            catch (SQLException ignored) {}

            list.add(o);
        }
        return list;
    }

    private List<Candidature> lireCandidatures(PreparedStatement ps) throws SQLException {
        List<Candidature> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Candidature c = new Candidature();
                c.setIdCandidature(rs.getInt("id_candidature"));
                Timestamp tc = rs.getTimestamp("date_candidature");
                if (tc != null) c.setDateCandidature(tc.toLocalDateTime());
                Timestamp ta = rs.getTimestamp("date_abonnement");
                if (ta != null) c.setDateAbonnement(ta.toLocalDateTime());
                Timestamp te = rs.getTimestamp("date_expiration");
                if (te != null) c.setDateExpiration(te.toLocalDateTime());
                c.setMatriculeOffre(rs.getString("matricule_offre"));
                c.setMailAcheteur(rs.getString("mail_acheteur"));
                c.setPrixPaye(rs.getBigDecimal("prix_paye"));
                c.setModePaiement(rs.getString("mode_paiement"));
                c.setReferencePaiement(rs.getString("reference_paiement"));
                c.setStatut(rs.getString("statut"));
                c.setMessage(rs.getString("message"));
                c.setCv(rs.getString("cv"));
                c.setLettreMotivation(rs.getString("lettre_motivation"));
                c.setNoteRecruteur(rs.getString("note_recruteur"));
                c.setActive(rs.getBoolean("is_active"));
                c.setIdUtilisateur(rs.getInt("id_utilisateur"));
                c.setIdOffre(rs.getInt("id_offre"));
                c.setTitreOffre(rs.getString("titre_offre"));
                c.setNomCandidat(rs.getString("nom_candidat"));
                c.setPrenomCandidat(rs.getString("prenom_candidat"));
                list.add(c);
            }
        }
        return list;
    }

    private List<Commentaire> lireCommentaires(PreparedStatement ps) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("id_commentaire"));
                c.setIdOffre(rs.getInt("id_offre"));
                c.setIdUtilisateur(rs.getInt("id_utilisateur"));
                c.setContenu(rs.getString("contenu"));
                Timestamp ts = rs.getTimestamp("date_commentaire");
                if (ts != null) c.setDateCommentaire(ts.toLocalDateTime());
                c.setNomAuteur(rs.getString("nom_auteur"));
                c.setPrenomAuteur(rs.getString("prenom_auteur"));
                list.add(c);
            }
        }
        return list;
    }
}