    package org.controller.Competences.back;

    import com.itextpdf.io.image.ImageData;
    import com.itextpdf.io.image.ImageDataFactory;
    import com.itextpdf.kernel.colors.ColorConstants;
    import com.itextpdf.kernel.geom.Rectangle;
    import com.itextpdf.kernel.pdf.PdfDocument;
    import com.itextpdf.kernel.pdf.PdfWriter;
    import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
    import com.itextpdf.layout.Document;
    import com.itextpdf.layout.borders.Border;
    import com.itextpdf.layout.element.Image;
    import com.itextpdf.layout.element.Paragraph;
    import com.itextpdf.layout.element.Table;
    import com.itextpdf.layout.properties.HorizontalAlignment;
    import com.itextpdf.layout.properties.TextAlignment;
    import com.itextpdf.layout.properties.UnitValue;
    import javafx.application.Platform;
    import javafx.beans.property.ReadOnlyObjectWrapper;
    import javafx.beans.property.SimpleStringProperty;
    import javafx.collections.FXCollections;
    import javafx.collections.transformation.FilteredList;
    import javafx.collections.transformation.SortedList;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.image.ImageView;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.*;
    import javafx.scene.media.AudioClip;
    import javafx.scene.media.MediaPlayer;
    import javafx.stage.FileChooser;
    import javafx.stage.Modality;
    import javafx.stage.Stage;
    import javafx.util.converter.IntegerStringConverter;
    import org.controller.utilisateur.ProfilAdminController;
    import org.model.Competences.Competence;
    import org.service.Competences.CompetenceService;
    import org.service.Competences.mailing.MailService;
    import org.utils.Session;

    import java.io.ByteArrayOutputStream;
    import java.io.File;
    import java.io.InputStream;
    import java.net.URL;
    import java.text.Normalizer;
    import java.time.DayOfWeek;
    import java.time.LocalDate;
    import java.time.Month;
    import java.time.format.DateTimeFormatter;
    import java.time.format.TextStyle;
    import java.util.Locale;
    import java.util.Optional;
    import java.util.Set;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.function.UnaryOperator;
    import java.util.regex.Pattern;

    public class CompetenceController {


        @FXML private BorderPane root;

        @FXML private TableView<Competence> table;
        @FXML private TableColumn<Competence, Number> colId;
        @FXML private TableColumn<Competence, String> colCategory;
        @FXML private TableColumn<Competence, String> colType;
        @FXML private TableColumn<Competence, String> colDescription;

        @FXML private TableColumn<Competence, String> colNiveau;
        @FXML private TableColumn<Competence, Number> colAnnees;
        @FXML private TableColumn<Competence, String> colCertification;
        @FXML private TableColumn<Competence, String> colStatut;

        @FXML private TableColumn<Competence, String> colEmail;

        @FXML private ComboBox<String> cbTypeFilter;
        @FXML private TextField tfSearchValue;

        @FXML private TextField tfCategory;
        @FXML private TextField tfType;
        @FXML private TextArea taDescription;

        @FXML private ComboBox<String> cbNiveau;
        @FXML private TextField tfAnnees;
        @FXML private TextField tfCertification;
        @FXML private ComboBox<String> cbStatut;

        @FXML private TextField tfEmail;

        @FXML private Label lblError;
        @FXML private Button btnCalendar;

        @FXML private Button btnMusicToggle;
        @FXML private Slider slVolume;
        @FXML private ImageView imgMusicIcon;


        // =========================
        // INIT
        // =========================


        @FXML
        public void initialize() {
            initClickSound();
            Platform.runLater(this::installGlobalClickSound);

            setupInputValidation();
            setupExtraFields();

            setupTable();
            setupSearchUI();

            refresh();

           /* playMusic();
            setupMusicControls();
            updateMusicUi();*/
        }



        // =========================
        // DATA / SERVICES
        // =========================
        private final CompetenceService service = new CompetenceService();
        private final javafx.collections.ObservableList<Competence> data = FXCollections.observableArrayList();
        private FilteredList<Competence> filteredData;

        private final MailService mailService = new MailService();

        //  pool mail
        private final ExecutorService mailExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "mail-sender");
            t.setDaemon(true);
            return t;
        });

        //  évite double-clic / double envoi
        private final Set<String> mailLocks = ConcurrentHashMap.newKeySet();

        // =========================
        // SEARCH ATTRIBUTES
        // =========================
        private static final String ATTR_ALL = "Tous";
        private static final String ATTR_NIVEAU = "Niveau";
        private static final String ATTR_TYPE = "Type";
        private static final String ATTR_CATEGORY = "Catégorie";
        private static final String ATTR_CERTIF = "Certification";
        private static final String ATTR_ANNEES = "Expérience";
        private static final String ATTR_STATUT = "Statut";
        private static final String ATTR_DESC = "Description";
        private static final String ATTR_EMAIL = "Email";

        // =========================
        // AUDIO
        // =========================
        private MediaPlayer mediaPlayer;

        private AudioClip clickClip;
        private long lastClickSoundMs = 0;
        private static final long CLICK_COOLDOWN_MS = 35;

        // =========================
        // VALIDATION
        // =========================
        private static final Pattern P_SIMPLE =Pattern.compile("^[\\p{L}0-9 _-]*$");

        private static final Pattern P_EMAIL  = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final Locale LOCALE_FR = Locale.FRENCH;
        private static final String CSS_PATH = "/view/css/competences/back/style.css";

        private static final String[] NIVEAUX = {"Débutant", "Intermédiaire", "Avancé", "Expert"};
        private static final String[] STATUTS = {"Validée", "En cours", "Expirée"};

        private static final int ANNEES_MIN = 0;
        private static final int ANNEES_MAX = 60;

        // =========================
        // CALENDAR STATE
        // =========================
        private LocalDate calendarMonth = LocalDate.now().withDayOfMonth(1);
        private LocalDate selectedDate  = LocalDate.now();

        // =========================
        //  Search UI
        // =========================
        private void setupSearchUI() {
            if (cbTypeFilter != null) {
                cbTypeFilter.setItems(FXCollections.observableArrayList(
                        ATTR_ALL, ATTR_EMAIL, ATTR_NIVEAU, ATTR_TYPE, ATTR_CATEGORY,
                        ATTR_CERTIF, ATTR_ANNEES, ATTR_STATUT, ATTR_DESC
                ));
                cbTypeFilter.setValue(ATTR_ALL);
                cbTypeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
            }

            if (tfSearchValue != null) {
                tfSearchValue.textProperty().addListener((obs, o, n) -> applyFilters());
            }
        }

        private void setupExtraFields() {
            if (cbNiveau != null) {
                cbNiveau.setItems(FXCollections.observableArrayList(NIVEAUX));
                if (cbNiveau.getValue() == null) cbNiveau.setValue("Débutant");
            }

            if (cbStatut != null) {
                cbStatut.setItems(FXCollections.observableArrayList(STATUTS));
                if (cbStatut.getValue() == null) cbStatut.setValue("En cours");
            }

            if (tfAnnees != null) {
                if (tfAnnees.getText() == null || tfAnnees.getText().trim().isEmpty()) {
                    tfAnnees.setText("0");
                }

                UnaryOperator<TextFormatter.Change> intFilter = change -> {
                    String newText = change.getControlNewText();
                    if (newText.isEmpty()) return change;
                    if (!newText.matches("\\d+")) return null;
                    if (newText.length() > 2) return null;

                    try {
                        int v = Integer.parseInt(newText);
                        if (v < ANNEES_MIN || v > ANNEES_MAX) return null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    return change;
                };

                TextFormatter<Integer> formatter = new TextFormatter<>(
                        new IntegerStringConverter(),
                        0,
                        intFilter
                );

                tfAnnees.setTextFormatter(formatter);

                tfAnnees.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        String s = nvl(tfAnnees.getText()).trim();
                        if (s.isEmpty()) tfAnnees.setText("0");
                    }
                });
            }
        }

        // =========================
        //  EMAIL (Validée -> sélection, Expirée -> expiration)
        // =========================
        private void sendStatusMailAsync(Competence c) {
            if (c == null) return;

            String statutRaw = nvl(c.getStatut()).trim();
            String statutNorm = normalize(statutRaw);

            boolean isValidee = statutNorm.equals("valide") || statutNorm.equals("validee");
            boolean isExpiree = statutNorm.equals("expiree") || statutNorm.equals("expire");

            if (!isValidee && !isExpiree) {
                setError("⚠️ Aucun mail envoyé : statut = '" + statutRaw + "'. (Seuls Validée / Expirée)");
                return;
            }

            String email = nvl(c.getEmail()).trim();
            if (email.isEmpty() || !P_EMAIL.matcher(email).matches()) {
                setError("Email invalide dans la ligne.");
                return;
            }

            String teamType = nvl(c.getType()).trim();
            if (teamType.isEmpty()) teamType = "votre domaine";

            //  anti double-clic / double envoi
            String lockKey = c.getId() + "|" + statutNorm + "|" + email;
            if (!mailLocks.add(lockKey)) {
                setError("Email déjà en cours d’envoi...");
                return;
            }

            final String emailFinal = email;
            final String teamTypeFinal = teamType;

            mailExecutor.submit(() -> {
                try {
                    if (isValidee) {
                        mailService.sendSelectionEmail(emailFinal, teamTypeFinal);
                        Platform.runLater(() -> setError(" Email de sélection envoyé à " + emailFinal));
                    } else {
                        mailService.sendExpiredEmail(emailFinal, teamTypeFinal);
                        Platform.runLater(() -> setError(" Email d’expiration envoyé à " + emailFinal));
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> setError(" Erreur email: " + ex.getMessage()));
                    ex.printStackTrace();
                } finally {
                    mailLocks.remove(lockKey);
                }
            });
        }

        private void setupTable() {
            colCategory.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getCategory())));
            colType.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getType())));
            colDescription.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getDescription())));

            if (colNiveau != null) colNiveau.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getNiveau())));
            if (colAnnees != null) colAnnees.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getAnneesExperience()));
            if (colCertification != null) colCertification.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getCertification())));

            if (colStatut != null) {
                colStatut.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getStatut())));

                //  version SAFE (ne dépend pas de getIndex())
                colStatut.setCellFactory(col -> new TableCell<>() {
                    private final Hyperlink link = new Hyperlink();

                    {
                        link.setOnAction(e -> {
                            Competence rowItem = getTableRow() != null ? (Competence) getTableRow().getItem() : null;
                            if (rowItem != null) sendStatusMailAsync(rowItem);
                        });
                    }

                    @Override
                    protected void updateItem(String statut, boolean empty) {
                        super.updateItem(statut, empty);

                        Competence rowItem = getTableRow() != null ? (Competence) getTableRow().getItem() : null;

                        if (empty || statut == null || rowItem == null) {
                            setGraphic(null);
                            setText(null);
                            return;
                        }

                        String s = statut.trim();
                        link.setText(s);

                        String norm = normalize(s);
                        boolean clickable =
                                norm.equals("valide") || norm.equals("validee") ||
                                        norm.equals("expiree") || norm.equals("expire");

                        link.setDisable(!clickable);
                        link.setOpacity(clickable ? 1.0 : 0.5);

                        setGraphic(link);
                        setText(null);
                    }
                });
            }

            if (colEmail != null) colEmail.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getEmail())));

            colId.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(table.getItems().indexOf(cellData.getValue()) + 1)
            );

            filteredData = new FilteredList<>(data, p -> true);
            SortedList<Competence> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(table.comparatorProperty());
            table.setItems(sortedData);

            table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                setError("");
                if (newV != null) {
                    if (tfCategory != null) tfCategory.setText(nvl(newV.getCategory()));
                    if (tfType != null) tfType.setText(nvl(newV.getType()));
                    if (taDescription != null) taDescription.setText(nvl(newV.getDescription()));

                    if (cbNiveau != null) cbNiveau.setValue(normalizeComboValue(cbNiveau, nvl(newV.getNiveau()), "Débutant"));
                    if (tfAnnees != null) tfAnnees.setText(String.valueOf(newV.getAnneesExperience()));
                    if (tfCertification != null) tfCertification.setText(nvl(newV.getCertification()));
                    if (cbStatut != null) cbStatut.setValue(normalizeComboValue(cbStatut, nvl(newV.getStatut()), "En cours"));

                    if (tfEmail != null) tfEmail.setText(nvl(newV.getEmail()));
                }
            });

            if (btnCalendar != null) {
                btnCalendar.setTooltip(new Tooltip("Aujourd’hui : " + LocalDate.now().format(DATE_FMT)));
            }
        }

        // =========================
        // DATA
        // =========================
        private void refresh() {
            try {
                data.setAll(service.getAll());
                applyFilters();
            } catch (Exception e) {
                setError("Erreur chargement: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // =========================
        //  FILTERS
        // =========================
        private void applyFilters() {
            if (filteredData == null) return;

            String attr = nvl(cbTypeFilter != null ? cbTypeFilter.getValue() : ATTR_ALL).trim();
            String qRaw = nvl(tfSearchValue != null ? tfSearchValue.getText() : "").trim();
            String q = qRaw.toLowerCase(Locale.ROOT);

            filteredData.setPredicate(c -> {
                if (c == null) return false;
                if (q.isEmpty()) return true;

                java.util.function.Function<String, Boolean> contains =
                        s -> nvl(s).toLowerCase(Locale.ROOT).contains(q);

                int annees = c.getAnneesExperience();      //  numérique
                String anneesStr = String.valueOf(annees); // si tu en as besoin ailleurs

                int visualId = table.getItems().indexOf(c) + 1;
                String visualIdStr = String.valueOf(visualId);

                switch (attr) {
                    case ATTR_EMAIL:
                        return contains.apply(c.getEmail());

                    case ATTR_TYPE:
                        return contains.apply(c.getType());

                    case ATTR_CATEGORY:
                        return contains.apply(c.getCategory());

                    case ATTR_NIVEAU:
                        return contains.apply(c.getNiveau());

                    case ATTR_ANNEES: {
                        //  ici c'est >= n
                        if (!q.matches("\\d+")) return false; // si pas un nombre, aucun résultat
                        int min = Integer.parseInt(q);
                        return annees >= min;
                    }

                    case ATTR_CERTIF:
                        return contains.apply(c.getCertification());

                    case ATTR_STATUT:
                        return contains.apply(c.getStatut());

                    case ATTR_DESC:
                        return contains.apply(c.getDescription());

                    case ATTR_ALL:
                    default: {
                        //  Si l'utilisateur tape un nombre : on applique annees >= n
                        boolean matchAnnees = false;
                        if (q.matches("\\d+")) {
                            int min = Integer.parseInt(q);
                            matchAnnees = annees >= min;
                        } else {
                            // si texte, on garde le comportement "contains" sur anneesStr si tu veux
                            matchAnnees = anneesStr.contains(q);
                        }

                        return visualIdStr.contains(q)
                                || contains.apply(c.getEmail())
                                || contains.apply(c.getType())
                                || contains.apply(c.getCategory())
                                || contains.apply(c.getNiveau())
                                || matchAnnees
                                || contains.apply(c.getCertification())
                                || contains.apply(c.getStatut())
                                || contains.apply(c.getDescription());
                    }
                }
            });
        }


        // =========================
        // CRUD
        // =========================
        @FXML
        private void onAdd() {
            if (!validateInputs()) return;

            try {
                int annees = parseAnneesOr0();

                Competence c = new Competence(
                        0,
                        tfCategory.getText().trim(),
                        tfType.getText().trim(),
                        taDescription.getText().trim(),
                        cbNiveau != null ? cbNiveau.getValue() : "Débutant",
                        annees,
                        tfCertification != null ? tfCertification.getText().trim() : null,
                        cbStatut != null ? cbStatut.getValue() : "En cours",
                        tfEmail != null ? tfEmail.getText().trim() : null
                );

                int newId = service.addAndReturnId(c);

                Competence inserted = new Competence(
                        newId,
                        c.getCategory(),
                        c.getType(),
                        c.getDescription(),
                        c.getNiveau(),
                        c.getAnneesExperience(),
                        c.getCertification(),
                        c.getStatut(),
                        c.getEmail()
                );

                data.add(inserted);
                applyFilters();
                selectRowByDbId(newId);

                clearFormOnly();
                setError(" Ajout effectué");
            } catch (Exception e) {
                setError("Erreur ajout: " + e.getMessage());
                e.printStackTrace();
            }
        }



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
                setError("Erreur ouverture profil : " + e.getMessage());
            }
        }

        @FXML
        private void onUpdate() {
            Competence selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { setError("Sélectionne une ligne à modifier."); return; }
            if (!validateInputs()) return;

            try {
                int annees = parseAnneesOr0();

                Competence updated = new Competence(
                        selected.getId(),
                        tfCategory.getText().trim(),
                        tfType.getText().trim(),
                        taDescription.getText().trim(),
                        cbNiveau != null ? cbNiveau.getValue() : selected.getNiveau(),
                        annees,
                        tfCertification != null ? tfCertification.getText().trim() : selected.getCertification(),
                        cbStatut != null ? cbStatut.getValue() : selected.getStatut(),
                        tfEmail != null ? tfEmail.getText().trim() : selected.getEmail()
                );

                service.update(updated);

                int idx = indexOfById(updated.getId());
                if (idx >= 0) data.set(idx, updated);

                applyFilters();
                selectRowByDbId(updated.getId());

                table.refresh();
                setError(" Modification effectuée.");
            } catch (Exception e) {
                setError("Erreur modification: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @FXML
        private void onDelete() {
            Competence selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { setError("Sélectionne une ligne à supprimer."); return; }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer cette compétence ?");


            DialogPane dp = alert.getDialogPane();

            // ✅ CSS dark
            dp.getStyleClass().add("dark-dialog-pane");
            URL css = getClass().getResource(CSS_PATH);
            if (css != null) dp.getStylesheets().add(css.toExternalForm());

            // ✅ rendre la fenêtre plus petite
            dp.setPrefWidth(320);     // ← diminue la largeur (essaie 280..360)
            dp.setMaxWidth(320);
            dp.setMinHeight(150);     // ← diminue la hauteur
            dp.setPrefHeight(140);

            // ✅ empêche l'alert de s'étirer à cause du contenu
            dp.setExpandableContent(null);

            // ✅ bouton OK par défaut (rouge via :default si ton CSS le fait)
            Button okBtn = (Button) dp.lookupButton(ButtonType.OK);
            if (okBtn != null) okBtn.setDefaultButton(true);

            alert.initOwner(table.getScene().getWindow());

            if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            try {
                service.delete(selected.getId());

                int idx = indexOfById(selected.getId());
                if (idx >= 0) data.remove(idx);

                applyFilters();
                table.getSelectionModel().clearSelection();
                clearFormOnly();
                setError(" Suppression effectuée.");
            } catch (Exception e) {
                setError("Erreur suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private int parseAnneesOr0() {
            if (tfAnnees == null) return 0;

            String s = nvl(tfAnnees.getText()).trim();
            if (s.isEmpty()) return 0;

            try {
                int v = Integer.parseInt(s);
                if (v < ANNEES_MIN) return ANNEES_MIN;
                if (v > ANNEES_MAX) return ANNEES_MAX;
                return v;
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private int indexOfById(int dbId) {
            for (int i = 0; i < data.size(); i++) {
                Competence c = data.get(i);
                if (c != null && c.getId() == dbId) return i;
            }
            return -1;
        }

        private void selectRowByDbId(int dbId) {
            Optional<Competence> match = table.getItems().stream()
                    .filter(c -> c != null && c.getId() == dbId)
                    .findFirst();

            match.ifPresent(c -> {
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().select(c);
                table.scrollTo(c);
            });
        }

        // =========================
        // RESET / FORM
        // =========================
        @FXML
        private void onResetSearch() {
            if (cbTypeFilter != null) cbTypeFilter.setValue(ATTR_ALL);
            if (tfSearchValue != null) tfSearchValue.clear();
            applyFilters();
        }

        private void clearFormOnly() {
            if (tfCategory != null) tfCategory.clear();
            if (tfType != null) tfType.clear();
            if (taDescription != null) taDescription.clear();

            if (cbNiveau != null) cbNiveau.setValue("Débutant");
            if (tfAnnees != null) tfAnnees.setText("0");
            if (tfCertification != null) tfCertification.clear();
            if (cbStatut != null) cbStatut.setValue("En cours");

            if (tfEmail != null) tfEmail.clear();
        }

        // =========================
        // VALIDATION
        // =========================
        private boolean validateInputs() {

            String category = nvl(tfCategory != null ? tfCategory.getText() : "").trim();
            String type     = nvl(tfType != null ? tfType.getText() : "").trim();
            String desc     = nvl(taDescription != null ? taDescription.getText() : "").trim();

            String niveau = (cbNiveau != null) ? nvl(cbNiveau.getValue()).trim() : "";
            String statut = (cbStatut != null) ? nvl(cbStatut.getValue()).trim() : "";
            int annees = parseAnneesOr0();

            String cert  = nvl(tfCertification != null ? tfCertification.getText() : "").trim();
            String email = nvl(tfEmail != null ? tfEmail.getText() : "").trim();

            //  Champs obligatoires uniquement
            java.util.List<String> champsVides = new java.util.ArrayList<>();

            if (category.isEmpty()) champsVides.add("Catégorie");
            if (type.isEmpty())     champsVides.add("Type");
            if (desc.isEmpty())     champsVides.add("Description");
            if (email.isEmpty())    champsVides.add("Email");

            //  tous les champs obligatoires vides
            if (champsVides.size() == 4) {
                setError("Complétez les données");
                return false;
            }

            //  certains champs obligatoires vides
            if (!champsVides.isEmpty()) {
                setError("Complétez les données (" +
                        String.join(", ", champsVides) + ")");
                return false;
            }

            //  Autres validations
            if (annees < ANNEES_MIN || annees > ANNEES_MAX) {
                setError("Années d'expérience invalides (" + ANNEES_MIN + ".." + ANNEES_MAX + ").");
                return false;
            }

            if (!P_EMAIL.matcher(email).matches()) {
                setError("Email invalide.");
                return false;
            }

            if (category.length() > 80) { setError("Catégorie trop longue (max 80)."); return false; }
            if (type.length() > 80)     { setError("Type trop long (max 80)."); return false; }
            if (desc.length() > 500)    { setError("Description trop longue (max 500)."); return false; }
            if (cert.length() > 100)    { setError("Certification trop longue (max 100)."); return false; }

            if (!P_SIMPLE.matcher(category).matches()) { setError("Catégorie : caractères interdits."); return false; }
            if (!P_SIMPLE.matcher(type).matches())     { setError("Type : caractères interdits."); return false; }

            setError("");
            return true;
        }


        private void setupInputValidation() {
            UnaryOperator<TextFormatter.Change> simpleFilter = change -> {
                String newText = change.getControlNewText();
                if (P_SIMPLE.matcher(newText).matches()) return change;
                showInvalidCharMessage(" Caractères interdits : <>:?.@ ");
                return null;
            };

            if (tfCategory != null) tfCategory.setTextFormatter(new TextFormatter<>(simpleFilter));
            if (tfType != null) tfType.setTextFormatter(new TextFormatter<>(simpleFilter));
        }

        private void showInvalidCharMessage(String msg) {
            if (lblError != null && !msg.equals(lblError.getText())) lblError.setText(msg);
        }

        private void setError(String msg) {
            if (lblError != null) lblError.setText(msg);
        }

        private static String normalizeComboValue(ComboBox<String> combo, String value, String fallback) {
            if (combo == null) return value;
            String v = nvl(value).trim();
            if (v.isEmpty()) return fallback;
            if (combo.getItems() != null && combo.getItems().contains(v)) return v;
            return fallback;
        }

        // =========================
        // 🖱️ CLICK SOUND
        // =========================
        private void initClickSound() {
            try {
                URL url = getClass().getResource("/view/files/competences/mouse.wav");
                if (url == null) return;

                clickClip = new AudioClip(url.toExternalForm());
                clickClip.setVolume(0.55);
            } catch (Exception ignored) { }
        }

        private void installGlobalClickSound() {
            if (root == null || clickClip == null) return;
            root.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> playClickSound());
        }

        private void playClickSound() {
            if (clickClip == null) return;

            long now = System.currentTimeMillis();
            if (now - lastClickSoundMs < CLICK_COOLDOWN_MS) return;
            lastClickSoundMs = now;

            try { clickClip.play(); } catch (Exception ignored) { }
        }

        // =========================
        // 🎵 MUSIC
        // =========================
      /*  private void setupMusicControls() {
            if (slVolume != null) {
                slVolume.setMin(0);
                slVolume.setMax(1);

                if (slVolume.getValue() == 0) slVolume.setValue(0.35);

                slVolume.setShowTickMarks(false);
                slVolume.setShowTickLabels(false);

                slVolume.valueProperty().addListener((obs, oldV, newV) -> {
                    if (mediaPlayer != null) mediaPlayer.setVolume(newV.doubleValue());
                });
            }
        }

        private void playMusic() {
            try {
                URL url = getClass().getResource("/view/files/competences/music.mp3");
                if (url == null) return;

                Media media = new Media(url.toExternalForm());
                mediaPlayer = new MediaPlayer(media);

                double vol = (slVolume != null) ? slVolume.getValue() : 0.35;
                mediaPlayer.setVolume(vol);

                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
            } catch (Exception ignored) { }
        }

    */

        @FXML
        private void onResetForm() {
            // Important : annuler la sélection sinon le listener refill les champs
            if (table != null) {
                table.getSelectionModel().clearSelection();
            }

            // vider les champs du formulaire
            clearFormOnly();

            // nettoyer message erreur
            setError("");
        }




       /* @FXML
        private void onToggleMusic() {
            try {
                if (mediaPlayer == null) {
                    playMusic();
                    updateMusicUi();
                    return;
                }

                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) mediaPlayer.pause();
                else mediaPlayer.play();

                updateMusicUi();
            } catch (Exception ignored) { }
        }

        private void updateMusicUi() {
            boolean playing = mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;

            if (btnMusicToggle != null) btnMusicToggle.setText(playing ? "🔇" : "🔊");

            if (imgMusicIcon != null) {
                String iconPath = playing ? "/view/image/music_on.png" : "/view/image/music_off.png";
                try {
                    javafx.scene.image.Image fxImg =
                            new javafx.scene.image.Image(getClass().getResource(iconPath).toExternalForm());
                    imgMusicIcon.setImage(fxImg);
                } catch (Exception ignored) { }
            }
        }

        public void stopMedia() {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }
            } catch (Exception ignored) { }
            updateMusicUi();
        }
    */
        // =========================
        // CHATBOT
        // =========================
        @FXML
        private void onChatbot() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Competences/back/ChatBoat.fxml"));
                Parent chatRoot = loader.load();

                Scene scene = new Scene(chatRoot);
                URL css = getClass().getResource(CSS_PATH);
                if (css != null) scene.getStylesheets().add(css.toExternalForm());

                Stage stage = new Stage();
                stage.setTitle("Chatbot – Domaine professionnel");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.setMinWidth(520);
                stage.setMinHeight(640);
                stage.show();
            } catch (Exception e) {
                setError("Erreur Chatbot : " + e.getMessage());
                e.printStackTrace();
            }
        }

        // =========================
        // CALENDRIER
        // =========================
        @FXML
        private void onCalendar() {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Calendrier");

            Label lblTitle = new Label();
            Label lblSelected = new Label();

            ComboBox<Month> cbMonth = new ComboBox<>();
            cbMonth.getItems().setAll(Month.values());
            cbMonth.setValue(calendarMonth.getMonth());
            cbMonth.setButtonCell(monthCell());
            cbMonth.setCellFactory(list -> monthCell());

            Spinner<Integer> spYear = new Spinner<>(1900, 2100, calendarMonth.getYear());
            spYear.setEditable(true);
            spYear.setPrefWidth(110);

            Button btnPrev = new Button("◀");
            Button btnNext = new Button("▶");

            HBox header = new HBox(10, btnPrev, cbMonth, spYear, btnNext);
            header.setAlignment(Pos.CENTER_LEFT);

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.setPadding(new Insets(10, 0, 0, 0));

            VBox calRoot = new VBox(12, lblTitle, header, grid, lblSelected);
            calRoot.setPadding(new Insets(16));
            calRoot.setMinWidth(430);

            calRoot.getStyleClass().add("cal-root");
            lblTitle.getStyleClass().add("cal-title");
            lblSelected.getStyleClass().add("cal-selected");
            btnPrev.getStyleClass().add("cal-nav");
            btnNext.getStyleClass().add("cal-nav");
            cbMonth.getStyleClass().add("cal-combo");
            spYear.getStyleClass().add("cal-spinner");
            grid.getStyleClass().add("cal-grid");

            Runnable refreshCalendar = () -> {
                calendarMonth = LocalDate.of(spYear.getValue(), cbMonth.getValue(), 1);

                String monthName = calendarMonth.getMonth().getDisplayName(TextStyle.FULL, LOCALE_FR);
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

                lblTitle.setText(monthName + " " + calendarMonth.getYear());
                lblSelected.setText("Date sélectionnée : " + selectedDate.format(DATE_FMT));

                buildCalendarGrid(grid, calendarMonth, lblSelected);
            };

            btnPrev.setOnAction(e -> {
                calendarMonth = calendarMonth.minusMonths(1);
                cbMonth.setValue(calendarMonth.getMonth());
                spYear.getValueFactory().setValue(calendarMonth.getYear());
                refreshCalendar.run();
            });

            btnNext.setOnAction(e -> {
                calendarMonth = calendarMonth.plusMonths(1);
                cbMonth.setValue(calendarMonth.getMonth());
                spYear.getValueFactory().setValue(calendarMonth.getYear());
                refreshCalendar.run();
            });

            cbMonth.valueProperty().addListener((obs, o, n) -> refreshCalendar.run());
            spYear.valueProperty().addListener((obs, o, n) -> refreshCalendar.run());

            refreshCalendar.run();

            Scene scene = new Scene(calRoot);
            URL css = getClass().getResource(CSS_PATH);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();
        }

        private ListCell<Month> monthCell() {
            return new ListCell<>() {
                @Override protected void updateItem(Month item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getDisplayName(TextStyle.FULL, LOCALE_FR));
                }
            };
        }

        private void buildCalendarGrid(GridPane grid, LocalDate monthStart, Label lblSelected) {
            grid.getChildren().clear();
            grid.getColumnConstraints().clear();

            for (int i = 0; i < 7; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / 7.0);
                grid.getColumnConstraints().add(cc);
            }

            String[] days = {"Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam.", "Dim."};
            for (int col = 0; col < 7; col++) {
                Label h = new Label(days[col]);
                h.getStyleClass().add("cal-dow");
                grid.add(h, col, 0);
            }

            LocalDate firstDay = monthStart.withDayOfMonth(1);
            int daysInMonth = firstDay.lengthOfMonth();

            int startOffset = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            if (startOffset < 0) startOffset += 7;

            int row = 1;
            int col = startOffset;

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = firstDay.withDayOfMonth(day);

                Button b = new Button(String.valueOf(day));
                b.setMaxWidth(Double.MAX_VALUE);
                b.setMinHeight(36);
                b.getStyleClass().add("cal-day");

                if (date.equals(LocalDate.now())) b.getStyleClass().add("cal-today");
                if (date.equals(selectedDate)) b.getStyleClass().add("cal-day-selected");

                b.setOnAction(e -> {
                    selectedDate = date;
                    lblSelected.setText("Date sélectionnée : " + selectedDate.format(DATE_FMT));
                    buildCalendarGrid(grid, monthStart, lblSelected);
                });

                grid.add(b, col, row);

                col++;
                if (col == 7) { col = 0; row++; }
            }
        }

        // =========================
        // PDF EXPORT
        // =========================
        // =========================
    // PDF EXPORT   (NE CHANGE RIEN SAUF LE FILTRE)
    // =========================
        @FXML
        private void onExportPDF() {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Enregistrer le PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                fileChooser.setInitialFileName("competences.pdf");

                File file = fileChooser.showSaveDialog(table.getScene().getWindow());
                if (file == null) return;

                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);

                pdf.addNewPage();

                PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
                Rectangle pageSize = pdf.getFirstPage().getPageSize();

                float margin = 20f;
                float stroke = 2f;

                canvas.setLineWidth(stroke);
                canvas.setStrokeColor(ColorConstants.BLACK);
                canvas.rectangle(
                        pageSize.getLeft() + margin,
                        pageSize.getBottom() + margin,
                        pageSize.getWidth() - 2 * margin,
                        pageSize.getHeight() - 2 * margin
                );
                canvas.stroke();

                Document document = new Document(pdf);

                Table header = new Table(new float[]{1, 4});
                header.setWidth(UnitValue.createPercentValue(100));

                Image logoImg = loadLogoFromResources("/view/image/competences/logo.png");
                if (logoImg != null) {
                    logoImg.setAutoScale(true);
                    logoImg.setMaxHeight(50);

                    header.addCell(new com.itextpdf.layout.element.Cell()
                            .setBorder(Border.NO_BORDER)
                            .add(logoImg));
                } else {
                    header.addCell(new com.itextpdf.layout.element.Cell()
                            .setBorder(Border.NO_BORDER)
                            .add(new Paragraph("")));
                }

                Paragraph title = new Paragraph("SkillSwap")
                        .setBold()
                        .setFontSize(22)
                        .setTextAlignment(TextAlignment.LEFT);

                Paragraph subtitle = new Paragraph("Liste des Compétences")
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.LEFT);

                header.addCell(new com.itextpdf.layout.element.Cell()
                        .setBorder(Border.NO_BORDER)
                        .add(title)
                        .add(subtitle));

                document.add(header);
                document.add(new Paragraph(" "));

                Table pdfTable = new Table(5);
                pdfTable.setWidth(UnitValue.createPercentValue(100));

                pdfTable.addHeaderCell("Email");
                pdfTable.addHeaderCell("Niveau");
                pdfTable.addHeaderCell("Type");
                pdfTable.addHeaderCell("Certification");
                pdfTable.addHeaderCell("Description");

                //  SEULE MODIF : exporter uniquement les lignes Validée
                for (Competence c : table.getItems()) {
                    if (c == null) continue;

                    String statutNorm = normalize(nvl(c.getStatut())); // normalize enlève accents + minuscule
                    boolean isValidee = statutNorm.equals("valide") || statutNorm.equals("validee");
                    if (!isValidee) continue;

                    pdfTable.addCell(nvl(c.getEmail()));
                    pdfTable.addCell(nvl(c.getNiveau()));
                    pdfTable.addCell(nvl(c.getType()));
                    pdfTable.addCell(nvl(c.getCertification()));
                    pdfTable.addCell(nvl(c.getDescription()));
                }

                document.add(pdfTable);

                document.add(new Paragraph(" "));
                Image signatureImg = loadLogoFromResources("/view/image/competences/signature.png");
                if (signatureImg != null) {
                    signatureImg.scaleToFit(160, 80);
                    signatureImg.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                    document.add(signatureImg);
                }

                document.close();
                setError(" PDF généré avec succès ");
            } catch (Exception e) {
                setError("Erreur PDF : " + e.getMessage());
                e.printStackTrace();
            }
        }


        private Image loadLogoFromResources(String resourcePath) {
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) return null;

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tmp = new byte[4096];
                int read;
                while ((read = is.read(tmp)) != -1) buffer.write(tmp, 0, read);

                ImageData imageData = ImageDataFactory.create(buffer.toByteArray());
                return new Image(imageData);
            } catch (Exception e) {
                return null;
            }
        }

        @FXML
        private void onDashboard() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Competences/back/Dashboard.fxml"));
                Parent dashRoot = loader.load();

                Dashboard controller = loader.getController();
                controller.setData(table.getItems());

                Scene scene = new Scene(dashRoot, 720, 520);
                URL css = getClass().getResource(CSS_PATH);
                if (css != null) scene.getStylesheets().add(css.toExternalForm());

                Stage stage = new Stage();
                stage.setTitle("Dashboard");
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                setError("Erreur Dashboard : " + e.getMessage());
                e.printStackTrace();
            }
        }

        private static String nvl(String s) {
            return s == null ? "" : s;
        }

        private static String normalize(String s) {
            if (s == null) return "";
            String lower = s.trim().toLowerCase(Locale.ROOT);
            return Normalizer.normalize(lower, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
        }


        @FXML
        private void onHistorique() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Competences/back/HistoriqueCompetences.fxml"));
                Parent histRoot = loader.load();

                Scene scene = new Scene(histRoot);

                //  réutilise ton CSS competences
                URL css = getClass().getResource("/view/css/competences/back/style.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());

                Stage stage = new Stage();
                stage.setTitle("Historique – Compétences");
                stage.initModality(Modality.APPLICATION_MODAL); // bloque le dashboard tant que l'historique est ouvert
                stage.setScene(scene);
                stage.setMinWidth(950);
                stage.setMinHeight(600);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                // si tu as un label d'erreur dans dashboard tu peux l'afficher,
                // sinon laisse juste printStackTrace().
            }
        }





    }
