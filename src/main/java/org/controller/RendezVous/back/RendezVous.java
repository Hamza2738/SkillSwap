    package org.controller.RendezVous.back;

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
    import javafx.scene.control.Label;
    import javafx.scene.control.TextArea;
    import javafx.scene.control.TextField;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.VBox;
    import javafx.stage.FileChooser;
    import javafx.stage.Stage;
    import org.controller.utilisateur.ProfilAdminController;
    import org.model.RendezVous.postulation;
    import org.model.RendezVous.rendez_vous;
    import org.service.RendezVous.PostulationService;
    import org.service.RendezVous.RendezVousService;
    import org.utils.Session;

    import java.awt.*;
    import java.io.File;
    import java.io.IOException;
    import java.sql.SQLException;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.Properties;

    public class RendezVous {

        // Pour LocalDateTime (date + heure)
        private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

        private static final String SMTP_USER = "skillswapskillswap@gmail.com";
        private static final String SMTP_PASS = "rhmq cwok iwsj jbru";
        private final RendezVousService rvService = new RendezVousService();
        private final PostulationService postService = new PostulationService();
        String email = "";
        // ====== TABLE RENDEZ-VOUS ======
        @FXML private TableColumn<rendez_vous, LocalDateTime> colRvDateCreation;
        @FXML private TableView<rendez_vous> tableRv;
        @FXML private TableColumn<rendez_vous, String> colRvTitre;
        @FXML private TableColumn<rendez_vous, String> colRvType;
        @FXML private TableColumn<rendez_vous, String> colRvCompetence;
        @FXML private TableColumn<rendez_vous, LocalDateTime> colRvDate;
        @FXML private TableColumn<postulation, String> colPostStatus;
        @FXML private TableColumn<postulation, String> colPostMsg;
        @FXML private TableColumn<postulation, LocalDateTime> colPostDate;
        @FXML private TableColumn<postulation, String> colPostRole;
        @FXML private TableColumn<postulation, String> colPostNom;
        @FXML private TableColumn<postulation, String> colPostPrenom;
        @FXML private TableColumn<postulation, String> colPostCv;
        @FXML private TableColumn<postulation, String> colPostDelete;
        @FXML private TableColumn<postulation, String> colPostMail;
        // ====== En haut de la classe ======
        @FXML private Button btnModifier;
        @FXML private Button btnSupprimer;

        // ====== FORM RV ======
        @FXML private TextField titreField;
        @FXML private TextArea descriptionField;
        @FXML private DatePicker datePicker;
        @FXML private Spinner<Integer> hourSpinner;
        @FXML private Spinner<Integer> minuteSpinner;
        @FXML private ComboBox<String> typeBox;
        @FXML private TextField competenceField;
        @FXML private TableColumn<rendez_vous, String> colRvMail; // Nouvelle colonne
        @FXML private ComboBox<String> filterComboBox; // ComboBox for filter selection
        @FXML private TextField searchTextField; // TextField for search input
        // ====== TABLE POSTULATIONS ======
        @FXML private TableView<postulation> tablePost;

        @FXML private VBox inputContainer;  // Assurez-vous que cet élément existe dans le fichier FXML
        @FXML private Label fieldLabel;     // Assurez-vous que cet élément existe dans le fichier FXML
        @FXML private HBox inputHbox;       // Assurez-vous que cet élément existe dans le fichier FXML
        @FXML private Label colonLabel;     // Assurez-vous que cet élément existe dans le fichier FXML
        @FXML private TextField nombrePlacesField;  // Déclaration du TextField pour le nombre de places
        @FXML
        private TableColumn<rendez_vous, String> colRvNombrePlaces;  // Déclaration de la colonne Nombre de places


        // ====== FORM POSTULER ======
        @FXML private VBox formPostulerBox;
        @FXML private TextField cvField;
        @FXML private TextArea messageField;
        @FXML private Button pdfButton;

        private String cvPath = "";
        @FXML private Button btnAccepter;
        @FXML private Button btnRefuser;
        // ✅ si tu n’as pas encore la session admin, mets une valeur fixe

        private int currentAdminId;
        private String currentUserEmail;
        @FXML
        public void initialize() {
            if (btnModifier != null) btnModifier.setDisable(true);
            if (btnSupprimer != null) btnSupprimer.setDisable(true);
            if (btnAccepter != null) btnAccepter.setDisable(true);
            if (btnRefuser  != null) btnRefuser.setDisable(true);
            // ====== Initialiser l'icône PDF du bouton ======
            if (pdfButton != null) {
                try {
                    var pdfUrl = getClass().getResourceAsStream("/view/files/RendezVous/pdf-logo.png");
                    if (pdfUrl != null) {
                        javafx.scene.image.Image pdfImage = new javafx.scene.image.Image(pdfUrl);
                        javafx.scene.image.ImageView pdfImageView = new javafx.scene.image.ImageView(pdfImage);
                        pdfImageView.setFitHeight(24);
                        pdfImageView.setFitWidth(24);
                        pdfImageView.setPreserveRatio(true);
                        pdfButton.setGraphic(pdfImageView);
                        pdfButton.setText(""); // ✅ pas de texte, juste l'icône
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("⚠️ Image PDF introuvable : " + ex.getMessage());
                }
            }

            if (Session.getCurrentUser() != null) {
                currentAdminId = Session.getCurrentUser().getId_utilisateur();
                currentUserEmail = Session.getCurrentUser().getEmail();
                email = currentUserEmail;
            } else {
                email = "Utilisateur non connecté";
                showWarning("Aucun utilisateur connecté.");
                return;
            }
            // Colonnes pour afficher les informations de la postulation

            if (colPostMail != null) {
                colPostMail.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getMail_user())
                ));}

            if (colPostStatus != null) {
                colPostStatus.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getStatus())
                ));
            }

            if (colPostMsg != null) {
                colPostMsg.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getMessage())
                ));
            }

            if (colPostDate != null) {
                colPostDate.setCellValueFactory(d -> new SimpleObjectProperty<>(
                        d.getValue() == null ? null : d.getValue().getDate_postulation()
                ));
            }

            if (colPostRole != null) {
                colPostRole.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getRole())
                ));
            }

            if (colPostNom != null) {
                colPostNom.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getNom())
                ));
            }

            if (colPostPrenom != null) {
                colPostPrenom.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getPrenom())
                ));
            }

            // ====== Combos / spinners ======
            if (typeBox != null) {
                typeBox.setItems(FXCollections.observableArrayList("presentiel", "distance"));
            }
            if (colRvMail != null) {
                colRvMail.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getMail_user())  // ✅ mail du créateur
                ));
            }
            if (hourSpinner != null) {
                hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
                hourSpinner.setEditable(true);
            }
            if (minuteSpinner != null) {
                minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
                minuteSpinner.setEditable(true);
            }


            // ====== Colonnes RV ======
            if (colRvTitre != null) {
                colRvTitre.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getTitre())
                ));
            }
            if (colPostCv != null) {
                colPostCv.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getCv())
                ));

                // Cellule pour la colonne CV avec un bouton pour télécharger
                colPostCv.setCellFactory(param -> new TableCell<postulation, String>() {
                    @Override
                    protected void updateItem(String cv, boolean empty) {
                        super.updateItem(cv, empty);
                        if (!empty && cv != null) {
                            Button btn = new Button("Télécharger CV");
                            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c8cff;");
                            btn.setOnAction(event -> handleDownloadCv(cv));  // Action pour télécharger le CV
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    }
                });
            }
            if (colRvNombrePlaces != null) {
                colRvNombrePlaces.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : String.valueOf(d.getValue().getNombre_places())
                ));
            }
            if (colRvDateCreation != null) {
                colRvDateCreation.setCellValueFactory(d ->
                        new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getDate_creation_rendez_vous())
                );
                colRvDateCreation.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(LocalDateTime dt, boolean empty) {
                        super.updateItem(dt, empty);
                        setText(empty || dt == null ? "" : dt.format(DT_FMT));
                    }
                });
            }
            if (colRvType != null) {
                colRvType.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getType())
                ));
            }
            if (colRvCompetence != null) {
                colRvCompetence.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getCompetence())
                ));
            }
            if (colRvDate != null) {
                colRvDate.setCellValueFactory(d ->
                        new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getDate_rendez_vous())
                );
                colRvDate.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(LocalDateTime dt, boolean empty) {
                        super.updateItem(dt, empty);
                        setText(empty || dt == null ? "" : dt.format(DT_FMT));
                    }
                });
            }

            // ====== Colonnes Postulations ======
            if (colPostStatus != null) {
                colPostStatus.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getStatus())
                ));
            }

            if (colPostMsg != null) {
                colPostMsg.setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue() == null ? "" : safe(d.getValue().getMessage())
                ));
            }
            if (colPostDate != null) {
                colPostDate.setCellValueFactory(d ->
                        new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getDate_postulation())
                );
                colPostDate.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(LocalDateTime dt, boolean empty) {
                        super.updateItem(dt, empty);
                        setText(empty || dt == null ? "" : dt.format(DT_FMT));
                    }
                });
            }
            // ====== Selection RV -> charger postulations + remplir form ======
            // ====== Selection RV -> charger postulations + remplir form ======
            if (tableRv != null) {
                tableRv.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        remplirFormRv(newV);
                        chargerPostulations(newV.getId_rendez_vous());
                    } else {
                        if (tablePost != null) tablePost.getItems().clear();
                        viderFormRv();
                    }
                    refreshActionButtons();
                    refreshFormPostuler();
                    refreshAccepterRefuserButtons();
                });
            }
            // Ajouter la colonne de suppression
            if (colPostDelete != null) {
                colPostDelete.setCellValueFactory(d -> new SimpleStringProperty("Supprimer"));

                colPostDelete.setCellFactory(param -> new TableCell<postulation, String>() {
                    private final Button deleteButton = new Button("Annulation");

                    {
                        // Appliquer le style bleu au bouton
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c8cff;");

                        // Lors du clic sur le bouton, supprimer la postulation
                        deleteButton.setOnAction(event -> {
                            postulation postToDelete = getTableRow().getItem();
                            if (postToDelete != null) {
                                handleDeletePostulation(postToDelete);  // Appel de la méthode pour supprimer la postulation
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setGraphic(deleteButton);  // Afficher le bouton de suppression dans la cellule
                        } else {
                            setGraphic(null);  // Ne rien afficher si la cellule est vide
                        }
                    }
                });
                filterComboBox.setItems(FXCollections.observableArrayList("Tous", "Titre", "Compétence", "Type"));
                filterComboBox.setValue("Tous");
            }


            // ====== Chargement initial ======
            chargerRendezVous();
        }
        private void applyStyles() {
            // Appliquer des couleurs de fond avec dégradé et de bordure à VBox
            inputContainer.setStyle("-fx-background-color: linear-gradient(to right, #5aa6ff, #7b61ff); -fx-border-color: rgba(255,255,255,0.10); -fx-border-width: 1; -fx-border-radius: 12;");

            // Appliquer des styles aux labels de champs (en blanc et en gras)
            fieldLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 700;");

            // Appliquer des couleurs de fond avec dégradé aux Spinners (ajouter un style direct)
            hourSpinner.setStyle("-fx-background-color: linear-gradient(to right, #5aa6ff, #7b61ff); -fx-text-fill: white; -fx-border-radius: 12;");
            minuteSpinner.setStyle("-fx-background-color: linear-gradient(to right, #5aa6ff, #7b61ff); -fx-text-fill: white; -fx-border-radius: 12;");

            // Appliquer des styles avec dégradé à HBox
            inputHbox.setStyle("-fx-background-color: linear-gradient(to right, #5aa6ff, #7b61ff); -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 12;");

            // Appliquer des couleurs au label des séparateurs (en blanc et gras)
            colonLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 700;");
        }


        @FXML
        private void onSearchKeyReleased(javafx.scene.input.KeyEvent event) throws SQLException {
            String searchText = searchTextField.getText().toLowerCase();
            String selectedFilter = filterComboBox.getValue();

            if (selectedFilter.equals("Tous")) {
                filterAllColumns(searchText);
            } else if (selectedFilter.equals("Titre")) {
                filterColumnByTitle(searchText);
            } else if (selectedFilter.equals("Compétence")) {
                filterColumnByCompetence(searchText);
            } else if (selectedFilter.equals("Type")) {
                filterColumnByType(searchText);
            }
        }
        private void refreshAccepterRefuserButtons() {
            rendez_vous rv = tableRv  == null ? null : tableRv.getSelectionModel().getSelectedItem();
            postulation p  = tablePost == null ? null : tablePost.getSelectionModel().getSelectedItem();

            boolean peutGerer = (rv != null) && isOwner(rv) && (p != null);

            if (btnAccepter != null) btnAccepter.setDisable(!peutGerer);
            if (btnRefuser  != null) btnRefuser.setDisable(!peutGerer);
        }
        // Method to filter all columns
        private void filterAllColumns(String searchText) throws SQLException {
            FilteredList<rendez_vous> filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(rvService.afficherTous()), p -> true);  // ✅
            filteredData.setPredicate(rv ->
                    rv.getTitre().toLowerCase().contains(searchText) ||
                            rv.getCompetence().toLowerCase().contains(searchText) ||
                            rv.getType().toLowerCase().contains(searchText));
            tableRv.setItems(filteredData);
        }

        private void filterColumnByTitle(String searchText) throws SQLException {
            FilteredList<rendez_vous> filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(rvService.afficherTous()), p -> true);  // ✅
            filteredData.setPredicate(rv -> rv.getTitre().toLowerCase().contains(searchText));
            tableRv.setItems(filteredData);
        }

        private void filterColumnByCompetence(String searchText) throws SQLException {
            FilteredList<rendez_vous> filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(rvService.afficherTous()), p -> true);  // ✅
            filteredData.setPredicate(rv -> rv.getCompetence().toLowerCase().contains(searchText));
            tableRv.setItems(filteredData);
        }

        private void filterColumnByType(String searchText) throws SQLException {
            FilteredList<rendez_vous> filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(rvService.afficherTous()), p -> true);  // ✅
            filteredData.setPredicate(rv -> rv.getType().toLowerCase().contains(searchText));
            tableRv.setItems(filteredData);
        }

        // =======================
        // LOAD
        // =======================
        private void chargerRendezVous() {
            try {
                List<rendez_vous> tous = new ArrayList<>(rvService.afficherTous());
                tous.sort((a, b) -> {
                    boolean aOwner = isOwner(a);
                    boolean bOwner = isOwner(b);
                    if (aOwner && !bOwner) return -1;
                    if (!aOwner && bOwner) return 1;
                    return 0;
                });
                tableRv.setItems(FXCollections.observableArrayList(tous));
            } catch (SQLException e) {
                showError("Erreur chargement rendez-vous : " + e.getMessage());
            }
        }

        private void chargerPostulations(int idRv) {
            try {
                // Charger les postulations associées à ce rendez-vous
                tablePost.setItems(FXCollections.observableArrayList(
                        postService.afficherParRendezVous(idRv)
                ));
            } catch (SQLException e) {
                showError("Erreur chargement postulations : " + e.getMessage());
            }
        }



        // =======================
        // RV CRUD (liés au FXML)
        // =======================
        private void handleDeletePostulation(postulation postToDelete) {
            if (confirm("Suppression", "Voulez-vous vraiment supprimer cette postulation ?")) {
                try {
                    // Supprimer la postulation de la base de données
                    postService.supprimer(postToDelete.getId_postulation());

                    // Recharger les postulations associées au rendez-vous après suppression
                    rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
                    if (rv != null) chargerPostulations(rv.getId_rendez_vous());

                    showInfo("Postulation supprimée ✅");
                } catch (SQLException e) {
                    showError("Erreur lors de la suppression : " + e.getMessage());
                }
            }
        }


        @FXML
        private void ajouterRv(ActionEvent e) {
            if (!validerFormRv()) return;

            try {
                // Création d'un nouvel objet rendez_vous
                rendez_vous r = new rendez_vous();

                // Assignation des autres attributs
                r.setId_admin_createur(currentAdminId);
                r.setTitre(titreField.getText().trim());
                r.setDescription(getText(descriptionField));
                r.setDate_rendez_vous(readDateTime());
                r.setType(typeBox.getValue());
                r.setCompetence(competenceField.getText().trim());
                r.setDate_creation_rendez_vous(LocalDateTime.now());

                // Récupérer le nombre de places et l'assigner
                r.setNombre_places(Integer.parseInt(nombrePlacesField.getText()));  // Récupérer le nombre de places

                // Assigner l'email de l'utilisateur connecté (si nécessaire)
                r.setMail_user(Session.getCurrentUser() != null ? Session.getCurrentUser().getEmail() : null);

                // Ajout du rendez-vous via le service
                rvService.ajouter(r);

                // Recharger la liste des rendez-vous et vider le formulaire
                chargerRendezVous();
                viderFormRv();
                showInfo("Rendez-vous ajouté ✅");

            } catch (SQLException ex) {
                showError("Erreur ajout : " + ex.getMessage());
            }
        }



        @FXML
        private void modifierRv(ActionEvent e) {
            rendez_vous selected = tableRv.getSelectionModel().getSelectedItem();
            if (selected == null || !isOwner(selected)) return; // ✅ silencieux
            if (!validerFormRv()) return;
            try {
                selected.setTitre(titreField.getText().trim());
                selected.setDescription(getText(descriptionField));
                selected.setDate_rendez_vous(readDateTime());
                selected.setType(typeBox.getValue());
                selected.setCompetence(competenceField.getText().trim());
                selected.setNombre_places(Integer.parseInt(nombrePlacesField.getText()));
                rvService.modifier(selected);
                chargerRendezVous();
                showInfo("Rendez-vous modifié ✅");
            } catch (SQLException ex) {
                showError("Erreur modification : " + ex.getMessage());
            }
        }

        @FXML
        private void supprimerRv(ActionEvent e) {
            rendez_vous selected = tableRv.getSelectionModel().getSelectedItem();
            if (selected == null || !isOwner(selected)) return;
            if (!confirm("Suppression", "Supprimer ce rendez-vous ?")) return;
            try {
                if (tablePost != null) {
                    for (postulation p : tablePost.getItems()) {
                        postService.supprimer(p.getId_postulation());
                    }
                }
                rvService.supprimer(selected.getId_rendez_vous());
                tableRv.getItems().remove(selected);
                if (tablePost != null) tablePost.getItems().clear();
                viderFormRv();
                showInfo("Rendez-vous supprimé ✅");
            } catch (SQLException ex) {
                showError("Erreur suppression : " + ex.getMessage());
            }
        }

        @FXML
        private void accepterPostulation(ActionEvent e) {
            rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
            if (rv == null || !isOwner(rv)) return; // ✅ silencieux
            postulation p = tablePost.getSelectionModel().getSelectedItem();
            if (p == null) return;
            if (!confirm("Accepter", "Accepter cette postulation ?")) return;
            try {
                postService.changerStatus(p.getId_postulation(), "accepte");
                if (p.getMail_user() != null) {
                    envoyerMailAcceptation(p.getMail_user(), p.getNom(), rv.getTitre(), rv.getType());
                }
                chargerPostulations(rv.getId_rendez_vous());
                showInfo("Postulation acceptée et email envoyé ✅");
            } catch (SQLException ex) {
                showError("Erreur acceptation : " + ex.getMessage());
            }
        }

        @FXML
        private void refuserPostulation(ActionEvent e) {
            rendez_vous rv = tableRv.getSelectionModel().getSelectedItem();
            if (rv == null || !isOwner(rv)) return; // ✅ silencieux
            postulation p = tablePost.getSelectionModel().getSelectedItem();
            if (p == null) return;
            if (!confirm("Refuser", "Refuser cette postulation ?")) return;
            try {
                postService.changerStatus(p.getId_postulation(), "refuse");
                chargerPostulations(rv.getId_rendez_vous());
                showInfo("Postulation refusée ✅");
            } catch (SQLException ex) {
                showError("Erreur refus : " + ex.getMessage());
            }
        }
        @FXML
        private void viderForm(ActionEvent e) {
            viderFormRv();
        }

        // =======================
        // POSTULATION : accepter / refuser (liés au FXML)
        // =======================

        @FXML
        private void goProfil(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/fxml/utilisateur/ProfilAdmin.fxml")
                );

                Scene scene = new Scene(loader.load(), 1920, 1000);

                var cssUrl = getClass().getResource("/view/css/utilisateur/style.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

                ProfilAdminController controller = loader.getController();
                if (Session.getCurrentUser() != null) {
                    controller.setUser(Session.getCurrentUser());
                }

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("SkillSwap - Profil Admin");
                stage.setScene(scene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @FXML
        private void compButton(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/fxml/Competences/back/competence.fxml")
                );
                Parent root = loader.load();
                Scene scene = new Scene(root, 1920, 1000);

                var cssUrl = getClass().getResource("/view/css/competences/back/style.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Gestion des Compétences");
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Méthode pour télécharger un CV
        private void handleDownloadCv(String cvFileName) {
            if (cvFileName == null || cvFileName.isEmpty()) {
                showWarning("Aucun fichier sélectionné.");
                return;
            }

            try {
                // Le chemin complet du fichier doit être stocké dans la base de données
                // Assurez-vous que le chemin est correct et complet
                File pdfFile = new File(cvFileName);  // cvFileName est le chemin absolu récupéré de la base de données

                if (pdfFile.exists()) {
                    // Ouvrir le fichier PDF dans le navigateur ou télécharger
                    Desktop.getDesktop().open(pdfFile);  // Cette commande ouvre le fichier PDF dans l'application par défaut
                } else {
                    showError("Fichier PDF introuvable.");
                }
            } catch (IOException e) {
                showError("Erreur lors de l'ouverture du fichier PDF : " + e.getMessage());

            }
        }

        // =======================
        // Helpers form
        // =======================
        @FXML
        private void remplirFormRv(rendez_vous r) {
            if (r == null) return;

            // Remplir les champs du formulaire de rendez-vous
            titreField.setText(safe(r.getTitre()));
            descriptionField.setText(r.getDescription() == null ? "" : r.getDescription());
            competenceField.setText(safe(r.getCompetence()));
            if (typeBox != null) typeBox.setValue(r.getType());

            // Charger la date et l'heure du rendez-vous
            LocalDateTime dt = r.getDate_rendez_vous();
            if (dt != null) {
                datePicker.setValue(dt.toLocalDate());
                if (hourSpinner != null) hourSpinner.getValueFactory().setValue(dt.getHour());
                if (minuteSpinner != null) minuteSpinner.getValueFactory().setValue(dt.getMinute());
            } else {
                datePicker.setValue(null);
            }

            // Charger les postulations associées au rendez-vous sélectionné
            chargerPostulations(r.getId_rendez_vous());
        }

        private void viderFormRv() {
            if (titreField != null) titreField.clear();
            if (descriptionField != null) descriptionField.clear();
            if (competenceField != null) competenceField.clear();
            if (typeBox != null) typeBox.setValue(null);
            if (datePicker != null) datePicker.setValue(null);

            if (hourSpinner != null && hourSpinner.getValueFactory() != null) hourSpinner.getValueFactory().setValue(9);
            if (minuteSpinner != null && minuteSpinner.getValueFactory() != null) minuteSpinner.getValueFactory().setValue(0);
        }

        private boolean validerFormRv() {

            // Vérification du titre
            String titre = titreField.getText() == null ? "" : titreField.getText().trim();
            if (titre.isEmpty()) {
                showWarning("Le titre est obligatoire.");
                return false;
            }

            // Vérification du type
            if (typeBox.getValue() == null) {
                showWarning("Choisis un type (présentiel/distance).");
                return false;
            }

            // Vérification de la compétence
            String comp = competenceField.getText() == null ? "" : competenceField.getText().trim();
            if (comp.isEmpty()) {
                showWarning("La compétence est obligatoire.");
                return false;
            }

            // Vérification de la date
            if (datePicker.getValue() == null) {
                showWarning("La date est obligatoire.");
                return false;
            }

            // Vérification de l'heure
            LocalDateTime dt = readDateTime();
            if (dt.isBefore(LocalDateTime.now())) {
                showWarning("La date du rendez-vous ne peut pas être dans le passé.");
                return false;
            }

            // Si tout est valide
            return true;
        }

        private LocalDateTime readDateTime() {
            LocalDate d = datePicker.getValue();
            int h = hourSpinner.getValue();
            int m = minuteSpinner.getValue();
            return LocalDateTime.of(d, LocalTime.of(h, m));
        }

        private String getText(TextArea a) { return a == null ? "" : a.getText(); }
        private String safe(String s) { return s == null ? "" : s; }

        // =======================
        // Alerts
        // =======================
        private boolean confirm(String title, String msg) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            Optional<ButtonType> res = a.showAndWait();
            return res.isPresent() && res.get() == ButtonType.OK;
        }

        private void showWarning(String m) { new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).show(); }
        private void showError(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).show(); }
        private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).show(); }



        private void envoyerMailAcceptation(String destinataire, String nom, String titreRv, String typeRv) {

            String sujet = "✅ Postulation acceptée - " + titreRv;
            String corps;

            if ("distance".equalsIgnoreCase(typeRv)) {
                corps = "<html><body style='font-family:Arial,sans-serif;'>"
                        + "<h2 style='color:#4CAF50;'>Félicitations " + nom + " ! 🎉</h2>"
                        + "<p>Votre postulation pour le rendez-vous : <strong>\"" + titreRv + "\"</strong> a été acceptée.</p>"
                        + "<p>Entrez le lien Meet : "
                        + "<a href='https://meet.google.com/tgi-yomj-ngb' style='color:#6c8cff;'>"
                        + "https://meet.google.com/tgi-yomj-ngb</a></p>"
                        + "<br><p style='color:#888;'>— L'équipe SkillSwap</p>"
                        + "</body></html>";
            } else {
                corps = "<html><body style='font-family:Arial,sans-serif; max-width:650px; margin:auto;'>"
                        + "<div style='background:#1a1a2e; padding:30px; border-radius:16px;'>"
                        + "<h2 style='color:#4CAF50;'>Félicitations " + nom + " ! 🎉</h2>"
                        + "<p style='color:#ffffff;'>Votre postulation pour le rendez-vous : <strong>\""
                        + titreRv + "\"</strong> a été acceptée.</p>"
                        + "<p style='color:#ffffff;'>📍 Vous pouvez voir la localisation ici :</p>"
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
                    props.put("mail.smtp.auth",            "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host",            "smtp.gmail.com");
                    props.put("mail.smtp.port",            "587");

                    jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props,
                            new jakarta.mail.Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                                }
                            });

                    Message message = new MimeMessage(mailSession);
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
        private boolean isOwner(rendez_vous rv) {
            if (rv == null) return false;
            return rv.getId_admin_createur() == currentAdminId;
        }

        // ====== refreshActionButtons — identique à TacheFrontController ======
        private void refreshActionButtons() {
            rendez_vous selected = tableRv == null ? null
                    : tableRv.getSelectionModel().getSelectedItem();
            boolean owner = isOwner(selected);

            if (btnModifier  != null) btnModifier.setDisable(selected == null || !owner);
            if (btnSupprimer != null) btnSupprimer.setDisable(selected == null || !owner);
            // ✅ Ne pas toucher btnAccepter/btnRefuser ici — géré par refreshAccepterRefuserButtons()
        }
        private void refreshFormPostuler() {
            rendez_vous selected = tableRv == null ? null
                    : tableRv.getSelectionModel().getSelectedItem();

            // ✅ Afficher le formulaire seulement si RV sélectionné ET pas à moi
            boolean showForm = (selected != null && !isOwner(selected));

            if (formPostulerBox != null) {
                formPostulerBox.setVisible(showForm);
                formPostulerBox.setManaged(showForm);
            }
        }
        @FXML
        private void handleFileChooser() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            Stage stage = (Stage) tableRv.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                cvField.setText(selectedFile.getName());
                cvPath = selectedFile.getAbsolutePath();
            }
        }

        @FXML
        private void postuler() {
            rendez_vous selected = tableRv.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            // ✅ Sécurité : on ne peut pas postuler à son propre RV
            if (isOwner(selected)) return;

            String msg = messageField.getText().trim();
            if (msg.isEmpty() || cvPath == null || cvPath.isEmpty()) {
                showWarning("Message et CV obligatoires.");
                return;
            }

            // Vérifier le nombre de places
            try {
                int nombrePostulations = postService.countPostulationsForRendezVous(
                        selected.getId_rendez_vous());
                if (nombrePostulations >= selected.getNombre_places()) {
                    showWarning("Le nombre maximum de postulations a été atteint.");
                    return;
                }

                postulation p = new postulation();
                p.setId_rendez_vous(selected.getId_rendez_vous());
                p.setId_postulant(currentAdminId);
                p.setMail_user(currentUserEmail);
                p.setNom(Session.getCurrentUser().getNom());
                p.setPrenom(Session.getCurrentUser().getPrenom());
                p.setRole(Session.getCurrentUser().getRole());
                p.setMessage(msg);
                p.setCv(cvPath);
                p.setStatus("en_attente");
                p.setTitre(selected.getTitre());

                postService.ajouter(p);

                // Recharger et vider le formulaire
                chargerPostulations(selected.getId_rendez_vous());
                messageField.clear();
                cvField.clear();
                cvPath = "";

                showInfo("Postulation envoyée ✅");

            } catch (SQLException ex) {
                showError("Erreur postulation : " + ex.getMessage());
            }
        }
    }