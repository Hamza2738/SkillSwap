package org.controller.RendezVous.front;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.model.RendezVous.postulation;
import org.model.RendezVous.rendez_vous;
import org.service.RendezVous.PostulationService;
import org.service.RendezVous.RendezVousService;
import org.utils.Session;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class PostulationsController {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final RendezVousService rvService = new RendezVousService();
    private final PostulationService postService = new PostulationService();

    // ===== TABLE RENDEZ-VOUS =====
    @FXML
    private TableView<rendez_vous> tableRv;
    @FXML
    private TableColumn<rendez_vous, String> colChefMail;
    @FXML
    private TableColumn<rendez_vous, String> colTitre;
    @FXML
    private TableColumn<rendez_vous, String> colType;
    @FXML
    private TableColumn<rendez_vous, String> colCompetence;
    @FXML
    private TableColumn<rendez_vous, LocalDateTime> colDate;
    @FXML
    private TableColumn<rendez_vous, LocalDateTime> colDateCreation;
    @FXML
    private TableColumn<rendez_vous, String> colRvNombrePlaces;

    // ===== FILTRE =====
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private TextField searchTextField;

    // ===== FORMULAIRE RV =====
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private ComboBox<String> typeBox;
    @FXML
    private TextField competenceField;
    @FXML
    private TextField nombrePlacesField;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    // ===== TABLE POSTULATIONS (unique) =====
    @FXML
    private TableView<postulation> tableMesPost;
    @FXML
    private TableColumn<postulation, String> colStatus;
    @FXML
    private TableColumn<postulation, LocalDateTime> colDatePost;
    @FXML
    private TableColumn<postulation, String> colNom;
    @FXML
    private TableColumn<postulation, String> colPrenom;
    @FXML
    private TableColumn<postulation, String> colRole;
    @FXML
    private TableColumn<postulation, String> colEmail;
    @FXML
    private TableColumn<postulation, String> colCvPost;
    @FXML
    private TableColumn<postulation, String> colMsgPost;
    @FXML
    private TableColumn<postulation, String> colDeletePost;

    // ===== BOUTONS ACCEPTER / REFUSER =====
    @FXML
    private Button btnAccepter;
    @FXML
    private Button btnRefuser;

    // ===== FORMULAIRE POSTULER =====
    @FXML
    private VBox formPostulerBox;
    @FXML
    private Button pdfButton;
    @FXML
    private TextField cvField;
    @FXML
    private TextArea messageField;

    private String cvPath = "";
    private int currentUserId;
    private String currentUserEmail;
    private static final String SMTP_USER = "skillswapskillswap@gmail.com";
    private static final String SMTP_PASS = "rhmq cwok iwsj jbru";


    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        if (Session.getCurrentUser() == null) {
            showWarning("Aucun utilisateur connecté.");
            return;
        }
        currentUserId = Session.getCurrentUser().getId_utilisateur();
        currentUserEmail = Session.getCurrentUser().getEmail();

        // Tous désactivés par défaut
        if (btnModifier != null) btnModifier.setDisable(true);
        if (btnSupprimer != null) btnSupprimer.setDisable(true);
        if (btnAccepter != null) btnAccepter.setDisable(true);
        if (btnRefuser != null) btnRefuser.setDisable(true);

        // Icône PDF
        if (pdfButton != null) {
            try {
                var stream = getClass().getResourceAsStream("/view/files/RendezVous/pdf-logo.png");
                if (stream != null) {
                    ImageView iv = new ImageView(new Image(stream));
                    iv.setFitHeight(24);
                    iv.setFitWidth(24);
                    iv.setPreserveRatio(true);
                    pdfButton.setGraphic(iv);
                    pdfButton.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ComboBoxes / Spinners
        if (typeBox != null)
            typeBox.setItems(FXCollections.observableArrayList("presentiel", "distance"));
        if (hourSpinner != null)
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        if (minuteSpinner != null)
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        if (filterComboBox != null) {
            filterComboBox.setItems(FXCollections.observableArrayList("Tous", "Titre", "Compétence", "Type", "Email"));
            filterComboBox.setValue("Tous");
        }

        // ---- Colonnes TABLE RENDEZ-VOUS ----
        if (colChefMail != null)
            colChefMail.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getMail_user())));
        if (colTitre != null)
            colTitre.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getTitre())));
        if (colType != null) colType.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getType())));
        if (colCompetence != null)
            colCompetence.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getCompetence())));
        if (colRvNombrePlaces != null)
            colRvNombrePlaces.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getNombre_places())));
        if (colDate != null) {
            colDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDate_rendez_vous()));
            colDate.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime dt, boolean empty) {
                    super.updateItem(dt, empty);
                    setText(empty || dt == null ? "" : dt.format(DT_FMT));
                }
            });
        }
        if (colDateCreation != null) {
            colDateCreation.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDate_creation_rendez_vous()));
            colDateCreation.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime dt, boolean empty) {
                    super.updateItem(dt, empty);
                    setText(empty || dt == null ? "" : dt.format(DT_FMT));
                }
            });
        }

        // ---- Colonnes TABLE POSTULATIONS ----
        if (colStatus != null)
            colStatus.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getStatus())));
        if (colNom != null) colNom.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getNom())));
        if (colPrenom != null)
            colPrenom.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getPrenom())));
        if (colRole != null) colRole.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getRole())));
        if (colEmail != null)
            colEmail.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getMail_user())));
        if (colMsgPost != null)
            colMsgPost.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getMessage())));
        if (colDatePost != null) {
            colDatePost.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDate_postulation()));
            colDatePost.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime dt, boolean empty) {
                    super.updateItem(dt, empty);
                    setText(empty || dt == null ? "" : dt.format(DT_FMT));
                }
            });
        }
        if (colCvPost != null) {
            colCvPost.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getCv())));
            colCvPost.setCellFactory(p -> new TableCell<>() {
                @Override
                protected void updateItem(String cv, boolean empty) {
                    super.updateItem(cv, empty);
                    if (!empty && cv != null && !cv.isEmpty()) {
                        Button b = new Button("Télécharger CV");
                        b.setStyle("-fx-background-color:transparent;-fx-text-fill:#6c8cff;");
                        b.setOnAction(e -> handleDownloadCv(cv));
                        setGraphic(b);
                    } else {
                        setGraphic(null);
                    }
                }
            });
        }
        if (colDeletePost != null) {
            colDeletePost.setCellValueFactory(d -> new SimpleStringProperty("Annuler"));
            colDeletePost.setCellFactory(p -> new TableCell<>() {
                private final Button btn = new Button("Annuler");

                {
                    btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ef4444;");
                    btn.setOnAction(e -> {
                        postulation post = getTableRow().getItem();
                        if (post != null) handleDeletePostulation(post);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });
        }

        // ---- Sélection dans tableMesPost → activer/désactiver Accepter/Refuser ----
        // Les boutons sont actifs UNIQUEMENT si le RV sélectionné m'appartient
        if (tableMesPost != null) {
            tableMesPost.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                refreshAccepterRefuserButtons();
            });
        }

        // ---- Sélection RV → charger postulations + rafraîchir tout ----
        if (tableRv != null) {
            tableRv.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    remplirFormRv(newV);
                    chargerPostulationsDuRv(newV.getId_rendez_vous());
                } else {
                    viderFormRv();
                    if (tableMesPost != null) tableMesPost.getItems().clear();
                }
                refreshActionButtons();
                refreshFormPostuler();
                refreshAccepterRefuserButtons();
            });
        }

        chargerRendezVous();
    }

    // ============================================================
    // CHARGER POSTULATIONS DU RV SÉLECTIONNÉ
    // ============================================================
    private void chargerPostulationsDuRv(int idRv) {
        try {
            if (tableMesPost != null) {
                List<postulation> posts = new ArrayList<>(postService.afficherParRendezVous(idRv));
                // Mes postulations en premier
                posts.sort((a, b) -> {
                    boolean aMe = a.getId_postulant() == currentUserId;
                    boolean bMe = b.getId_postulant() == currentUserId;
                    if (aMe && !bMe) return -1;
                    if (!aMe && bMe) return 1;
                    return 0;
                });
                tableMesPost.setItems(FXCollections.observableArrayList(posts));
            }
        } catch (SQLException e) {
            showError("Erreur chargement postulations : " + e.getMessage());
        }
    }

    private void filterAllColumns(String t) throws SQLException {
        List<rendez_vous> tous = new ArrayList<>(rvService.afficherTous());
        tous.sort((a, b) -> Boolean.compare(!isOwner(a), !isOwner(b)));
        FilteredList<rendez_vous> f = new FilteredList<>(FXCollections.observableArrayList(tous), p -> true);
        f.setPredicate(rv -> rv.getTitre().toLowerCase().contains(t)
                || rv.getCompetence().toLowerCase().contains(t)
                || safe(rv.getMail_user()).toLowerCase().contains(t)
                || rv.getType().toLowerCase().contains(t));
        tableRv.setItems(f);
    }

    // ============================================================
    // ACCEPTER — uniquement si je suis owner du RV
    // ============================================================
    @FXML
    private void accepterPostulation(ActionEvent e) {
        rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
        if (rv == null || !isOwner(rv)) {
            showWarning("Vous ne pouvez accepter que les postulations de vos propres rendez-vous.");
            return;
        }
        postulation p = tableMesPost.getSelectionModel().getSelectedItem();
        if (p == null) {
            showWarning("Sélectionnez une postulation.");
            return;
        }
        if (!confirm("Accepter", "Accepter la postulation de "
                + safe(p.getNom()) + " " + safe(p.getPrenom()) + " ?")) return;
        try {
            postService.changerStatus(p.getId_postulation(), "accepte");
            chargerPostulationsDuRv(rv.getId_rendez_vous());
            envoyerMailAcceptation(p, rv); // ← AJOUT
            showInfo("Postulation acceptée ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    // ============================================================
    // REFUSER — uniquement si je suis owner du RV
    // ============================================================
    @FXML
    private void refuserPostulation(ActionEvent e) {
        rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
        if (rv == null || !isOwner(rv)) {
            showWarning("Vous ne pouvez refuser que les postulations de vos propres rendez-vous.");
            return;
        }
        postulation p = tableMesPost.getSelectionModel().getSelectedItem();
        if (p == null) {
            showWarning("Sélectionnez une postulation.");
            return;
        }
        if (!confirm("Refuser", "Refuser la postulation de "
                + safe(p.getNom()) + " " + safe(p.getPrenom()) + " ?")) return;
        try {
            postService.changerStatus(p.getId_postulation(), "refuse");
            chargerPostulationsDuRv(rv.getId_rendez_vous());
            showInfo("Postulation refusée ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    // ============================================================
    // RAFRAÎCHIR BOUTONS ACCEPTER/REFUSER
    // Actifs seulement si : RV sélectionné + j'en suis owner + une postulation est sélectionnée
    // ============================================================
    private void refreshAccepterRefuserButtons() {
        rendez_vous rv = tableRv == null ? null : tableRv.getSelectionModel().getSelectedItem();
        postulation p = tableMesPost == null ? null : tableMesPost.getSelectionModel().getSelectedItem();

        boolean peutGerer = (rv != null) && isOwner(rv) && (p != null);

        if (btnAccepter != null) btnAccepter.setDisable(!peutGerer);
        if (btnRefuser != null) btnRefuser.setDisable(!peutGerer);
    }

    // ============================================================
    // FILTRE / RECHERCHE
    // ============================================================
    @FXML
    private void onSearchKeyReleased(javafx.scene.input.KeyEvent event) throws SQLException {
        String text = searchTextField.getText().toLowerCase();
        String filter = filterComboBox.getValue();
        if ("Tous".equals(filter)) filterAllColumns(text);
        else if ("Titre".equals(filter)) filterByTitle(text);
        else if ("Compétence".equals(filter)) filterByCompetence(text);
        else if ("Type".equals(filter)) filterByType(text);
        else if ("Email".equals(filter)) filterByMail(text);
    }

    private void filterByTitle(String t) throws SQLException {
        FilteredList<rendez_vous> f = new FilteredList<>(FXCollections.observableArrayList(rvService.afficherTous()), p -> true);
        f.setPredicate(rv -> rv.getTitre().toLowerCase().contains(t));
        tableRv.setItems(f);
    }

    private void filterByCompetence(String t) throws SQLException {
        FilteredList<rendez_vous> f = new FilteredList<>(FXCollections.observableArrayList(rvService.afficherTous()), p -> true);
        f.setPredicate(rv -> rv.getCompetence().toLowerCase().contains(t));
        tableRv.setItems(f);
    }

    private void filterByType(String t) throws SQLException {
        FilteredList<rendez_vous> f = new FilteredList<>(FXCollections.observableArrayList(rvService.afficherTous()), p -> true);
        f.setPredicate(rv -> rv.getType().toLowerCase().contains(t));
        tableRv.setItems(f);
    }

    private void filterByMail(String t) throws SQLException {
        FilteredList<rendez_vous> f = new FilteredList<>(FXCollections.observableArrayList(rvService.afficherTous()), p -> true);
        f.setPredicate(rv -> safe(rv.getMail_user()).toLowerCase().contains(t));
        tableRv.setItems(f);
    }


    private void envoyerMailAcceptation(postulation p, rendez_vous rv) {
        String destinataire = p.getMail_user();
        String titre = safe(rv.getTitre());
        String type = safe(rv.getType());
        String dateStr = rv.getDate_rendez_vous() != null
                ? rv.getDate_rendez_vous().format(DT_FMT) : "date non définie";

        String sujet = "✅ Postulation acceptée - " + titre;
        String corps;

        if ("distance".equalsIgnoreCase(type)) {
            corps = "<html><body style='font-family:Arial,sans-serif;'>"
                    + "<h2 style='color:#4CAF50;'>Félicitations ! Votre postulation a été acceptée 🎉</h2>"
                    + "<p>Votre postulation pour le rendez-vous : <strong>\"" + titre + "\"</strong> a été acceptée.</p>"
                    + "<p>Entrez le lien Meet : "
                    + "<a href='https://meet.google.com/tgi-yomj-ngb' style='color:#6c8cff;'>"
                    + "https://meet.google.com/tgi-yomj-ngb</a></p>"
                    + "<p>📅 À : <strong>" + dateStr + "</strong></p>"
                    + "<br><p style='color:#888;'>— L'équipe SkillSwap</p>"
                    + "</body></html>";
        } else {
            String adresse = safe(rv.getDescription());
            if (adresse.isEmpty()) adresse = "Tunis, Tunisie";

            corps = "<html><body style='font-family:Arial,sans-serif; max-width:650px; margin:auto;'>"
                    + "<div style='background:#1a1a2e; padding:30px; border-radius:16px;'>"
                    + "<h2 style='color:#4CAF50;'>Félicitations ! 🎉</h2>"
                    + "<p style='color:#ffffff;'>Votre postulation pour le rendez-vous : <strong>\""
                    + titre + "\"</strong> a été acceptée.</p>"
                    + "<p style='color:#ffffff;'>📅 À : <strong>" + dateStr + "</strong></p>"
                    + "<p style='color:#ffffff;'>📍 Localisation : <strong>" + adresse + "</strong></p>"
                    + "<br>"
                    + "<img src='https://i.imgur.com/3tbXItf.png' "
                    + "width='600' height='300' "
                    + "alt='Carte du lieu' "
                    + "style='border-radius:12px; border:2px solid #4CAF50; display:block;' />"
                    + "<br>"
                    + "<p style='color:#aaaaaa; font-size:12px;'>— L'équipe SkillSwap</p>"
                    + "</div>"
                    + "</body></html>";
        }

        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                // ✅ FIX : nom complet pour éviter le conflit avec org.utils.Session
                jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                    }
                });

                Message message = new MimeMessage(mailSession); // ✅ mailSession
                message.setFrom(new InternetAddress(SMTP_USER, "SkillSwap"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
                message.setSubject(sujet);
                message.setContent(corps, "text/html; charset=utf-8");
                Transport.send(message);

                javafx.application.Platform.runLater(() ->
                        showInfo("Mail envoyé à " + destinataire + " ✅"));
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        showError("Erreur envoi mail : " + ex.getMessage()));
            }
        }).start();
    }

    // ============================================================
    // CHARGEMENT
    // ============================================================
    private void chargerRendezVous() {
        try {
            List<rendez_vous> tous = new ArrayList<>(rvService.afficherTous());
            // Mes RV en premier, puis les autres
            tous.sort((a, b) -> {
                boolean aOwner = isOwner(a);
                boolean bOwner = isOwner(b);
                if (aOwner && !bOwner) return -1;
                if (!aOwner && bOwner) return 1;
                return 0;
            });
            tableRv.setItems(FXCollections.observableArrayList(tous));
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    // ============================================================
    // CRUD RENDEZ-VOUS
    // ============================================================
    @FXML
    private void ajouterRv(ActionEvent e) {
        if (!validerFormRv()) return;
        try {
            rendez_vous r = new rendez_vous();
            r.setId_admin_createur(currentUserId);
            r.setTitre(titreField.getText().trim());
            r.setDescription(getText(descriptionField));
            r.setDate_rendez_vous(readDateTime());
            r.setType(typeBox.getValue());
            r.setCompetence(competenceField.getText().trim());
            r.setDate_creation_rendez_vous(LocalDateTime.now());
            r.setNombre_places(Integer.parseInt(nombrePlacesField.getText().trim()));
            r.setMail_user(currentUserEmail);
            rvService.ajouter(r);
            chargerRendezVous();
            viderFormRv();
            showInfo("Rendez-vous ajouté ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void modifierRv(ActionEvent e) {
        rendez_vous s = tableRv.getSelectionModel().getSelectedItem();
        if (s == null || !isOwner(s)) return;
        if (!validerFormRv()) return;
        try {
            s.setTitre(titreField.getText().trim());
            s.setDescription(getText(descriptionField));
            s.setDate_rendez_vous(readDateTime());
            s.setType(typeBox.getValue());
            s.setCompetence(competenceField.getText().trim());
            s.setNombre_places(Integer.parseInt(nombrePlacesField.getText().trim()));
            rvService.modifier(s);
            chargerRendezVous();
            showInfo("Rendez-vous modifié ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void supprimerRv(ActionEvent e) {
        rendez_vous s = tableRv.getSelectionModel().getSelectedItem();
        if (s == null || !isOwner(s)) return;
        if (!confirm("Suppression", "Supprimer ce rendez-vous ?")) return;
        try {
            for (postulation p : postService.afficherParRendezVous(s.getId_rendez_vous()))
                postService.supprimer(p.getId_postulation());
            rvService.supprimer(s.getId_rendez_vous());
            tableRv.getItems().remove(s);
            viderFormRv();
            if (tableMesPost != null) tableMesPost.getItems().clear();
            showInfo("Rendez-vous supprimé ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void viderForm(ActionEvent e) {
        viderFormRv();
    }

    // ============================================================
    // POSTULATION
    // ============================================================
    @FXML
    private void handleFileChooser() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showOpenDialog((Stage) tableRv.getScene().getWindow());
        if (file != null) {
            cvField.setText(file.getName());
            cvPath = file.getAbsolutePath();
        }
    }

    @FXML
    private void postuler() {
        rendez_vous s = tableRv.getSelectionModel().getSelectedItem();
        if (s == null || isOwner(s)) return;
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || cvPath == null || cvPath.isEmpty()) {
            showWarning("Message et CV obligatoires.");
            return;
        }
        try {
            if (postService.countPostulationsForRendezVous(s.getId_rendez_vous()) >= s.getNombre_places()) {
                showWarning("Nombre maximum de postulations atteint.");
                return;
            }
            postulation p = new postulation();
            p.setId_rendez_vous(s.getId_rendez_vous());
            p.setId_postulant(currentUserId);
            p.setMail_user(currentUserEmail);
            p.setNom(Session.getCurrentUser().getNom());
            p.setPrenom(Session.getCurrentUser().getPrenom());
            p.setRole(Session.getCurrentUser().getRole());
            p.setMessage(msg);
            p.setCv(cvPath);
            p.setStatus("en_attente");
            p.setTitre(s.getTitre());
            postService.ajouter(p);
            chargerPostulationsDuRv(s.getId_rendez_vous()); // rafraîchir le tableau
            messageField.clear();
            cvField.clear();
            cvPath = "";
            showInfo("Postulation envoyée ✅");
        } catch (SQLException ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    private void handleDeletePostulation(postulation p) {
        if (confirm("Annulation", "Annuler cette postulation ?")) {
            try {
                postService.supprimer(p.getId_postulation());
                rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
                if (rv != null) chargerPostulationsDuRv(rv.getId_rendez_vous());
                showInfo("Annulée ✅");
            } catch (SQLException e) {
                showError("Erreur : " + e.getMessage());
            }
        }
    }

    private void handleDownloadCv(String cv) {
        try {
            File f = new File(cv);
            if (f.exists()) Desktop.getDesktop().open(f);
            else showError("Fichier introuvable.");
        } catch (IOException e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS FORMULAIRE
    // ============================================================
    private void remplirFormRv(rendez_vous r) {
        titreField.setText(safe(r.getTitre()));
        descriptionField.setText(r.getDescription() == null ? "" : r.getDescription());
        competenceField.setText(safe(r.getCompetence()));
        nombrePlacesField.setText(String.valueOf(r.getNombre_places()));
        if (typeBox != null) typeBox.setValue(r.getType());
        LocalDateTime dt = r.getDate_rendez_vous();
        if (dt != null) {
            datePicker.setValue(dt.toLocalDate());
            if (hourSpinner != null) hourSpinner.getValueFactory().setValue(dt.getHour());
            if (minuteSpinner != null) minuteSpinner.getValueFactory().setValue(dt.getMinute());
        } else {
            datePicker.setValue(null);
        }
    }

    private void viderFormRv() {
        if (titreField != null) titreField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (competenceField != null) competenceField.clear();
        if (nombrePlacesField != null) nombrePlacesField.clear();
        if (typeBox != null) typeBox.setValue(null);
        if (datePicker != null) datePicker.setValue(null);
        if (hourSpinner != null && hourSpinner.getValueFactory() != null) hourSpinner.getValueFactory().setValue(9);
        if (minuteSpinner != null && minuteSpinner.getValueFactory() != null)
            minuteSpinner.getValueFactory().setValue(0);
        tableRv.getSelectionModel().clearSelection();
        refreshActionButtons();
        refreshFormPostuler();
        refreshAccepterRefuserButtons();
    }

    private boolean validerFormRv() {
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            showWarning("Titre obligatoire.");
            return false;
        }
        if (typeBox.getValue() == null) {
            showWarning("Type obligatoire.");
            return false;
        }
        if (competenceField.getText() == null || competenceField.getText().trim().isEmpty()) {
            showWarning("Compétence obligatoire.");
            return false;
        }
        if (datePicker.getValue() == null) {
            showWarning("Date obligatoire.");
            return false;
        }
        try {
            if (Integer.parseInt(nombrePlacesField.getText().trim()) <= 0) {
                showWarning("Nombre de places > 0.");
                return false;
            }
        } catch (NumberFormatException ex) {
            showWarning("Nombre de places invalide.");
            return false;
        }
        if (readDateTime().isBefore(LocalDateTime.now())) {
            showWarning("Date dans le passé.");
            return false;
        }
        return true;
    }

    private LocalDateTime readDateTime() {
        return LocalDateTime.of(datePicker.getValue(),
                LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
    }

    private void refreshActionButtons() {
        rendez_vous s = tableRv == null ? null : tableRv.getSelectionModel().getSelectedItem();
        boolean owner = isOwner(s);
        if (btnModifier != null) btnModifier.setDisable(s == null || !owner);
        if (btnSupprimer != null) btnSupprimer.setDisable(s == null || !owner);
    }

    private void refreshFormPostuler() {
        rendez_vous s = tableRv == null ? null : tableRv.getSelectionModel().getSelectedItem();
        // Formulaire postuler visible seulement si RV sélectionné et je n'en suis PAS owner
        boolean show = (s != null && !isOwner(s));
        if (formPostulerBox != null) {
            formPostulerBox.setVisible(show);
            formPostulerBox.setManaged(show);
        }
    }

    /**
     * Je suis owner si l'id créateur ET l'email correspondent à mon compte.
     */
    private boolean isOwner(rendez_vous rv) {
        if (rv == null) return false;
        return rv.getId_admin_createur() == currentUserId
                && currentUserEmail.equalsIgnoreCase(safe(rv.getMail_user()));
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/profil.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());
            if (Session.getCurrentUser() != null) {
                org.controller.utilisateur.ProfilController ctrl = loader.getController();
                ctrl.setUser(Session.getCurrentUser());
            }
            Stage stage = (Stage) tableRv.getScene().getWindow();
            stage.setTitle("SkillSwap - Profil");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Erreur navigation : " + e.getMessage());
        }
    }

    @FXML
    private void compButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Competences/front/CompetenceFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1000);
            var css = getClass().getResource("/view/css/competences/front/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Compétences");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // UTILS
    // ============================================================
    private String getText(TextArea a) {
        return a == null ? "" : a.getText();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private void showWarning(String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).show();
    }

    private void showError(String m) {
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).show();
    }

    private void showInfo(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).show();
    }
}