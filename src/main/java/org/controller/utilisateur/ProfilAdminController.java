package org.controller.utilisateur;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controller.suivi_tache.back.GestionProjetController;
import org.model.RendezVous.postulation;
import org.model.RendezVous.rendez_vous;
import org.model.Utilisateurs.Connexion;
import org.model.Utilisateurs.Message;
import org.model.Utilisateurs.utilisateur;
import org.model.suiviTache.Projet;
import org.model.suiviTache.Tache;
import org.service.RendezVous.PostulationService;
import org.service.RendezVous.RendezVousService;
import org.service.suivi_tache.ProjetService;
import org.service.suivi_tache.TacheService;
import org.service.utilisateur.ConnexionService;
import org.service.utilisateur.MessageService;
import org.service.utilisateur.UtilisateurService;
import org.utils.Session;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfilAdminController {

    // -------- Topbar --------
    @FXML private TextField tfSearch;
    @FXML private Label lblBadgeInvitations;

    // -------- Profil --------
    @FXML private Label lblEmail, lblRole;
    @FXML private TextField tfNom, tfPrenom, tfTelephone;
    @FXML private Label lblMsg;
    @FXML private ImageView imgProfile;
    @FXML private Label lblChangePhoto;
    @FXML private StackPane avatarBox;
    @FXML private ImageView imgCover;

    // -------- Connexion --------
    @FXML private Label lblConnexion;

    // -------- Projets --------
    @FXML private VBox chefProjetBox;
    @FXML private TextField tfProjetTitre;
    @FXML private TextField tfProjetStatut;
    @FXML private TextField tfProjetDebut;
    @FXML private TextField tfProjetFin;
    @FXML private TextArea taProjetDescription;
    @FXML private Label lblProjetInfo;

    // -------- Contacts --------
    @FXML private VBox contactsListBox;

    // -------- Sidebar gauche (texte pur) --------
    @FXML private VBox  tachesListBox;
    @FXML private VBox  rdvListBox;
    @FXML private VBox  postulationsListBox;
    @FXML private Label lblNoTaches;
    @FXML private Label lblNoRdv;
    @FXML private Label lblNoPostulations;

    // -------- Services --------
    private final UtilisateurService service          = new UtilisateurService();
    private final ConnexionService   connexionService = new ConnexionService();
    private final MessageService     messageService   = new MessageService();
    private final ProjetService      projetService    = new ProjetService();
    private final TacheService       tacheService     = new TacheService();
    private final RendezVousService  rvService        = new RendezVousService();
    private final PostulationService postService      = new PostulationService();

    // Formatters
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT  =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Styles lignes texte sidebar
    private static final String STYLE_LIGNE =
            "-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-padding: 0 0 1 4;";
    private static final String STYLE_SEP   =
            "-fx-text-fill: #334155; -fx-font-size: 10px; -fx-padding: 4 0 4 0;";

    // -------- État --------
    private utilisateur currentUser;
    private utilisateur myProfileUser;

    private List<Projet> projetsChef;
    private int currentProjetIndex = 0;

    private long lastSearchTs = 0;
    private final ContextMenu     suggestionsMenu      = new ContextMenu();
    private final PauseTransition searchDelay          = new PauseTransition(Duration.millis(180));
    private static final int      SUGGEST_LIMIT        = 7;
    private boolean               suppressAutoComplete = false;
    private final java.util.LinkedHashMap<Integer, Stage>   chatStages  = new java.util.LinkedHashMap<>();
    private final java.util.LinkedHashMap<Integer, Boolean> chatReduit  = new java.util.LinkedHashMap<>();
    private static final int MAX_CHATS = 3;
    private static final double CHAT_WIDTH  = 340;
    private static final double CHAT_HEIGHT = 500;
    private static final double CHAT_HEADER_H = 62;
    private static final double CHAT_GAP    = 12;
    // Enregistrement vocal
    private TargetDataLine micLine      = null;
    private ByteArrayOutputStream audioBuffer = null;
    private Thread           recordThread = null;

    // =========================
    // INITIALIZE
    // =========================
    @FXML
    public void initialize() {
        setupAvatarHover();
        makeAvatarRound();
        suggestionsMenu.getStyleClass().add("search-menu");
        suggestionsMenu.setStyle("""
            -fx-background-color: #0f172a;
            -fx-background-radius: 8;
            -fx-padding: 6;
        """);
        if (lblMsg != null) lblMsg.setText("");
    }

    // =========================
    // SET USER
    // =========================
    public void setUser(utilisateur u) {
        this.currentUser = u;
        if (u == null) return;

        if (myProfileUser == null) myProfileUser = u;

        if (lblEmail    != null) lblEmail.setText(nvl(u.getEmail()));
        if (lblRole     != null) lblRole.setText(nvl(u.getRole()));
        if (tfNom       != null) tfNom.setText(nvl(u.getNom()));
        if (tfPrenom    != null) tfPrenom.setText(nvl(u.getPrenom()));
        if (tfTelephone != null) tfTelephone.setText(nvl(u.getTelephone()));

        loadProfileImageFromUser();
        loadCoverImageFromUser();
        loadProjetsChef();
        loadChefProjects();

        refreshConnexionLabel();
        refreshBadgeInvitations();
        loadContacts();

        // ---- Sidebar gauche : texte ----
        chargerTachesTexte(u.getId_utilisateur());
        chargerRdvTexte(u.getId_utilisateur());
        chargerPostulationsTexte(u.getId_utilisateur());

        if (lblMsg != null) lblMsg.setText("");
    }

    // =========================
// SIDEBAR GAUCHE — TÂCHES
// =========================
    private void chargerTachesTexte(int userId) {
        if (tachesListBox == null) return;
        tachesListBox.getChildren().removeIf(n ->
                n.getUserData() != null && "dynamic".equals(n.getUserData()));
        try {
            List<Projet> tousLesProjets = projetService.afficherTous();

            // ✅ Récupérer les tâches de MES projets + tâches où je suis assigné
            List<Tache> taches = tousLesProjets.stream()
                    .flatMap(p -> {
                        try {
                            return tacheService.afficherParProjet(p.getId()).stream()
                                    .filter(t ->
                                            // ✅ Tâche créée par moi (userId ou email)
                                            t.getUserId() == userId
                                                    || nvl(t.getMailUser()).equalsIgnoreCase(nvl(currentUser.getEmail()))
                                                    // ✅ OU le projet m'appartient
                                                    || p.getUserId() == userId
                                                    || nvl(p.getMailUser()).equalsIgnoreCase(nvl(currentUser.getEmail()))
                                    );
                        } catch (Exception ex) {
                            return java.util.stream.Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());

            System.out.println("🔍 chargerTachesTexte userId=" + userId
                    + " email=" + currentUser.getEmail()
                    + " → " + taches.size() + " tâches trouvées");

            boolean empty = taches.isEmpty();
            if (lblNoTaches != null) {
                lblNoTaches.setVisible(empty);
                lblNoTaches.setManaged(empty);
            }

            for (Tache t : taches) {
                String projetNom = tousLesProjets.stream()
                        .filter(p -> p.getId() == t.getProjetId())
                        .map(Projet::getTitre).findFirst().orElse("—");
                VBox bloc = new VBox(1);
                bloc.setUserData("dynamic");
                bloc.getChildren().addAll(
                        ligne("— Projet      : " + projetNom),
                        ligne("— Tâche       : " + nvl(t.getTitre())),
                        ligne("— Statut      : " + nvl(t.getStatut())),
                        ligne("— Priorité    : " + nvl(t.getPriorite())),
                        ligne("— Échéance    : " + (t.getEcheance() != null
                                ? t.getEcheance().format(D_FMT) : "—")),
                        separateur()
                );
                tachesListBox.getChildren().add(bloc);
            }
        } catch (Exception ex) {
            System.out.println("❌ EXCEPTION chargerTachesTexte : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    // =========================
    // SIDEBAR GAUCHE — RENDEZ-VOUS
    // =========================
    private void chargerRdvTexte(int userId) {
        if (rdvListBox == null) return;
        rdvListBox.getChildren().removeIf(n ->
                n.getUserData() != null && "dynamic".equals(n.getUserData()));
        try {
            List<rendez_vous> rdvList = rvService.afficherTous().stream()
                    .filter(r -> r.getId_admin_createur() == userId
                            || nvl(r.getMail_user()).equalsIgnoreCase(nvl(currentUser.getEmail())))
                    .collect(Collectors.toList());

            boolean empty = rdvList.isEmpty();
            if (lblNoRdv != null) { lblNoRdv.setVisible(empty); lblNoRdv.setManaged(empty); }

            for (rendez_vous r : rdvList) {
                VBox bloc = new VBox(1);
                bloc.setUserData("dynamic");
                bloc.getChildren().addAll(
                        ligne("— Titre       : " + nvl(r.getTitre())),
                        ligne("— Type        : " + nvl(r.getType())),
                        ligne("— Compétence  : " + nvl(r.getCompetence())),
                        ligne("— Date        : " + (r.getDate_rendez_vous() != null
                                ? r.getDate_rendez_vous().format(DT_FMT) : "—")),
                        ligne("— Places      : " + r.getNombre_places()),
                        separateur()
                );
                rdvListBox.getChildren().add(bloc);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // =========================
    // SIDEBAR GAUCHE — POSTULATIONS
    // =========================
    private void chargerPostulationsTexte(int userId) {
        if (postulationsListBox == null) return;
        postulationsListBox.getChildren().removeIf(n ->
                n.getUserData() != null && "dynamic".equals(n.getUserData()));
        try {
            List<postulation> posts = postService.afficherParUser(userId);

            boolean empty = posts.isEmpty();
            if (lblNoPostulations != null) {
                lblNoPostulations.setVisible(empty);
                lblNoPostulations.setManaged(empty);
            }

            for (postulation p : posts) {
                String statutLabel = switch (nvl(p.getStatus())) {
                    case "accepte"    -> "Accepté ✅";
                    case "refuse"     -> "Refusé ❌";
                    case "en_attente" -> "En attente ⏳";
                    default           -> nvl(p.getStatus());
                };
                VBox bloc = new VBox(1);
                bloc.setUserData("dynamic");
                bloc.getChildren().addAll(
                        ligne("— RDV         : " + nvl(p.getTitre())),
                        ligne("— Statut      : " + statutLabel),
                        ligne("— Date        : " + (p.getDate_postulation() != null
                                ? p.getDate_postulation().format(DT_FMT) : "—")),
                        ligne("— Message     : " + truncate(nvl(p.getMessage()), 35)),
                        separateur()
                );
                postulationsListBox.getChildren().add(bloc);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ---- helpers lignes texte ----
    private Label ligne(String texte) {
        Label l = new Label(texte);
        l.setStyle(STYLE_LIGNE);
        l.setWrapText(true);
        l.setMaxWidth(260);
        return l;
    }

    private Label separateur() {
        Label s = new Label("  ─────────────────");
        s.setStyle(STYLE_SEP);
        return s;
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // =========================
    // PHOTO PROFIL
    // =========================
    @FXML
    private void onChangePhoto(MouseEvent e) {
        try {
            if (currentUser == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une photo de profil");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
            );

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            File selected = fc.showOpenDialog(stage);
            if (selected == null) return;

            String ext = getFileExtension(selected.getName());
            String fileName = "u_" + currentUser.getId_utilisateur() + "_" + UUID.randomUUID() + ext;

            // ✅ dossier Symfony public
            Path destDir = Paths.get(
                    "C:\\Users\\hamza\\Bureau\\SkillSwap\\symfony\\public\\uploads\\utilisateur"
            );
            Files.createDirectories(destDir);

            Path dest = destDir.resolve(fileName);
            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // ✅ chemin à enregistrer en base pour Symfony
            String dbPath = "/uploads/utilisateur/" + fileName;

            service.updatePhoto(currentUser.getId_utilisateur(), dbPath);
            currentUser.setPhoto_profil(dbPath);

            // ✅ affichage immédiat côté JavaFX
            if (imgProfile != null) {
                imgProfile.setImage(new Image(dest.toUri().toString(), true));
                makeAvatarRound();
            }

            if (lblMsg != null) lblMsg.setText("Photo mise à jour ✅");

        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur photo : " + ex.getMessage());
        }
    }

    private void loadProfileImageFromUser() {
        if (imgProfile == null) return;

        setDefaultProfileImage();

        if (currentUser == null) {
            makeAvatarRound();
            return;
        }

        String path = currentUser.getPhoto_profil();

        if (path == null || path.isBlank() || "NULL".equalsIgnoreCase(path.trim())) {
            makeAvatarRound();
            return;
        }

        try {
            // 1) chemin absolu disque
            Path p = Paths.get(path);
            if (p.isAbsolute() && Files.exists(p)) {
                imgProfile.setImage(new Image(p.toUri().toString(), true));
                makeAvatarRound();
                return;
            }

            // 2) chemin web Symfony : /uploads/utilisateur/xxx.png
            if (path.startsWith("/uploads/")) {
                Path symfonyPublic = Paths.get(
                        "C:\\Users\\hamza\\Bureau\\SkillSwap\\symfony\\public" + path.replace("/", "\\")
                );
                if (Files.exists(symfonyPublic)) {
                    imgProfile.setImage(new Image(symfonyPublic.toUri().toString(), true));
                    makeAvatarRound();
                    return;
                }
            }

            // 3) ancienne ressource Java
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                imgProfile.setImage(new Image(stream));
                makeAvatarRound();
                return;
            }

            setDefaultProfileImage();
            makeAvatarRound();

        } catch (Exception e) {
            setDefaultProfileImage();
            makeAvatarRound();
        }
    }
    private void setDefaultProfileImage() {
        try {
            var defaultStream = getClass().getResourceAsStream("/view/image/utilisateur/avatar.png");
            if (defaultStream != null) {
                imgProfile.setImage(new Image(defaultStream));
            } else {
                imgProfile.setImage(null);
            }
        } catch (Exception e) {
            imgProfile.setImage(null);
        }
    }
    // =========================
    // PHOTO COUVERTURE
    // =========================
    @FXML
    private void onChangeCover(ActionEvent event) {
        try {
            if (currentUser == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une photo de couverture");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File selected = fc.showOpenDialog(stage);
            if (selected == null) return;

            String ext      = getFileExtension(selected.getName());
            String fileName = "cover_" + currentUser.getId_utilisateur() + "_" + UUID.randomUUID() + ext;

            Path destDir = Paths.get(System.getProperty("user.home"), "Documents", "SkillSwap", "uploads", "covers");
            Files.createDirectories(destDir);

            Path dest = destDir.resolve(fileName);
            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            String savedPath = dest.toAbsolutePath().toString();
            currentUser.setPhoto_couverture(savedPath);
            service.updateProfilPro(currentUser);

            if (imgCover != null) imgCover.setImage(new Image(dest.toUri().toString(), true));
            if (lblMsg != null) lblMsg.setText("Couverture mise à jour ✅");

        } catch (Exception e) {
            e.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur couverture : " + e.getMessage());
        }
    }

    private void loadCoverImageFromUser() {
        if (imgCover == null) return;

        // Reset couverture
        imgCover.setImage(null);

        if (currentUser == null) return;

        String path = currentUser.getPhoto_couverture();
        if (path == null || path.isBlank() || "NULL".equalsIgnoreCase(path.trim())) {
            return;
        }

        try {
            Path p = Paths.get(path);
            if (p.isAbsolute() && Files.exists(p)) {
                imgCover.setImage(new Image(p.toUri().toString(), true));
                return;
            }

            var stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                imgCover.setImage(new Image(stream));
            }
        } catch (Exception ignored) {
            imgCover.setImage(null);
        }
    }

    // =========================
    // AVATAR HOVER + ROUND
    // =========================
    private void setupAvatarHover() {
        if (avatarBox == null || lblChangePhoto == null) return;
        lblChangePhoto.setVisible(false);
        avatarBox.setOnMouseEntered(ev -> lblChangePhoto.setVisible(true));
        avatarBox.setOnMouseExited(ev -> lblChangePhoto.setVisible(false));
    }

    private void makeAvatarRound() {
        if (imgProfile == null) return;
        imgProfile.layoutBoundsProperty().addListener((obs, oldVal, bounds) -> {
            double radius = Math.min(bounds.getWidth(), bounds.getHeight()) / 2.0;
            imgProfile.setClip(new Circle(bounds.getWidth() / 2.0, bounds.getHeight() / 2.0, radius));
        });
    }

    private String getFileExtension(String name) {
        int idx = name.lastIndexOf('.');
        return (idx == -1) ? ".png" : name.substring(idx).toLowerCase();
    }

    // =========================
    // PROJETS
    // =========================
    private void loadChefProjects() {
        if (chefProjetBox == null) {
            System.out.println("⚠️ chefProjetBox est NULL → vérifier fx:id dans FXML");
            return;
        }
        if (currentUser == null) return;

        chefProjetBox.getChildren().clear();
        System.out.println("🔍 loadChefProjects() → userId=" + currentUser.getId_utilisateur());

        try {
            List<Projet> projets = projetService.afficherParUtilisateur(currentUser.getId_utilisateur());
            System.out.println("✅ projets trouvés = " + (projets == null ? "null" : projets.size()));

            if (projets == null || projets.isEmpty()) {
                Label none = new Label("Aucun projet créé.");
                none.getStyleClass().add("meta-value");
                chefProjetBox.getChildren().add(none);
                return;
            }

            for (Projet p : projets) {
                System.out.println("  → projet: id=" + p.getId() + " titre=" + p.getTitre()
                        + " userId=" + p.getUserId());
                VBox card = new VBox(8);
                card.getStyleClass().add("project-card");
                card.getChildren().addAll(
                        new Label("📌 " + nvl(p.getTitre())),
                        new Label("Description : " + nvl(p.getDescription())),
                        new Label("Début : "  + (p.getDateDebut() == null ? "-" : p.getDateDebut().toString())),
                        new Label("Fin : "    + (p.getDateFin()   == null ? "-" : p.getDateFin().toString())),
                        new Label("Statut : " + nvl(p.getStatut()))
                );
                chefProjetBox.getChildren().add(card);
            }
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION loadChefProjects : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
// PROJETS — loadProjetsChef (navigation prev/next)
// =========================
    private void loadProjetsChef() {
        if (currentUser == null) return;
        int userId = currentUser.getId_utilisateur();
        String email = nvl(currentUser.getEmail());

        System.out.println("🔍 loadProjetsChef() userId=" + userId);

        try {
            projetsChef = projetService.afficherParUtilisateur(userId);

            // ✅ Fallback email
            if ((projetsChef == null || projetsChef.isEmpty()) && !email.isBlank()) {
                projetsChef = projetService.afficherTous().stream()
                        .filter(p -> nvl(p.getMailUser()).equalsIgnoreCase(email))
                        .collect(Collectors.toList());
            }

            System.out.println("✅ projetsChef size=" + (projetsChef == null ? "null" : projetsChef.size()));

            if (projetsChef == null || projetsChef.isEmpty()) {
                clearProjetFields();
                if (lblProjetInfo != null) lblProjetInfo.setText("Aucun projet");
                return;
            }
            currentProjetIndex = 0;
            showProjet(currentProjetIndex);

        } catch (Exception e) {
            System.out.println("❌ EXCEPTION loadProjetsChef : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showProjet(int index) {
        if (projetsChef == null || projetsChef.isEmpty()) return;
        Projet p = projetsChef.get(index);
        if (tfProjetTitre       != null) tfProjetTitre.setText(nvl(p.getTitre()));
        if (tfProjetStatut      != null) tfProjetStatut.setText(nvl(p.getStatut()));
        if (tfProjetDebut       != null) tfProjetDebut.setText(p.getDateDebut() == null ? "" : p.getDateDebut().toString());
        if (tfProjetFin         != null) tfProjetFin.setText(p.getDateFin() == null ? "" : p.getDateFin().toString());
        if (taProjetDescription != null) taProjetDescription.setText(nvl(p.getDescription()));
        if (lblProjetInfo       != null) lblProjetInfo.setText("Projet " + (index + 1) + " / " + projetsChef.size());
    }

    @FXML private void nextProjet() {
        if (projetsChef == null || projetsChef.isEmpty()) return;
        currentProjetIndex = (currentProjetIndex + 1) % projetsChef.size();
        showProjet(currentProjetIndex);
    }

    @FXML private void prevProjet() {
        if (projetsChef == null || projetsChef.isEmpty()) return;
        currentProjetIndex = (currentProjetIndex - 1 + projetsChef.size()) % projetsChef.size();
        showProjet(currentProjetIndex);
    }

    private void clearProjetFields() {
        if (tfProjetTitre       != null) tfProjetTitre.clear();
        if (tfProjetStatut      != null) tfProjetStatut.clear();
        if (tfProjetDebut       != null) tfProjetDebut.clear();
        if (tfProjetFin         != null) tfProjetFin.clear();
        if (taProjetDescription != null) taProjetDescription.clear();
    }

    // =========================
    // CRUD
    // =========================
    @FXML
    private void modifier(ActionEvent e) {
        try {
            if (currentUser == null) return;
            currentUser.setNom(tfNom.getText().trim());
            currentUser.setPrenom(tfPrenom.getText().trim());
            currentUser.setTelephone(tfTelephone.getText().trim());
            service.update(currentUser);
            if (lblMsg != null) lblMsg.setText("Profil mis à jour ✅");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void supprimerCompte(ActionEvent e) {
        try {
            if (currentUser == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer le compte ?");
            confirm.setContentText("Cette action est irréversible.");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) return;
            service.delete(currentUser.getId_utilisateur());
            currentUser = null;
            if (lblMsg != null) lblMsg.setText("Compte supprimé ✅");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur : " + ex.getMessage());
        }
    }

    // =========================
    // CONNEXION
    // =========================
    private void refreshConnexionLabel() {
        if (lblConnexion == null) return;
        if (currentUser == null || myProfileUser == null
                || currentUser.getId_utilisateur() == myProfileUser.getId_utilisateur()) {
            lblConnexion.setVisible(false);
            return;
        }
        lblConnexion.setVisible(true);
        try {
            String statut = connexionService.getStatut(
                    myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
            if (statut == null) {
                lblConnexion.setText("➕ Se connecter");
            } else if ("en_attente".equals(statut)) {
                boolean estMoi = connexionService.estDemandeur(
                        myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
                lblConnexion.setText(estMoi ? "⏳ Invitation envoyée" : "✅ Accepter l'invitation");
            } else if ("accepte".equals(statut)) {
                lblConnexion.setText("✔ Connecté · Retirer");
            } else {
                lblConnexion.setText("➕ Se connecter");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblConnexion.setText("➕ Se connecter");
        }
    }

    @FXML
    private void onConnexionClick(MouseEvent e) {
        if (currentUser == null || myProfileUser == null) return;
        if (currentUser.getId_utilisateur() == myProfileUser.getId_utilisateur()) return;
        try {
            String statut = connexionService.getStatut(
                    myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
            if (statut == null) {
                connexionService.envoyerInvitation(
                        myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
                if (lblMsg != null) lblMsg.setText("Invitation envoyée à " + currentUser.getNom() + " ✅");
            } else if ("en_attente".equals(statut)) {
                boolean estMoi = connexionService.estDemandeur(
                        myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
                if (estMoi) {
                    connexionService.supprimerConnexion(
                            myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
                    if (lblMsg != null) lblMsg.setText("Invitation annulée.");
                } else {
                    connexionService.accepterInvitation(
                            currentUser.getId_utilisateur(), myProfileUser.getId_utilisateur());
                    if (lblMsg != null) lblMsg.setText("Connexion acceptée ✅");
                }
            } else if ("accepte".equals(statut)) {
                connexionService.supprimerConnexion(
                        myProfileUser.getId_utilisateur(), currentUser.getId_utilisateur());
                if (lblMsg != null) lblMsg.setText("Connexion retirée.");
            }
            refreshConnexionLabel();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur : " + ex.getMessage());
        }
    }

    // =========================
    // BADGE INVITATIONS
    // =========================
    private void refreshBadgeInvitations() {
        if (lblBadgeInvitations == null || myProfileUser == null) return;
        try {
            int count = connexionService.getInvitationsRecues(myProfileUser.getId_utilisateur()).size();
            if (count > 0) {
                lblBadgeInvitations.setText(String.valueOf(count));
                lblBadgeInvitations.setVisible(true);
            } else {
                lblBadgeInvitations.setVisible(false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onInvitationsClick(ActionEvent event) {
        if (myProfileUser == null) return;
        try {
            List<Connexion> invitations =
                    connexionService.getInvitationsRecues(myProfileUser.getId_utilisateur());

            Stage popup = new Stage();
            popup.setTitle("Invitations reçues");
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());

            VBox root = new VBox(10);
            root.setStyle("-fx-background-color: #0f172a; -fx-padding: 20;");

            if (invitations.isEmpty()) {
                Label none = new Label("Aucune invitation en attente.");
                none.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
                root.getChildren().add(none);
            } else {
                for (Connexion inv : invitations) {
                    utilisateur demandeur = service.findById(inv.getIdDemandeur());
                    if (demandeur == null) continue;

                    ImageView avatar = new ImageView();
                    avatar.setFitWidth(42); avatar.setFitHeight(42);
                    avatar.setPreserveRatio(true); avatar.setSmooth(true);
                    avatar.setClip(new Circle(21, 21, 21));
                    chargerPhoto(demandeur.getPhoto_profil(), avatar);

                    Label nom = new Label(nvl(demandeur.getNom()) + " " + nvl(demandeur.getPrenom()));
                    nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                    Label email = new Label(nvl(demandeur.getEmail()));
                    email.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                    VBox info = new VBox(3, nom, email);
                    info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Button btnAccepter = new Button("✅ Accepter");
                    btnAccepter.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
                    Button btnRefuser = new Button("❌ Refuser");
                    btnRefuser.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");

                    HBox boutons = new HBox(8, btnAccepter, btnRefuser);
                    boutons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    HBox ligne = new HBox(12, avatar, info, new Region(), boutons);
                    HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
                    ligne.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    ligne.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-padding: 10 14;");
                    root.getChildren().add(ligne);

                    final int idDemandeur = demandeur.getId_utilisateur();
                    final int idMoi       = myProfileUser.getId_utilisateur();

                    btnAccepter.setOnAction(ev -> {
                        try {
                            connexionService.accepterInvitation(idDemandeur, idMoi);
                            root.getChildren().remove(ligne);
                            refreshBadgeInvitations();
                            refreshConnexionLabel();
                            loadContacts();
                            if (lblMsg != null) lblMsg.setText("Connexion acceptée ✅");
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                    btnRefuser.setOnAction(ev -> {
                        try {
                            connexionService.supprimerConnexion(idDemandeur, idMoi);
                            root.getChildren().remove(ligne);
                            refreshBadgeInvitations();
                            if (lblMsg != null) lblMsg.setText("Invitation refusée.");
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                }
            }

            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a;");
            popup.setScene(new Scene(scroll, 480, 400));
            popup.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur invitations : " + ex.getMessage());
        }
    }

    // =========================
    // CONTACTS SIDEBAR
    // =========================
    private void loadContacts() {
        if (contactsListBox == null) {
            System.out.println("❌ contactsListBox NULL");
            return;
        }
        if (myProfileUser == null) {
            System.out.println("❌ myProfileUser NULL");
            return;
        }

        System.out.println("✅ loadContacts() admin → userId=" + myProfileUser.getId_utilisateur());
        contactsListBox.getChildren().clear();

        try {
            List<Connexion> connexions =
                    connexionService.getConnexions(myProfileUser.getId_utilisateur());

            System.out.println("✅ connexions trouvées = " + connexions.size());

            if (connexions.isEmpty()) {
                Label none = new Label("Aucun contact pour l'instant.");
                none.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-padding: 10 14;");
                contactsListBox.getChildren().add(none);
                return;
            }

            for (Connexion c : connexions) {
                System.out.println("  → id=" + c.getId()
                        + " demandeur=" + c.getIdDemandeur()
                        + " receveur=" + c.getIdReceveur()
                        + " statut=" + c.getStatut());

                int idAmi = (c.getIdDemandeur() == myProfileUser.getId_utilisateur())
                        ? c.getIdReceveur() : c.getIdDemandeur();

                utilisateur ami = service.findById(idAmi);
                if (ami == null) {
                    System.out.println("  ❌ findById(" + idAmi + ") = NULL");
                    continue;
                }
                System.out.println("  ✅ ami = " + ami.getNom() + " " + ami.getPrenom());

                ImageView avatar = new ImageView();
                avatar.setFitWidth(36); avatar.setFitHeight(36);
                avatar.setPreserveRatio(true); avatar.setSmooth(true);
                avatar.setClip(new Circle(18, 18, 18));
                chargerPhoto(ami.getPhoto_profil(), avatar);

                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5);
                dot.setFill(javafx.scene.paint.Color.web("#22c55e"));
                dot.setStroke(javafx.scene.paint.Color.web("#111827"));
                dot.setStrokeWidth(2);
                StackPane avatarStack = new StackPane(avatar, dot);
                StackPane.setAlignment(dot, javafx.geometry.Pos.BOTTOM_RIGHT);
                avatarStack.setPrefSize(36, 36);

                Label nomLabel = new Label(nvl(ami.getNom()) + " " + nvl(ami.getPrenom()));
                nomLabel.getStyleClass().add("contact-name");

                int nonLus = 0;
                try { nonLus = messageService.getNonLus(idAmi, myProfileUser.getId_utilisateur()); }
                catch (Exception ex) { System.out.println("  ⚠️ getNonLus erreur: " + ex.getMessage()); }

                Label badgeMsg = new Label(nonLus > 0 ? String.valueOf(nonLus) : "");
                badgeMsg.setVisible(nonLus > 0);
                badgeMsg.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 1 5;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                HBox row = new HBox(10, avatarStack, nomLabel, spacer, badgeMsg);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.getStyleClass().add("contact-row");

                final utilisateur finalAmi = ami;
                row.setOnMouseClicked(ev -> ouvrirChat(finalAmi, badgeMsg));
                contactsListBox.getChildren().add(row);
                System.out.println("  ✅ ajouté dans sidebar : " + ami.getNom());
            }

        } catch (Exception e) {
            System.out.println("❌ EXCEPTION loadContacts : " + e.getMessage());
            e.printStackTrace();
        }
    }


// ================================================================
//  ouvrirChat() — VERSION FINALE COMPLÈTE
//  Emoji picker style Facebook + upload image/vidéo + vocal
// ================================================================


        private void ouvrirChat(utilisateur ami, Label badgeMsg) {
            if (myProfileUser == null) return;
            int idAmi = ami.getId_utilisateur();

            // ── Déjà ouvert ? ──
            if (chatStages.containsKey(idAmi)) {
                Stage ex = chatStages.get(idAmi);
                if (Boolean.TRUE.equals(chatReduit.get(idAmi))) agrandirChat(idAmi, ex);
                else ex.toFront();
                return;
            }

            // ── Limite 3 chats : fermer le plus ancien ──
            if (chatStages.size() >= MAX_CHATS) {
                int oldest = chatStages.keySet().iterator().next();
                Stage old  = chatStages.remove(oldest);
                chatReduit.remove(oldest);
                if (old != null) old.close();
            }

            Stage chatStage = new Stage();
            chatStage.initStyle(StageStyle.UNDECORATED);
            chatStages.put(idAmi, chatStage);
            chatReduit.put(idAmi, false);

            // ══════════════════════════════════════════
            //  HEADER
            // ══════════════════════════════════════════
            ImageView avatarHeader = new ImageView();
            avatarHeader.setFitWidth(36); avatarHeader.setFitHeight(36);
            avatarHeader.setPreserveRatio(true);
            avatarHeader.setClip(new Circle(18, 18, 18));
            chargerPhoto(ami.getPhoto_profil(), avatarHeader);

            javafx.scene.shape.Circle statusDot = new javafx.scene.shape.Circle(5);
            statusDot.setFill(javafx.scene.paint.Color.web("#31a24c"));
            statusDot.setStroke(javafx.scene.paint.Color.web("#1a1a2e"));
            statusDot.setStrokeWidth(2);
            StackPane avatarStack = new StackPane(avatarHeader, statusDot);
            StackPane.setAlignment(statusDot, javafx.geometry.Pos.BOTTOM_RIGHT);

            Label headerName = new Label(nvl(ami.getNom()) + " " + nvl(ami.getPrenom()));
            headerName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label headerSub = new Label("En ligne");
            headerSub.setStyle("-fx-text-fill: #31a24c; -fx-font-size: 11px;");
            VBox headerInfo = new VBox(1, headerName, headerSub);
            headerInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label btnPhone = makeIconLabel("📞");
            Label btnVideoH = makeIconLabel("📹");
            Button btnMin   = new Button("—");
            styleSmallBtn(btnMin, "14px", "0 0 4 0");
            Button btnClose = new Button("✕");
            styleSmallBtn(btnClose, "12px", null);

            btnClose.setOnAction(e -> {
                chatStages.remove(idAmi);
                chatReduit.remove(idAmi);
                chatStage.close();
                repositionnerTous();
            });

            HBox rightBtns = new HBox(5, btnPhone, btnVideoH, btnMin, btnClose);
            rightBtns.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Region hSpacer = new Region();
            HBox.setHgrow(hSpacer, javafx.scene.layout.Priority.ALWAYS);

            HBox header = new HBox(10, avatarStack, headerInfo, hSpacer, rightBtns);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setPadding(new Insets(0, 10, 0, 10));
            header.setPrefHeight(CHAT_HEADER_H);
            header.setStyle(
                    "-fx-background-color: #1a1a2e;" +
                            "-fx-border-color: #2d2d44; -fx-border-width: 0 0 1 0;"
            );

            // ══════════════════════════════════════════
            //  ZONE MESSAGES
            // ══════════════════════════════════════════
            VBox messagesBox = new VBox(4);
            messagesBox.setPadding(new Insets(10));

            ScrollPane scrollMessages = new ScrollPane(messagesBox);
            scrollMessages.setFitToWidth(true);
            scrollMessages.setStyle("-fx-background-color: #1a1a2e; -fx-background: #1a1a2e;");
            scrollMessages.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollMessages.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            VBox.setVgrow(scrollMessages, javafx.scene.layout.Priority.ALWAYS);

            // ── Charger historique ──
            try {
                messageService.marquerLus(idAmi, myProfileUser.getId_utilisateur());
                if (badgeMsg != null) badgeMsg.setVisible(false);

                List<org.model.Utilisateurs.Message> conv =
                        messageService.getConversation(myProfileUser.getId_utilisateur(), idAmi);

                for (int i = 0; i < conv.size(); i++) {
                    org.model.Utilisateurs.Message msg = conv.get(i);
                    boolean isLast = (i == conv.size() - 1)
                            || conv.get(i + 1).getIdEnvoyeur() != msg.getIdEnvoyeur();
                    boolean showAv = isLast && msg.getIdEnvoyeur() != myProfileUser.getId_utilisateur();
                    ajouterBulleMessenger(messagesBox, msg, myProfileUser.getId_utilisateur(), ami, showAv);
                }
                javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));
            } catch (Exception e) { e.printStackTrace(); }

            // ══════════════════════════════════════════
            //  EMOJI PICKER — STYLE FACEBOOK
            // ══════════════════════════════════════════
            String[][] CATEGORIES = {
                    {"😀", "Smileys & personnes",
                            "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍","🤩",
                            "😘","😗","😚","😙","🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🤫","🤔","🤐",
                            "🤨","😐","😑","😶","😏","😒","🙄","😬","🤥","😌","😔","😪","🤤","😴","😷","🤒",
                            "🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸","😎","🤓","🧐","😕",
                            "😟","🙁","😮","😯","😲","😳","🥺","😦","😧","😨","😰","😥","😢","😭","😱","😖",
                            "😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩","🤡",
                            "👹","👺","👻","👽","👾","🤖"},
                    {"👋", "Gestes & corps",
                            "👋","🤚","🖐️","✋","🖖","👌","🤌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉","👆",
                            "🖕","👇","☝️","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝","🙏","✍️",
                            "💅","🤳","💪","🦵","🦶","👂","🦻","👃","🧠","👀","👁️","👅","👄","💋"},
                    {"🌹", "Animaux & nature",
                            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐸","🐵",
                            "🙈","🙉","🙊","🐔","🐧","🐦","🐤","🦆","🦅","🦉","🦇","🐺","🐴","🦄","🐝",
                            "🦋","🐌","🐞","🐜","🐢","🐍","🦎","🐙","🦑","🐡","🐠","🐟","🐬","🐳","🐋",
                            "🦈","🐊","🐅","🐆","🦓","🦍","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🐃","🐂",
                            "🐄","🐎","🐖","🐏","🐑","🐐","🦌","🐕","🐩","🐈","🐓","🦃","🦚","🦜","🦢",
                            "🦩","🕊️","🐇","🦝","🦨","🦡","🦦","🦥","🐁","🐀","🐿️","🦔","🌵","🎄","🌲",
                            "🌳","🌴","🌱","🌿","☘️","🍀","🍃","🍂","🍁","🍄","🌾","💐","🌷","🌹","🥀",
                            "🌺","🌸","🌼","🌻"},
                    {"🍔", "Nourriture & boissons",
                            "🍕","🍔","🍟","🌭","🍿","🧂","🥓","🥚","🍳","🧇","🥞","🧈","🍞","🥐","🥖",
                            "🥨","🥯","🧀","🥗","🥙","🥪","🌮","🌯","🍝","🍜","🍲","🍛","🍣","🍱","🥟",
                            "🍤","🍙","🍚","🍘","🍥","🥮","🧆","🍡","🧁","🍰","🎂","🍮","🍭","🍬","🍫",
                            "🍩","🍪","🌰","🥜","🍯","🧃","🥤","🧋","☕","🍵","🫖","🍺","🍻","🥂","🍷",
                            "🥃","🍸","🍹","🧉","🍾","🧊"},
                    {"⚽", "Activités",
                            "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🏓","🏸","🏒","🏑","🥍",
                            "🏏","🥅","⛳","🥊","🥋","🎽","🛹","🛼","🛷","⛸️","🥌","🎿","🏋️","🤼","🤸",
                            "🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗","🚵","🚴","🏆","🥇","🥈",
                            "🥉","🏅","🎖️","🎭","🎨","🎬","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🎸",
                            "🎻","🎲","♟️","🎯","🎳","🎮","🎰","🧩"},
                    {"✈️", "Voyage & lieux",
                            "🚗","🚕","🚙","🚌","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🏍️","🛵",
                            "🚲","🛴","🚏","⛽","🚧","⚓","⛵","🛶","🚤","🛳️","🚢","✈️","🛩️","🛫","🛬",
                            "💺","🚁","🚀","🛸","🪐","🌍","🌎","🌏","🗺️","🧭","🏔️","⛰️","🌋","🗻","🏕️",
                            "🏖️","🏜️","🏝️","🏞️","🏟️","🏛️","🏗️","🛖","🏘️","🏠","🏡","🏢","🏥","🏦",
                            "🏨","🏪","🏫","🏬","🏭","🏯","🏰","💒","🗼","🗽","⛪","🕌","🕍","⛩️","🕋",
                            "⛲","⛺","🌁","🌃","🏙️","🌄","🌅","🌆","🌇","🌉","🎠","🎡","🎢","🎪"},
                    {"💡", "Objets",
                            "⌚","📱","💻","⌨️","🖥️","🖨️","🖱️","🕹️","💾","💿","📷","📸","📹","🎥","📞",
                            "☎️","📺","📻","🧭","⏱️","⏲️","⏰","🕰️","⌛","⏳","📡","🔋","🔌","💡","🔦",
                            "🕯️","💰","💴","💵","💶","💷","💸","💳","🪙","📈","📉","📊","📋","📌","📍",
                            "📎","📏","📐","✂️","🗃️","🗄️","🗑️","🔒","🔓","🔑","🗝️","🔨","⚒️","🛠️","🔧",
                            "🔩","⚙️","⚖️","🔗","🧲","🪜","🧰","💊","💉","🩸","🩹","🩺","🚪","🛏️","🛋️",
                            "🚽","🚿","🛁","🧴","🧷","🧹","🧺","🧻","🧼","🛒","⚰️","🪦","🗿"},
                    {"❤️", "Symboles",
                            "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗",
                            "💖","💝","💟","☮️","✝️","☪️","🕉️","☸️","✡️","🔯","☯️","☦️","🛐","⛎","♈",
                            "♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛️","☢️","☣️","📴",
                            "📳","🈶","🈚","✴️","🆚","💮","🉐","㊙️","㊗️","🈴","🈵","🈹","🈲","🅰️","🅱️",
                            "🆎","🆑","🅾️","🆘","❌","⭕","🛑","⛔","📛","🚫","💯","💢","♨️","🔞","💤",
                            "🔅","🔆","📶","ℹ️","🔤","🔡","🔠","🆖","🆗","🆙","🆒","🆕","🆓","🔢","▶️",
                            "⏸️","⏹️","⏭️","⏮️","⏩","⏪","🔀","🔁","🔂","🔼","🔽","◀️","🔊","🔉","🔈",
                            "🔇","📣","📢","🔔","🔕","♻️","✅","❎","💠","🔷","🔶","🔹","🔸","▪️","▫️",
                            "◾","◽","◼️","◻️","⬛","⬜","🟥","🟧","🟨","🟩","🟦","🟪","🟫"},
            };

            final int[] activeCat = {0};

            // Grille fluide
            javafx.scene.layout.FlowPane emojiGrid = new javafx.scene.layout.FlowPane();
            emojiGrid.setHgap(2); emojiGrid.setVgap(2);
            emojiGrid.setPadding(new Insets(6));
            emojiGrid.setPrefWrapLength(302);
            emojiGrid.setStyle("-fx-background-color: transparent;");

            // Titre catégorie
            Label catTitle = new Label(CATEGORIES[0][1]);
            catTitle.setStyle(
                    "-fx-text-fill: #a89ec8; -fx-font-size: 11px;" +
                            "-fx-font-weight: bold; -fx-padding: 2 6 2 6;"
            );

            TextField[] tfRef = new TextField[1];

            // Remplissage de la grille
            java.util.function.BiConsumer<Integer, String> remplir = (catIdx, filtre) -> {
                emojiGrid.getChildren().clear();
                String[] cat = CATEGORIES[catIdx];
                catTitle.setText(cat[1]);
                String q = (filtre == null) ? "" : filtre.trim();
                for (int ei = 2; ei < cat.length; ei++) {
                    String em = cat[ei];
                    if (!q.isEmpty() && !em.contains(q)) continue;
                    Label eL = new Label(em);
                    eL.setStyle(
                            "-fx-font-size: 24px; -fx-cursor: hand;" +
                                    "-fx-padding: 3; -fx-background-radius: 6;"
                    );
                    eL.setOnMouseEntered(ev -> eL.setStyle(
                            "-fx-font-size: 24px; -fx-cursor: hand; -fx-padding: 3;" +
                                    "-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 6;"
                    ));
                    eL.setOnMouseExited(ev -> eL.setStyle(
                            "-fx-font-size: 24px; -fx-cursor: hand; -fx-padding: 3; -fx-background-radius: 6;"
                    ));
                    eL.setOnMouseClicked(ev -> {
                        if (tfRef[0] != null) {
                            int caret = tfRef[0].getCaretPosition();
                            String cur = tfRef[0].getText();
                            tfRef[0].setText(cur.substring(0, caret) + em + cur.substring(caret));
                            tfRef[0].positionCaret(caret + em.length());
                            tfRef[0].requestFocus();
                        }
                    });
                    emojiGrid.getChildren().add(eL);
                }
            };
            remplir.accept(0, null);

            // Barre de recherche style Facebook
            Label searchIco = new Label("🔍");
            searchIco.setStyle("-fx-font-size: 13px; -fx-padding: 0 4 0 2;");

            TextField tfEmojiSearch = new TextField();
            tfEmojiSearch.setPromptText("Rechercher un emoji");
            tfEmojiSearch.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #e8e0ff;" +
                            "-fx-prompt-text-fill: #9890b8; -fx-font-size: 13px;" +
                            "-fx-border-width: 0; -fx-padding: 0;"
            );
            HBox.setHgrow(tfEmojiSearch, javafx.scene.layout.Priority.ALWAYS);
            tfEmojiSearch.textProperty().addListener((obs, old, nw) ->
                    remplir.accept(activeCat[0], nw)
            );

            HBox searchBar = new HBox(4, searchIco, tfEmojiSearch);
            searchBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            searchBar.setStyle(
                    "-fx-background-color: #1e1a40; -fx-background-radius: 20;" +
                            "-fx-padding: 7 10 7 8;"
            );
            searchBar.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(searchBar, new Insets(0, 6, 2, 6));

            // ScrollPane grille
            ScrollPane emojiScroll = new ScrollPane(emojiGrid);
            emojiScroll.setFitToWidth(true);
            emojiScroll.setPrefHeight(200);
            emojiScroll.setMinHeight(200);
            emojiScroll.setMaxHeight(200);
            emojiScroll.setStyle(
                    "-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;"
            );
            emojiScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            emojiScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Barre de catégories (bas du picker)
            HBox catBar = new HBox(0);
            catBar.setAlignment(javafx.geometry.Pos.CENTER);
            catBar.setStyle(
                    "-fx-background-color: #211e45;" +
                            "-fx-border-color: #3d3870; -fx-border-width: 1 0 0 0;" +
                            "-fx-padding: 4;"
            );

            Label[] catBtns = new Label[CATEGORIES.length];
            String activeStyle =
                    "-fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5 7; -fx-background-radius: 6;" +
                            "-fx-background-color: rgba(255,255,255,0.13);" +
                            "-fx-border-color: #a78bfa; -fx-border-width: 0 0 2 0; -fx-border-radius: 0;";
            String normalStyle =
                    "-fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5 7; -fx-background-radius: 6;";
            String hoverStyle  =
                    "-fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5 7; -fx-background-radius: 6;" +
                            "-fx-background-color: rgba(255,255,255,0.08);";

            for (int ci = 0; ci < CATEGORIES.length; ci++) {
                final int idx = ci;
                Label btn = new Label(CATEGORIES[ci][0]);
                btn.setStyle(ci == 0 ? activeStyle : normalStyle);
                btn.setOnMouseEntered(ev -> { if (activeCat[0] != idx) btn.setStyle(hoverStyle); });
                btn.setOnMouseExited(ev  -> { if (activeCat[0] != idx) btn.setStyle(normalStyle); });
                btn.setOnMouseClicked(ev -> {
                    catBtns[activeCat[0]].setStyle(normalStyle);
                    activeCat[0] = idx;
                    btn.setStyle(activeStyle);
                    tfEmojiSearch.clear();
                    remplir.accept(idx, null);
                    emojiScroll.setVvalue(0);
                });
                catBtns[ci] = btn;
                catBar.getChildren().add(btn);
            }

            // Panel complet
            VBox emojiPanel = new VBox(4, searchBar, catTitle, emojiScroll, catBar);
            emojiPanel.setStyle(
                    "-fx-background-color: #2e2b5f;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #5a4fcf; -fx-border-width: 1.5; -fx-border-radius: 14;" +
                            "-fx-padding: 8 0 0 0;" +
                            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.75),18,0.45,0,6);"
            );
            emojiPanel.setMaxWidth(CHAT_WIDTH - 10);
            emojiPanel.setVisible(false);
            emojiPanel.setManaged(false);

            // ══════════════════════════════════════════
            //  CHAMP DE SAISIE + BOUTON ENVOI
            // ══════════════════════════════════════════
            TextField tfMessage = new TextField();
            tfRef[0] = tfMessage;
            tfMessage.setPromptText("Aa");
            tfMessage.setStyle(
                    "-fx-background-color: #2d2d44; -fx-text-fill: white;" +
                            "-fx-prompt-text-fill: #8080a0; -fx-background-radius: 20;" +
                            "-fx-padding: 8 14; -fx-font-size: 13px; -fx-border-width: 0;"
            );
            HBox.setHgrow(tfMessage, javafx.scene.layout.Priority.ALWAYS);

            Button btnSend = new Button("👍");
            btnSend.setStyle(
                    "-fx-background-color: transparent; -fx-font-size: 20px;" +
                            "-fx-cursor: hand; -fx-padding: 0 4;"
            );

            tfMessage.textProperty().addListener((obs, old, nw) -> {
                if (nw == null || nw.isBlank()) {
                    btnSend.setText("👍");
                    btnSend.setStyle(
                            "-fx-background-color: transparent; -fx-font-size: 20px;" +
                                    "-fx-cursor: hand; -fx-padding: 0 4;"
                    );
                } else {
                    btnSend.setText("➤");
                    btnSend.setStyle(
                            "-fx-background-color: #7b5ea7; -fx-text-fill: white;" +
                                    "-fx-font-size: 13px; -fx-font-weight: bold;" +
                                    "-fx-background-radius: 50%;" +
                                    "-fx-min-width: 32; -fx-min-height: 32;" +
                                    "-fx-max-width: 32; -fx-max-height: 32;" +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            Runnable envoyer = () -> {
                String texte = tfMessage.getText() == null ? "" : tfMessage.getText().trim();
                if (texte.isBlank()) {
                    try {
                        messageService.envoyer(myProfileUser.getId_utilisateur(), idAmi, "👍");
                        org.model.Utilisateurs.Message m = new org.model.Utilisateurs.Message();
                        m.setIdEnvoyeur(myProfileUser.getId_utilisateur());
                        m.setIdReceveur(idAmi);
                        m.setContenu("👍");
                        ajouterBulleMessenger(messagesBox, m, myProfileUser.getId_utilisateur(), ami, false);
                        javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));
                    } catch (Exception ex) { ex.printStackTrace(); }
                    return;
                }
                try {
                    messageService.envoyer(myProfileUser.getId_utilisateur(), idAmi, texte);
                    org.model.Utilisateurs.Message m = new org.model.Utilisateurs.Message();
                    m.setIdEnvoyeur(myProfileUser.getId_utilisateur());
                    m.setIdReceveur(idAmi);
                    m.setContenu(texte);
                    ajouterBulleMessenger(messagesBox, m, myProfileUser.getId_utilisateur(), ami, false);
                    tfMessage.clear();
                    javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));
                } catch (Exception ex) { ex.printStackTrace(); }
            };
            btnSend.setOnAction(e -> envoyer.run());
            tfMessage.setOnAction(e -> envoyer.run());

            // ══════════════════════════════════════════
            //  TOOLBAR : 📷 🎬 😊 🎤
            // ══════════════════════════════════════════
            // 📎 Upload fichier (PDF, Word, Excel, PPTX...)
            Label btnFile = makeToolLabel("📎");
            btnFile.setOnMouseClicked(e -> {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Choisir un fichier");
                fc.getExtensionFilters().addAll(
                        new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers supportés",
                                "*.pdf","*.docx","*.doc","*.xlsx","*.xls","*.pptx","*.ppt",
                                "*.txt","*.csv","*.zip","*.rar"),
                        new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"),
                        new javafx.stage.FileChooser.ExtensionFilter("Word", "*.docx","*.doc"),
                        new javafx.stage.FileChooser.ExtensionFilter("Excel", "*.xlsx","*.xls"),
                        new javafx.stage.FileChooser.ExtensionFilter("PowerPoint", "*.pptx","*.ppt"),
                        new javafx.stage.FileChooser.ExtensionFilter("Autres", "*.txt","*.csv","*.zip","*.rar")
                );
                java.io.File f = fc.showOpenDialog(chatStage);
                if (f == null) return;

                // ✅ Affichage immédiat de la carte fichier
                afficherCarteFichier(messagesBox, f.getName(), f.length(), f.getAbsolutePath(), true);
                javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));

                // ✅ Sauvegarde en BD en arrière-plan
                new Thread(() -> {
                    try {
                        messageService.envoyerFichier(myProfileUser.getId_utilisateur(), idAmi, f);
                    } catch (Exception saveEx) {
                        System.out.println("⚠️ Erreur sauvegarde fichier : " + saveEx.getMessage());
                    }
                }).start();
            });

            // 📷 Upload image
            Label btnImg = makeToolLabel("📷");
            btnImg.setOnMouseClicked(e -> {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Choisir une image");
                fc.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter(
                                "Images", "*.png","*.jpg","*.jpeg","*.gif","*.webp"));
                java.io.File f = fc.showOpenDialog(chatStage);
                if (f == null) return;

                // ✅ Afficher IMMÉDIATEMENT avec le fichier local (pas besoin du service pour display)
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                            f.toURI().toString(), 200, 0, false, true); // ← false = pas background loading

                    if (img.isError()) {
                        System.out.println("❌ Erreur chargement image : " + img.getException());
                        return;
                    }

                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                    iv.setFitWidth(200);
                    iv.setPreserveRatio(false);  // ← false pour clip correct
                    iv.setFitHeight(img.getHeight() * 200 / img.getWidth()); // ratio manuel

                    javafx.scene.shape.Rectangle clipR = new javafx.scene.shape.Rectangle(
                            200, img.getHeight() * 200 / img.getWidth());
                    clipR.setArcWidth(14);
                    clipR.setArcHeight(14);
                    iv.setClip(clipR);

                    HBox row = new HBox(iv);
                    row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    row.setPadding(new Insets(2, 8, 2, 50));
                    messagesBox.getChildren().add(row);
                    javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));

                } catch (Exception displayEx) {
                    System.out.println("❌ Erreur affichage image : " + displayEx.getMessage());
                    displayEx.printStackTrace();
                }

                // ✅ Sauvegarder en BD en arrière-plan (séparé du display)
                new Thread(() -> {
                    try {
                        messageService.envoyerImage(myProfileUser.getId_utilisateur(), idAmi, f);
                    } catch (Exception saveEx) {
                        System.out.println("⚠️ Erreur sauvegarde image BD : " + saveEx.getMessage());
                    }
                }).start();
            });

            // 🎬 Upload vidéo
            Label btnVid = makeToolLabel("🎬");
            btnVid.setOnMouseClicked(e -> {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Choisir une vidéo");
                fc.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter(
                                "Vidéos", "*.mp4","*.avi","*.mkv","*.mov","*.webm","*.flv"));
                java.io.File f = fc.showOpenDialog(chatStage);
                if (f == null) return;
                try {
                    org.model.Utilisateurs.Message msgSaved =
                            messageService.envoyerVideo(myProfileUser.getId_utilisateur(), idAmi, f);
                    Label thumb = new Label();
                    thumb.setStyle(
                            "-fx-background-color: #12122a; -fx-background-radius: 10;" +
                                    "-fx-min-width: 200; -fx-min-height: 100;" +
                                    "-fx-max-width: 200; -fx-max-height: 100;" +
                                    "-fx-border-color: #5a4fcf; -fx-border-width: 1.2; -fx-border-radius: 10;");
                    Label playLbl = new Label("▶");
                    playLbl.setStyle(
                            "-fx-text-fill: white; -fx-font-size: 26px;" +
                                    "-fx-background-color: rgba(123,94,167,0.88);" +
                                    "-fx-background-radius: 50%; -fx-padding: 9 13; -fx-cursor: hand;");
                    StackPane sp = new StackPane(thumb, playLbl);
                    Label nameLbl = new Label("🎬  " + msgSaved.getFichierNom());
                    nameLbl.setStyle(
                            "-fx-text-fill: #c0b0f0; -fx-font-size: 11px;" +
                                    "-fx-padding: 3 8 0 8; -fx-wrap-text: true; -fx-max-width: 184;");
                    long kb = msgSaved.getFichierTaille() / 1024;
                    String sz = kb > 1024 ? String.format("%.1f MB", kb / 1024.0) : kb + " KB";
                    Label szLbl = new Label(sz);
                    szLbl.setStyle("-fx-text-fill: #7070a0; -fx-font-size: 10px; -fx-padding: 0 8 4 8;");
                    VBox card = new VBox(3, sp, nameLbl, szLbl);
                    card.setStyle(
                            "-fx-background-color: #2a2a50; -fx-background-radius: 12;" +
                                    "-fx-padding: 0 0 6 0; -fx-max-width: 208;" +
                                    "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),10,0.3,0,3);");
                    final String savedPath = msgSaved.getFichierPath();
                    javafx.event.EventHandler<javafx.scene.input.MouseEvent> openVid = ev -> {
                        try { java.awt.Desktop.getDesktop().open(
                                java.nio.file.Paths.get(savedPath).toFile()); }
                        catch (Exception ex2) { ex2.printStackTrace(); }
                    };
                    playLbl.setOnMouseClicked(openVid);
                    sp.setOnMouseClicked(openVid);
                    HBox row = new HBox(card);
                    row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    row.setPadding(new Insets(2, 8, 2, 50));
                    messagesBox.getChildren().add(row);
                    javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            // 😊 Toggle emoji picker
            Label btnEmoji = makeToolLabel("😊");
            btnEmoji.setOnMouseClicked(e -> {
                boolean v = emojiPanel.isVisible();
                emojiPanel.setVisible(!v);
                emojiPanel.setManaged(!v);
                if (!v) tfEmojiSearch.requestFocus();
            });

            // 🎤 Message vocal
            Button btnMic = new Button("🎤");
            btnMic.setStyle(
                    "-fx-background-color: transparent; -fx-font-size: 18px;" +
                            "-fx-cursor: hand; -fx-padding: 2 4;"
            );
            Label recLabel = new Label("● Enregistrement...");
            recLabel.setStyle(
                    "-fx-text-fill: #ef4444; -fx-font-size: 10px;" +
                            "-fx-font-weight: bold; -fx-padding: 0 4;");
            recLabel.setVisible(false);
            recLabel.setManaged(false);

            btnMic.setOnMousePressed(e -> {
                recLabel.setVisible(true); recLabel.setManaged(true);
                btnMic.setStyle(
                        "-fx-background-color: #ef444430; -fx-font-size: 18px;" +
                                "-fx-cursor: hand; -fx-background-radius: 50%; -fx-padding: 2 4;");
                demarrerEnregistrement();
            });
            btnMic.setOnMouseReleased(e -> {
                recLabel.setVisible(false); recLabel.setManaged(false);
                btnMic.setStyle(
                        "-fx-background-color: transparent; -fx-font-size: 18px;" +
                                "-fx-cursor: hand; -fx-padding: 2 4;");
                byte[] audio = arreterEnregistrement();
                if (audio == null || audio.length <= 1000) return;
                try {
                    float duree = audio.length / 32000.0f;
                    org.model.Utilisateurs.Message msgSaved =
                            messageService.envoyerVocal(myProfileUser.getId_utilisateur(), idAmi, audio, duree);
                    ajouterBulleVocale(messagesBox, msgSaved.getAudioData(),
                            msgSaved.getDureeVocal(), myProfileUser.getId_utilisateur(), true);
                    javafx.application.Platform.runLater(() -> scrollMessages.setVvalue(1.0));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            // ── Ligne input ──
            HBox inputRow = new HBox(6, tfMessage, btnMic, btnSend);
            inputRow.setAlignment(javafx.geometry.Pos.CENTER);
            inputRow.setPadding(new Insets(6, 8, 4, 8));

            // ── Toolbar ──
            HBox toolRow = new HBox(8, btnImg, btnVid, btnFile, btnEmoji, recLabel);
            toolRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            toolRow.setPadding(new Insets(0, 8, 6, 8));

            VBox bottomBar = new VBox(0, emojiPanel, inputRow, toolRow);
            bottomBar.setStyle(
                    "-fx-background-color: #1a1a2e;" +
                            "-fx-border-color: #2d2d44; -fx-border-width: 1 0 0 0;"
            );

            // ══════════════════════════════════════════
            //  ROOT COMPLET
            // ══════════════════════════════════════════
            VBox chatRoot = new VBox(header, scrollMessages, bottomBar);
            chatRoot.setStyle(
                    "-fx-background-color: #1a1a2e;" +
                            "-fx-background-radius: 10 10 0 0;" +
                            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.6),20,0.5,0,-4);"
            );

            Scene scene = new Scene(chatRoot, CHAT_WIDTH, CHAT_HEIGHT);
            var css = getClass().getResource("/view/css/utilisateur/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            chatStage.setScene(scene);

            repositionnerTous();
            chatStage.show();

            // ══════════════════════════════════════════
            //  LOGIQUE RÉDUIRE / AGRANDIR
            // ══════════════════════════════════════════
            header.setOnMouseClicked(ev -> {
                if (Boolean.TRUE.equals(chatReduit.get(idAmi))) {
                    scrollMessages.setVisible(true);  scrollMessages.setManaged(true);
                    bottomBar.setVisible(true);        bottomBar.setManaged(true);
                    agrandirChat(idAmi, chatStage);
                    btnMin.setText("—");
                    header.setStyle(
                            "-fx-background-color: #1a1a2e;" +
                                    "-fx-border-color: #2d2d44; -fx-border-width: 0 0 1 0;");
                }
            });

            btnMin.setOnAction(e -> {
                if (!Boolean.TRUE.equals(chatReduit.get(idAmi))) {
                    // Réduire
                    scrollMessages.setVisible(false); scrollMessages.setManaged(false);
                    bottomBar.setVisible(false);       bottomBar.setManaged(false);
                    chatReduit.put(idAmi, true);
                    btnMin.setText("▲");
                    header.setStyle(
                            "-fx-background-color: #1a1a2e; -fx-cursor: hand;" +
                                    "-fx-border-color: #2d2d44; -fx-border-width: 0 0 1 0;");
                    chatStage.setHeight(CHAT_HEADER_H);
                    repositionnerTous();
                } else {
                    // Agrandir
                    scrollMessages.setVisible(true);  scrollMessages.setManaged(true);
                    bottomBar.setVisible(true);        bottomBar.setManaged(true);
                    agrandirChat(idAmi, chatStage);
                    btnMin.setText("—");
                    header.setStyle(
                            "-fx-background-color: #1a1a2e;" +
                                    "-fx-border-color: #2d2d44; -fx-border-width: 0 0 1 0;");
                }
            });
        }

        public Message envoyerImage(int idEnvoyeur, int idReceveur, File imageFile) throws IOException {
            // Copier l'image vers un répertoire permanent
            String fileName = "img_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".png";
            Path uploadDir = Paths.get(System.getProperty("user.home"), "SkillSwap", "images");
            Files.createDirectories(uploadDir);
            Path dest = uploadDir.resolve(fileName);
            Files.copy(imageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Sauvegarder en BD
            Message msg = new Message();
            msg.setIdEnvoyeur(idEnvoyeur);
            msg.setIdReceveur(idReceveur);
            msg.setTypeMessage("image");
            msg.setFichierPath(dest.toAbsolutePath().toString()); // ✅ Chemin absolu
            msg.setFichierNom(fileName);
            // ... sauvegarder en BD
            return msg;
        }
    // ════════════════════════════════════════════════════════════════
//  ajouterBulleMessenger()  — COMPLET ET FINAL
// ════════════════════════════════════════════════════════════════
    private void ajouterBulleMessenger(VBox messagesBox,
                                       org.model.Utilisateurs.Message msg,
                                       int monId,
                                       utilisateur ami,
                                       boolean showAvatar) {
        boolean estMoi = (msg.getIdEnvoyeur() == monId);
        String  texte  = msg.getContenu() == null ? "" : msg.getContenu();
        String  type   = msg.getTypeMessage() != null ? msg.getTypeMessage() : "texte";


        // ── FICHIER (PDF, Word, Excel...) ──
        if ("fichier".equals(type) && msg.getFichierPath() != null) {
            afficherCarteFichier(messagesBox,
                    nvl(msg.getFichierNom()),
                    msg.getFichierTaille(),
                    msg.getFichierPath(),
                    estMoi);
            return;
        }
        // ── IMAGE ──
        if ("image".equals(type)) {
            String path = msg.getFichierPath();

            if (path == null || path.isBlank()) {
                afficherTagLabel(messagesBox, "📷  " + nvl(msg.getFichierNom()), estMoi);
                return;
            }

            try {
                java.nio.file.Path p = java.nio.file.Paths.get(path);

                if (!java.nio.file.Files.exists(p)) {
                    System.out.println("❌ Fichier image introuvable : " + path);
                    afficherTagLabel(messagesBox, "📷  Image introuvable", estMoi);
                    return;
                }

                // ✅ background=false → image chargée SYNCHRONEMENT, taille disponible immédiatement
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        p.toUri().toString(), 200, 0, false, false);

                if (img.isError()) {
                    afficherTagLabel(messagesBox, "📷  Erreur image", estMoi);
                    return;
                }

                double w = img.getWidth()  > 0 ? img.getWidth()  : 200;
                double h = img.getHeight() > 0 ? img.getHeight() : 150;
                double displayH = h * 200.0 / w;

                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(displayH);
                iv.setPreserveRatio(false);

                // ✅ Clip avec dimensions connues (pas de layoutBounds listener)
                javafx.scene.shape.Rectangle clipR = new javafx.scene.shape.Rectangle(200, displayH);
                clipR.setArcWidth(14);
                clipR.setArcHeight(14);
                iv.setClip(clipR);

                HBox row = new HBox(iv);
                row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT
                        : javafx.geometry.Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, estMoi ? 8 : 4, 2, estMoi ? 50 : 4));
                messagesBox.getChildren().add(row);
                return;

            } catch (Exception ex) {
                System.out.println("❌ Erreur image historique : " + ex.getMessage());
                afficherTagLabel(messagesBox, "📷  " + nvl(msg.getFichierNom()), estMoi);
                return;
            }
        }
        // ── VIDÉO ──
        if ("video".equals(type) && msg.hasFichier()) {
            Label thumb = new Label();
            thumb.setStyle(
                    "-fx-background-color: #12122a; -fx-background-radius: 10;" +
                            "-fx-min-width: 200; -fx-min-height: 100;" +
                            "-fx-max-width: 200; -fx-max-height: 100;" +
                            "-fx-border-color: #5a4fcf; -fx-border-width: 1.2; -fx-border-radius: 10;");
            Label playLbl = new Label("▶");
            playLbl.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 26px;" +
                            "-fx-background-color: rgba(123,94,167,0.88);" +
                            "-fx-background-radius: 50%; -fx-padding: 9 13; -fx-cursor: hand;");
            StackPane sp = new StackPane(thumb, playLbl);

            Label nameLbl = new Label("🎬  " + nvl(msg.getFichierNom()));
            nameLbl.setStyle("-fx-text-fill: #c0b0f0; -fx-font-size: 11px;" +
                    "-fx-padding: 3 8 0 8; -fx-wrap-text: true; -fx-max-width: 184;");
            long kb = msg.getFichierTaille() / 1024;
            String sz = kb > 1024 ? String.format("%.1f MB", kb/1024.0) : kb + " KB";
            Label szLbl = new Label(sz);
            szLbl.setStyle("-fx-text-fill: #7070a0; -fx-font-size: 10px; -fx-padding: 0 8 4 8;");

            VBox card = new VBox(3, sp, nameLbl, szLbl);
            card.setStyle(
                    "-fx-background-color: #2a2a50; -fx-background-radius: 12;" +
                            "-fx-padding: 0 0 6 0; -fx-max-width: 208;" +
                            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),10,0.3,0,3);");

            final String savedPath = msg.getFichierPath();
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> openVid = ev -> {
                try { java.awt.Desktop.getDesktop().open(
                        java.nio.file.Paths.get(savedPath).toFile()); }
                catch (Exception ex2) { ex2.printStackTrace(); }
            };
            playLbl.setOnMouseClicked(openVid);
            sp.setOnMouseClicked(openVid);

            HBox row = new HBox(card);
            row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, estMoi ? 8 : 4, 2, estMoi ? 50 : 4));
            messagesBox.getChildren().add(row);
            return;
        }

        // ── VOCAL ──
        if ("vocal".equals(type)) {
            byte[] audio = msg.getAudioData();
            float  duree = msg.getDureeVocal() > 0 ? msg.getDureeVocal() : 1f;
            if (audio != null && audio.length > 0) {
                ajouterBulleVocale(messagesBox, audio, duree, monId, estMoi);
            } else {
                afficherTagLabel(messagesBox, "🎤  Message vocal", estMoi);
            }
            return;
        }

        // ── TEXTE / EMOJI normal ──
        Label bulle = new Label(texte);
        bulle.setWrapText(true);
        bulle.setMaxWidth(205);

        if (estMoi) {
            bulle.setStyle(
                    "-fx-background-color: #7b5ea7;" +
                            "-fx-text-fill: white; -fx-font-size: 13px;" +
                            "-fx-padding: 8 14; -fx-background-radius: 18 18 4 18;");
            HBox row = new HBox(bulle);
            row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            row.setPadding(new Insets(1, 8, 1, 50));
            messagesBox.getChildren().add(row);
        } else {
            bulle.setStyle(
                    "-fx-background-color: #3a3a5c;" +
                            "-fx-text-fill: #e8e8f4; -fx-font-size: 13px;" +
                            "-fx-padding: 8 14; -fx-background-radius: 18 18 18 4;");
            if (showAvatar) {
                ImageView av = new ImageView();
                av.setFitWidth(26); av.setFitHeight(26);
                av.setPreserveRatio(true);
                av.setClip(new Circle(13, 13, 13));
                chargerPhoto(ami.getPhoto_profil(), av);
                HBox row = new HBox(6, av, bulle);
                row.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
                row.setPadding(new Insets(1, 50, 1, 4));
                messagesBox.getChildren().add(row);
            } else {
                Region ph = new Region(); ph.setPrefWidth(32);
                HBox row = new HBox(6, ph, bulle);
                row.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
                row.setPadding(new Insets(1, 50, 1, 4));
                messagesBox.getChildren().add(row);
            }
        }
    }

    /** Helper : label tag fallback (image/video/vocal non chargeable) */
    private void afficherTagLabel(VBox box, String texte, boolean estMoi) {
        Label lbl = new Label(texte);
        lbl.setStyle(
                "-fx-background-color:" + (estMoi ? "#7b5ea7" : "#3a3a5c") + ";" +
                        "-fx-text-fill: #d0c0ff; -fx-font-size: 12px;" +
                        "-fx-padding: 8 14; -fx-background-radius: 14; -fx-font-style: italic;");
        HBox row = new HBox(lbl);
        row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, estMoi ? 8 : 4, 2, estMoi ? 50 : 4));
        box.getChildren().add(row);
    }
    private void afficherCarteFichier(VBox messagesBox, String nom, long taille, String path, boolean estMoi) {
        // ── Icône selon extension ──
        String ext = nom.contains(".")
                ? nom.substring(nom.lastIndexOf('.') + 1).toLowerCase() : "file";

        String icone = switch (ext) {
            case "pdf"              -> "📄";
            case "docx", "doc"      -> "📝";
            case "xlsx", "xls"      -> "📊";
            case "pptx", "ppt"      -> "📋";
            case "txt"              -> "📃";
            case "csv"              -> "📈";
            case "zip", "rar"       -> "🗜️";
            default                 -> "📎";
        };

        String couleurExt = switch (ext) {
            case "pdf"              -> "#ef4444";
            case "docx", "doc"      -> "#3b82f6";
            case "xlsx", "xls"      -> "#22c55e";
            case "pptx", "ppt"      -> "#f97316";
            case "zip", "rar"       -> "#a78bfa";
            default                 -> "#94a3b8";
        };

        // ── Taille formatée ──
        String tailleStr;
        if (taille < 1024) tailleStr = taille + " B";
        else if (taille < 1024 * 1024) tailleStr = String.format("%.1f KB", taille / 1024.0);
        else tailleStr = String.format("%.1f MB", taille / (1024.0 * 1024));

        // ── Icône fichier ──
        Label icoLabel = new Label(icone);
        icoLabel.setStyle("-fx-font-size: 28px; -fx-padding: 6;");

        // ── Nom + taille ──
        Label nomLabel = new Label(nom);
        nomLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-max-width: 150; -fx-wrap-text: true;");
        Label tailleLabel = new Label(tailleStr + " · " + ext.toUpperCase());
        tailleLabel.setStyle(
                "-fx-text-fill: " + couleurExt + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        javafx.scene.layout.VBox infos = new javafx.scene.layout.VBox(3, nomLabel, tailleLabel);
        infos.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ── Bouton ouvrir ──
        Button btnOuvrir = new Button("⬇");
        btnOuvrir.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 50%;" +
                        "-fx-min-width: 30; -fx-min-height: 30;" +
                        "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand;");
        btnOuvrir.setOnMouseEntered(ev ->
                btnOuvrir.setStyle(btnOuvrir.getStyle().replace(
                        "rgba(255,255,255,0.12)", "rgba(255,255,255,0.22)")));
        btnOuvrir.setOnMouseExited(ev ->
                btnOuvrir.setStyle(btnOuvrir.getStyle().replace(
                        "rgba(255,255,255,0.22)", "rgba(255,255,255,0.12)")));
        btnOuvrir.setOnAction(ev -> {
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception ex) {
                System.out.println("❌ Impossible d'ouvrir : " + ex.getMessage());
            }
        });

        // ── Carte complète ──
        HBox card = new HBox(10, icoLabel, infos, new Region(), btnOuvrir);
        HBox.setHgrow(infos, javafx.scene.layout.Priority.ALWAYS);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 12, 10, 10));
        card.setMaxWidth(270);
        card.setStyle(
                "-fx-background-color: " + (estMoi ? "#4a3580" : "#2d2d50") + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + couleurExt + "33;" +
                        "-fx-border-width: 1; -fx-border-radius: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.4),8,0.2,0,2);");

        // Clic sur la carte = ouvrir aussi
        card.setOnMouseClicked(ev -> {
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox row = new HBox(card);
        row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT
                : javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, estMoi ? 8 : 4, 3, estMoi ? 20 : 4));
        messagesBox.getChildren().add(row);
    }

// ================================================================
//  ajouterBulleVocale() — MISE À JOUR signature avec duree param
//  Remplacer l'ancienne version
// ================================================================

    private void ajouterBulleVocale(VBox messagesBox, byte[] audioData,
                                    float duree, int monId, boolean estMoi) {
        Button btnPlay = new Button("▶");
        btnPlay.setStyle(
                "-fx-background-color:" + (estMoi ? "#9b7bc7" : "#5a5a7c") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 50%;" +
                        "-fx-min-width: 30; -fx-min-height: 30;" +
                        "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand;");

        javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(0);
        pb.setPrefWidth(110);
        pb.setStyle(
                "-fx-accent:" + (estMoi ? "#c9a8f0" : "#8080b0") + ";" +
                        "-fx-background-color:" + (estMoi ? "#5a3d8a" : "#2d2d50") + ";" +
                        "-fx-background-radius: 4; -fx-pref-height: 4;");

        Label durLbl = new Label(String.format("%.1fs", duree));
        durLbl.setStyle("-fx-text-fill: #c0c0d8; -fx-font-size: 10px;");
        Label micIco = new Label("🎤");
        micIco.setStyle("-fx-font-size: 13px;");

        HBox inner = new HBox(6, btnPlay, micIco, pb, durLbl);
        inner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        inner.setPadding(new Insets(8, 14, 8, 10));
        inner.setStyle(
                "-fx-background-color:" + (estMoi ? "#7b5ea7" : "#3a3a5c") + ";" +
                        "-fx-background-radius: 18; -fx-max-width: 225;");

        final boolean[] playing = {false};
        final javafx.animation.Timeline[] anim = {null};

        btnPlay.setOnAction(ev -> {
            if (!playing[0]) {
                playing[0] = true; btnPlay.setText("⏸");
                anim[0] = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(duree),
                                new javafx.animation.KeyValue(pb.progressProperty(), 1.0)));
                anim[0].setOnFinished(e2 -> {
                    playing[0] = false; btnPlay.setText("▶"); pb.setProgress(0);
                });
                anim[0].play();

                if (audioData != null && audioData.length > 0) {
                    new Thread(() -> {
                        try {
                            javax.sound.sampled.AudioFormat fmt =
                                    new javax.sound.sampled.AudioFormat(16000, 16, 1, true, false);
                            javax.sound.sampled.AudioInputStream ais =
                                    new javax.sound.sampled.AudioInputStream(
                                            new java.io.ByteArrayInputStream(audioData), fmt,
                                            audioData.length / fmt.getFrameSize());
                            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                            clip.open(ais); clip.start();
                            clip.addLineListener(e2 -> {
                                if (e2.getType() == javax.sound.sampled.LineEvent.Type.STOP)
                                    javafx.application.Platform.runLater(() -> {
                                        playing[0] = false; btnPlay.setText("▶");
                                    });
                            });
                        } catch (Exception ex2) { ex2.printStackTrace(); }
                    }).start();
                }
            } else {
                playing[0] = false; btnPlay.setText("▶");
                if (anim[0] != null) anim[0].pause();
            }
        });

        HBox row = new HBox(inner);
        row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, estMoi ? 8 : 4, 2, estMoi ? 50 : 4));
        messagesBox.getChildren().add(row);
    }

// ════════════════════════════════════════════════════════════════
//  MÉTHODES UTILITAIRES  (ajouter dans la classe)
// ════════════════════════════════════════════════════════════════

    private void repositionnerTous() {
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        java.util.List<Integer> ids = new java.util.ArrayList<>(chatStages.keySet());
        for (int i = ids.size() - 1; i >= 0; i--) {
            int id = ids.get(i);
            Stage st = chatStages.get(id);
            if (st == null) continue;
            int pos = ids.size() - 1 - i;
            double x = screen.getMaxX() - (CHAT_WIDTH + CHAT_GAP) * (pos + 1);
            boolean reduit = Boolean.TRUE.equals(chatReduit.get(id));
            double h = reduit ? CHAT_HEADER_H : CHAT_HEIGHT;
            st.setX(x);
            st.setY(screen.getMaxY() - h);
            if (!st.isShowing()) st.show();
        }
    }

    private void agrandirChat(int idAmi, Stage chatStage) {
        chatReduit.put(idAmi, false);
        chatStage.setHeight(CHAT_HEIGHT);
        repositionnerTous();
    }

    private void ajouterBulleVocale(VBox messagesBox, byte[] audioData,
                                    int monId, boolean estMoi) {
        Button btnPlay = new Button("▶");
        btnPlay.setStyle(
                "-fx-background-color:" + (estMoi ? "#9b7bc7" : "#5a5a7c") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 50%;" +
                        "-fx-min-width: 30; -fx-min-height: 30;" +
                        "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand;");

        javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(0);
        pb.setPrefWidth(110);
        pb.setStyle("-fx-accent:" + (estMoi ? "#c9a8f0" : "#8080b0") + ";" +
                "-fx-background-color:" + (estMoi ? "#5a3d8a" : "#2d2d50") + ";" +
                "-fx-background-radius: 4; -fx-pref-height: 4;");

        double duree = audioData.length / 32000.0;
        Label durLbl = new Label(String.format("%.1fs", duree));
        durLbl.setStyle("-fx-text-fill: #c0c0d8; -fx-font-size: 10px;");
        Label micIco = new Label("🎤");
        micIco.setStyle("-fx-font-size: 13px;");

        HBox inner = new HBox(6, btnPlay, micIco, pb, durLbl);
        inner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        inner.setPadding(new Insets(8, 14, 8, 10));
        inner.setStyle(
                "-fx-background-color:" + (estMoi ? "#7b5ea7" : "#3a3a5c") + ";" +
                        "-fx-background-radius: 18; -fx-max-width: 225;");

        final boolean[] playing = {false};
        final javafx.animation.Timeline[] anim = {null};

        btnPlay.setOnAction(e -> {
            if (!playing[0]) {
                playing[0] = true; btnPlay.setText("⏸");
                anim[0] = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(duree),
                                new javafx.animation.KeyValue(pb.progressProperty(), 1.0)));
                anim[0].setOnFinished(ev -> {
                    playing[0] = false; btnPlay.setText("▶"); pb.setProgress(0);
                });
                anim[0].play();
                new Thread(() -> {
                    try {
                        AudioFormat fmt = new AudioFormat(16000, 16, 1, true, false);
                        AudioInputStream ais = new AudioInputStream(
                                new ByteArrayInputStream(audioData), fmt,
                                audioData.length / fmt.getFrameSize());
                        Clip clip = AudioSystem.getClip();
                        clip.open(ais); clip.start();
                        clip.addLineListener(ev2 -> {
                            if (ev2.getType() == LineEvent.Type.STOP)
                                javafx.application.Platform.runLater(() -> {
                                    playing[0] = false; btnPlay.setText("▶");
                                });
                        });
                    } catch (Exception ex) { ex.printStackTrace(); }
                }).start();
            } else {
                playing[0] = false; btnPlay.setText("▶");
                if (anim[0] != null) anim[0].pause();
            }
        });

        HBox row = new HBox(inner);
        row.setAlignment(estMoi ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, estMoi ? 8 : 4, 2, estMoi ? 50 : 4));
        messagesBox.getChildren().add(row);
    }

    private void demarrerEnregistrement() {
        try {
            AudioFormat fmt = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) return;
            micLine = (TargetDataLine) AudioSystem.getLine(info);
            micLine.open(fmt); micLine.start();
            audioBuffer = new ByteArrayOutputStream();
            recordThread = new Thread(() -> {
                byte[] buf = new byte[1024];
                while (micLine != null && micLine.isOpen()) {
                    int n = micLine.read(buf, 0, buf.length);
                    if (n > 0) audioBuffer.write(buf, 0, n);
                }
            });
            recordThread.setDaemon(true);
            recordThread.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private byte[] arreterEnregistrement() {
        try {
            if (micLine != null) { micLine.stop(); micLine.close(); micLine = null; }
            if (recordThread != null) { recordThread.interrupt(); recordThread = null; }
            return audioBuffer != null ? audioBuffer.toByteArray() : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private Label makeIconLabel(String emoji) {
        Label l = new Label(emoji);
        l.setStyle("-fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 50%;");
        l.setOnMouseEntered(e -> l.setStyle(l.getStyle() + "-fx-background-color: #2d2d44;"));
        l.setOnMouseExited(e -> l.setStyle("-fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 50%;"));
        return l;
    }

    private void styleSmallBtn(Button b, String fs, String pad) {
        String p = pad != null ? pad : "0";
        b.setStyle(
                "-fx-background-color: #2d2d44; -fx-text-fill: #c0c0d8;" +
                        "-fx-font-size:" + fs + "; -fx-background-radius: 50%;" +
                        "-fx-min-width: 26; -fx-min-height: 26;" +
                        "-fx-max-width: 26; -fx-max-height: 26;" +
                        "-fx-cursor: hand; -fx-padding:" + p + ";");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("#2d2d44","#4a3fa0")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("#4a3fa0","#2d2d44")));
    }

    private Label makeToolLabel(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 3; -fx-background-radius: 50%; -fx-text-fill: #a78bfa;");
        l.setOnMouseEntered(e -> l.setStyle(l.getStyle() + "-fx-background-color: #2d2d44;"));
        l.setOnMouseExited(e  -> l.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 3; -fx-background-radius: 50%; -fx-text-fill: #a78bfa;"));
        return l;
    }


// ----------------------------------------------------------------
// E) ENVOI MESSAGE TEXTE (helper)
// ----------------------------------------------------------------

    private void envoyerTexte(VBox messagesBox, String texte, int idAmi,
                              utilisateur ami, ScrollPane scroll) {
        try {
            messageService.envoyer(myProfileUser.getId_utilisateur(), idAmi, texte);
            org.model.Utilisateurs.Message msg = new org.model.Utilisateurs.Message();
            msg.setIdEnvoyeur(myProfileUser.getId_utilisateur());
            msg.setIdReceveur(idAmi);
            msg.setContenu(texte);
            ajouterBulleMessenger(messagesBox, msg, myProfileUser.getId_utilisateur(), ami, false);
            javafx.application.Platform.runLater(() -> scroll.setVvalue(1.0));
        } catch (Exception ex) { ex.printStackTrace(); }
    }


// ----------------------------------------------------------------
// I) HELPERS VISUELS
// ----------------------------------------------------------------

    /** Crée une icône bouton pour le header (téléphone, vidéo…) */
    private Label iconBtn(String emoji) {
        Label l = new Label(emoji);
        l.setStyle(
                "-fx-font-size: 16px; -fx-cursor: hand;" +
                        "-fx-padding: 4; -fx-background-radius: 50%;"
        );
        l.setOnMouseEntered(e -> l.setStyle(l.getStyle() + "-fx-background-color: #2d2d44;"));
        l.setOnMouseExited(e  -> l.setStyle(
                "-fx-font-size: 16px; -fx-cursor: hand;" +
                        "-fx-padding: 4; -fx-background-radius: 50%;"
        ));
        return l;
    }

    /** Bouton style header (—, ✕) */
    private void styleHeaderBtn(Button b, String fontSize, String pad) {
        String p = pad != null ? pad : "0";
        b.setStyle(
                "-fx-background-color: #2d2d44; -fx-text-fill: #c0c0d8;" +
                        "-fx-font-size: " + fontSize + ";" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 26; -fx-min-height: 26;" +
                        "-fx-max-width: 26; -fx-max-height: 26;" +
                        "-fx-cursor: hand; -fx-padding: " + p + ";"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("#2d2d44", "#3d3d5c")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("#3d3d5c", "#2d2d44")));
    }

    /** Icône de la barre d'outils (📷 🎬 😊 GIF) */
    private Label toolBtn(String txt) {
        Label l = new Label(txt);
        l.setStyle(
                "-fx-font-size: 18px; -fx-cursor: hand;" +
                        "-fx-padding: 4; -fx-background-radius: 50%;" +
                        "-fx-text-fill: #a78bfa;"
        );
        l.setOnMouseEntered(e -> l.setStyle(l.getStyle() + "-fx-background-color: #2d2d44;"));
        l.setOnMouseExited(e  -> l.setStyle(
                "-fx-font-size: 18px; -fx-cursor: hand;" +
                        "-fx-padding: 4; -fx-background-radius: 50%;" +
                        "-fx-text-fill: #a78bfa;"
        ));
        return l;
    }




// ----------------------------------------------------------------
// D) REMPLACER ajouterBulleMessage() (version inchangée, juste pour référence)
// ----------------------------------------------------------------

    private void ajouterBulleMessage(VBox messagesBox, org.model.Utilisateurs.Message msg,
                                     int monId, utilisateur ami, boolean showAvatar) {
        boolean estMoi = (msg.getIdEnvoyeur() == monId);
        Label bulle = new Label(msg.getContenu());
        bulle.setWrapText(true);
        bulle.setMaxWidth(210);
        bulle.getStyleClass().add(estMoi ? "msg-bubble-me" : "msg-bubble-other");

        if (estMoi) {
            HBox ligne = new HBox(bulle);
            ligne.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            ligne.setPadding(new Insets(1, 4, 1, 50));
            messagesBox.getChildren().add(ligne);
        } else {
            if (showAvatar) {
                ImageView av = new ImageView();
                av.setFitWidth(28); av.setFitHeight(28);
                av.setPreserveRatio(true);
                av.setClip(new Circle(14, 14, 14));
                chargerPhoto(ami.getPhoto_profil(), av);
                HBox ligne = new HBox(6, av, bulle);
                ligne.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
                ligne.setPadding(new Insets(1, 50, 1, 4));
                messagesBox.getChildren().add(ligne);
            } else {
                Region placeholder = new Region();
                placeholder.setPrefWidth(34);
                HBox ligne = new HBox(6, placeholder, bulle);
                ligne.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
                ligne.setPadding(new Insets(1, 50, 1, 4));
                messagesBox.getChildren().add(ligne);
            }
        }
    }

    // =========================
    // HELPER : charger photo
    // =========================
    private void chargerPhoto(String photoPath, ImageView iv) {
        boolean loaded = false;
        if (photoPath != null && !photoPath.isBlank()) {
            try {
                Path p = Paths.get(photoPath);
                if (p.isAbsolute() && Files.exists(p)) {
                    iv.setImage(new Image(p.toUri().toString(), true));
                    loaded = true;
                }
                if (!loaded) {
                    var stream = getClass().getResourceAsStream(photoPath);
                    if (stream != null) { iv.setImage(new Image(stream)); loaded = true; }
                }
            } catch (Exception ignored) {}
        }
        if (!loaded) {
            var stream = getClass().getResourceAsStream("/view/image/utilisateur/avatar.png");
            if (stream != null) iv.setImage(new Image(stream));
        }
    }

    // =========================
    // SEARCH
    // =========================
    @FXML
    private void onSearchKey() {
        String q = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
        long now = System.currentTimeMillis();
        if (now - lastSearchTs < 200) return;
        lastSearchTs = now;

        if (q.length() < 2) {
            suggestionsMenu.hide();
            if (q.isEmpty()) returnToMyProfileIfNeeded();
            if (lblMsg != null) lblMsg.setText("");
            return;
        }
        searchDelay.stop();
        searchDelay.setOnFinished(e -> fetchAndShowSuggestions(q));
        searchDelay.playFromStart();
    }

    @FXML
    private void onSearchClick(ActionEvent e) {
        String q = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
        if (q.length() < 2) {
            if (lblMsg != null) lblMsg.setText("Tape au moins 2 lettres.");
            return;
        }
        suggestionsMenu.hide();
        searchAndDisplay(q);
    }

    private void fetchAndShowSuggestions(String q) {
        if (suppressAutoComplete) return;
        try {
            List<utilisateur> results = service.searchSuggestions(q, SUGGEST_LIMIT);
            if (results == null || results.isEmpty()) { suggestionsMenu.hide(); return; }

            List<CustomMenuItem> items = results.stream()
                    .map(this::buildSuggestionItem).collect(Collectors.toList());
            suggestionsMenu.getItems().setAll(items);

            double width = tfSearch.getWidth() > 0 ? tfSearch.getWidth() : tfSearch.getPrefWidth();
            suggestionsMenu.setStyle(
                    "-fx-min-width: " + width + "px;" +
                            "-fx-pref-width: " + width + "px;" +
                            "-fx-max-width: " + width + "px;");

            tfSearch.widthProperty().addListener((obs, oldW, newW) -> {
                suggestionsMenu.setMinWidth(newW.doubleValue());
                suggestionsMenu.setPrefWidth(newW.doubleValue());
            });

            if (!suggestionsMenu.isShowing())
                suggestionsMenu.show(tfSearch, Side.BOTTOM, 0, 0);

        } catch (Exception ex) { ex.printStackTrace(); suggestionsMenu.hide(); }
    }

    private CustomMenuItem buildSuggestionItem(utilisateur u) {
        final String baseName = (nvl(u.getNom()) + " " + nvl(u.getPrenom())).trim();
        final String fullName = !baseName.isBlank() ? baseName : nvl(u.getEmail());

        String sub = nvl(u.getRole());
        if (!nvl(u.getEmail()).isBlank()) {
            if (!sub.isBlank()) sub += " • ";
            sub += nvl(u.getEmail());
        }

        ImageView avatar = new ImageView();
        avatar.setFitWidth(38); avatar.setFitHeight(38);
        avatar.setPreserveRatio(true); avatar.setSmooth(true);
        avatar.setClip(new Circle(19, 19, 19));
        chargerPhoto(u.getPhoto_profil(), avatar);

        Label title = new Label(fullName);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label subtitle = new Label(sub);
        subtitle.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, title, subtitle);
        textBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox row = new HBox(10, avatar, textBox);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 6 12;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-padding: 6 12;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 6 12;"));

        CustomMenuItem item = new CustomMenuItem(row, true);
        item.setOnAction(e -> {
            suppressAutoComplete = true;
            tfSearch.setText(fullName);
            tfSearch.positionCaret(tfSearch.getText().length());
            suppressAutoComplete = false;
            suggestionsMenu.hide();
            setUser(u);
            if (lblMsg != null) lblMsg.setText("Profil trouvé ✅");
        });
        return item;
    }

    private void searchAndDisplay(String q) {
        try {
            utilisateur u = service.findByNomOrPrenom(q);
            if (u == null) {
                if (lblMsg != null) lblMsg.setText("Aucun utilisateur trouvé pour : " + q);
                return;
            }
            setUser(u);
            if (lblMsg != null) lblMsg.setText("Profil trouvé ✅");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur recherche : " + ex.getMessage());
        }
    }

    private void returnToMyProfileIfNeeded() {
        if (myProfileUser != null && currentUser != myProfileUser) {
            setUser(myProfileUser);
            if (lblMsg != null) lblMsg.setText("");
        }
    }

    // =========================
    // NAVIGATION
    // =========================
    @FXML private void logout(ActionEvent e)     { goTo("/view/fxml/utilisateur/login.fxml",              (Node) e.getSource(), "SkillSwap - Connexion",   "/view/css/utilisateur/style.css"); }
    @FXML private void compButton(ActionEvent e) { goTo("/view/fxml/Competences/back/competence.fxml",    (Node) e.getSource(), "SkillSwap - Compétences", "/view/css/competences/back/style.css"); }
    @FXML private void compLabel(MouseEvent e)   { goTo("/view/fxml/Competences/back/competence.fxml",    (Node) e.getSource(), "SkillSwap - Compétences", "/view/css/competences/back/style.css"); }

    @FXML
    private void suivistach(ActionEvent event) {
        try {
            // ✅ Mettre à jour la session AVANT de naviguer
            if (currentUser != null) {
                Session.setCurrentUser(currentUser);
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/suivi_tache/back/gestion_projet.fxml")
            );
            Parent root = loader.load();

            // ✅ Récupérer le controller pour brancher le callback
            GestionProjetController ctrl = loader.getController();
            if (ctrl != null) {
                ctrl.setOnProjetChangedCallback(() ->
                        javafx.application.Platform.runLater(() -> {
                            loadProjetsChef();
                            loadChefProjects();
                            chargerTachesTexte(currentUser.getId_utilisateur());
                        })
                );
            }

            Scene scene = new Scene(root, 1920, 1000);
            var css = getClass().getResource("/view/css/suiviTache/back/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Gestion Projet");
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur navigation : " + ex.getMessage());
        }
    }

    @FXML
    private void redv(ActionEvent event) {
        goTo("/view/fxml/RendezVous/back/RendezVous.fxml",
                (Node) event.getSource(), "Les Rendez-Vous", "/view/css/RendezVous/back/style.css");
    }

    private void goTo(String fxmlPath, Node source, String title, String cssPath) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) throw new RuntimeException("FXML introuvable: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1000);
            var css = getClass().getResource(cssPath);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur navigation : " + ex.getMessage());
        }
    }
    @FXML
    private void goToOffres(ActionEvent event) {
        try {
            var url = getClass().getResource("/view/fxml/Offres/back/OffreBack.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // ← passe l'utilisateur pour que retourProfil fonctionne
            org.controller.Offres.back.OffreBackController ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setUser(currentUser);

            Scene scene = new Scene(root, 1920, 1000);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SkillSwap — Gestion Offres");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (lblMsg != null) lblMsg.setText("Erreur : " + e.getMessage());
        }
    }

    // =========================
    // HELPERS
    // =========================
    private static String nvl(String s) { return s == null ? "" : s; }
}