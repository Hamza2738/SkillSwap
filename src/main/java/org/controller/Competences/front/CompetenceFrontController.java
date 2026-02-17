package org.controller.Competences.front;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.model.Competences.Competence;
import org.service.Competences.CompetenceService;

import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class CompetenceFrontController {

    // =========================
    // FXML
    // =========================
    @FXML private TableView<Competence> table;
    @FXML private TableColumn<Competence, Number> colId;
    @FXML private TableColumn<Competence, String> colEmail;
    @FXML private TableColumn<Competence, String> colStatut;
    @FXML private TableColumn<Competence, String> colNiveau;
    @FXML private TableColumn<Competence, String> colType;
    @FXML private TableColumn<Competence, String> colCategory;
    @FXML private TableColumn<Competence, String> colCertification;
    @FXML private TableColumn<Competence, Number> colAnnees;
    @FXML private TableColumn<Competence, String> colDescription;

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

    // =========================
    // SERVICES / DATA
    // =========================
    private final CompetenceService service = new CompetenceService();

    // ✅ FRONT: affiche uniquement ce que tu ajoutes via ce front (pas toute la DB)
    private final javafx.collections.ObservableList<Competence> data =
            FXCollections.observableArrayList();

    // =========================
    // CONST
    // =========================
    private static final String CSS_PATH = "/view/css/competences/style.css";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_FR = Locale.FRENCH;

    private static final String[] NIVEAUX =
            {"Débutant", "Intermédiaire", "Avancé", "Expert"};

    private static final String[] STATUTS =
            {"Validée", "En cours", "Expirée"};

    private static final int ANNEES_MIN = 0;
    private static final int ANNEES_MAX = 60;

    private static final Pattern P_SIMPLE =
            Pattern.compile("^[\\p{L}0-9 _-]*$");

    private static final Pattern P_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // =========================
    // CALENDAR STATE (nécessaire)
    // =========================
    private LocalDate calendarMonth = LocalDate.now().withDayOfMonth(1);
    private LocalDate selectedDate  = LocalDate.now();

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {
        setupExtraFields();
        setupInputValidation();
        setupTable();
        setupSearchUI();

        // IMPORTANT: le TableView doit utiliser la liste locale
        if (table != null) table.setItems(data);

        setError("");
    }

    // =========================
    // NAV TOPBAR (pour éviter crash FXML)
    // =========================
    @FXML private void onAccueil() { setError(""); }
    @FXML private void onProfil()  { setError(""); }
    @FXML private void onOffres()  { setError(""); }

    // =========================
    // TABLE
    // =========================
    private void setupTable() {
        if (colCategory != null) colCategory.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getCategory())));
        if (colType != null) colType.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getType())));
        if (colDescription != null) colDescription.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getDescription())));
        if (colNiveau != null) colNiveau.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getNiveau())));
        if (colCertification != null) colCertification.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getCertification())));
        if (colStatut != null) colStatut.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getStatut())));
        if (colEmail != null) colEmail.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getEmail())));
        if (colAnnees != null) colAnnees.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getAnneesExperience()));

        if (colId != null) {
            colId.setCellValueFactory(c ->
                    new ReadOnlyObjectWrapper<>(table.getItems().indexOf(c.getValue()) + 1)
            );
        }

        // selection -> remplir form
        if (table != null) {
            table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
                if (c == null) return;

                if (tfCategory != null) tfCategory.setText(nvl(c.getCategory()));
                if (tfType != null) tfType.setText(nvl(c.getType()));
                if (taDescription != null) taDescription.setText(nvl(c.getDescription()));
                if (cbNiveau != null) cbNiveau.setValue(normalizeComboValue(cbNiveau, nvl(c.getNiveau()), "Débutant"));
                if (tfAnnees != null) tfAnnees.setText(String.valueOf(c.getAnneesExperience()));
                if (tfCertification != null) tfCertification.setText(nvl(c.getCertification()));
                if (cbStatut != null) cbStatut.setValue(normalizeComboValue(cbStatut, nvl(c.getStatut()), "En cours"));
                if (tfEmail != null) tfEmail.setText(nvl(c.getEmail()));
            });
        }
    }

    // =========================
    // SEARCH UI (safe)
    // =========================
    private void setupSearchUI() {
        // Optionnel côté front (pas obligatoire)
        // On laisse vide pour éviter d’éventuelles erreurs si FXML contient ces nodes.
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
                    tfCertification != null ? tfCertification.getText().trim() : "",
                    cbStatut != null ? cbStatut.getValue() : "En cours",
                    tfEmail != null ? tfEmail.getText().trim() : ""
            );

            // DB -> récupère ID
            int newId = service.addAndReturnId(c);

            // ✅ AJOUT dans TABLE (liste locale)
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
            table.refresh();
            selectRowByDbId(newId);

            clearFormOnly();
            setError("Ajout effectué.");

        } catch (Exception e) {
            setError("Erreur ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdate() {
        if (table == null) return;

        Competence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { setError("Sélectionne une ligne."); return; }
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

            table.refresh();
            setError("Modification effectuée.");

        } catch (Exception e) {
            setError("Erreur modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        if (table == null) return;

        Competence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { setError("Sélectionne une ligne."); return; }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette compétence ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            service.delete(selected.getId());

            int idx = indexOfById(selected.getId());
            if (idx >= 0) data.remove(idx);

            table.getSelectionModel().clearSelection();
            clearFormOnly();
            setError("Suppression effectuée.");

        } catch (Exception e) {
            setError("Erreur suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
    // RESET (si FXML appelle)
    // =========================
    @FXML
    private void onResetSearch() {
        if (cbTypeFilter != null) cbTypeFilter.getSelectionModel().clearSelection();
        if (tfSearchValue != null) tfSearchValue.clear();
        setError("");
    }

    @FXML
    private void onResetForm() {
        if (table != null) table.getSelectionModel().clearSelection();
        clearFormOnly();
        setError("");
    }

    // =========================
    // DASHBOARD / CHATBOT
    // =========================
    @FXML
    private void onDashboard() {
        openModal("/view/fxml/Competences/front/Dashboard.fxml", "Dashboard (Front)", 720, 520);
    }

    @FXML
    private void onChatbot() {
        openModal("/view/fxml/Competences/front/ChatBoat.fxml", "Chatbot (Front)", 520, 640);
    }

    private void openModal(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent r = loader.load();

            Scene scene = new Scene(r, w, h);
            URL css = getClass().getResource(CSS_PATH);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            setError("Erreur ouverture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
    // CALENDRIER (Front) - même structure que BACK
    // =========================
    @FXML
    private void onCalendar() {
        try {
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

            // CSS classes
            calRoot.getStyleClass().add("cal-root");
            lblTitle.getStyleClass().add("cal-title");
            lblSelected.getStyleClass().add("cal-selected");
            btnPrev.getStyleClass().add("cal-nav");
            btnNext.getStyleClass().add("cal-nav");
            cbMonth.getStyleClass().add("cal-combo");
            spYear.getStyleClass().add("cal-spinner");
            grid.getStyleClass().add("cal-grid");

            Runnable refreshCalendar = () -> {
                Month m = cbMonth.getValue();
                int y = spYear.getValue();

                if (m == null) m = LocalDate.now().getMonth();
                calendarMonth = LocalDate.of(y, m, 1);

                if (selectedDate == null ||
                        selectedDate.getMonth() != calendarMonth.getMonth() ||
                        selectedDate.getYear() != calendarMonth.getYear()) {
                    selectedDate = calendarMonth;
                }

                String monthName = calendarMonth.getMonth().getDisplayName(TextStyle.FULL, LOCALE_FR);
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

                lblTitle.setText(monthName + " " + calendarMonth.getYear());
                lblSelected.setText("Date sélectionnée : " + selectedDate.format(DATE_FMT));

                buildCalendarGrid(grid, calendarMonth, lblSelected);
            };

            btnPrev.setOnAction(e -> {
                LocalDate prev = calendarMonth.minusMonths(1);
                cbMonth.setValue(prev.getMonth());
                spYear.getValueFactory().setValue(prev.getYear());
                selectedDate = LocalDate.of(prev.getYear(), prev.getMonth(), 1);
                refreshCalendar.run();
            });

            btnNext.setOnAction(e -> {
                LocalDate next = calendarMonth.plusMonths(1);
                cbMonth.setValue(next.getMonth());
                spYear.getValueFactory().setValue(next.getYear());
                selectedDate = LocalDate.of(next.getYear(), next.getMonth(), 1);
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

        } catch (Exception e) {
            setError("Erreur Calendrier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ListCell<Month> monthCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
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
        int startOffset = firstDay.getDayOfWeek().getValue() - 1; // 0..6 (lun..dim)

        int row = 1;
        int col = startOffset;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = firstDay.withDayOfMonth(day);

            Button b = new Button(String.valueOf(day));
            b.setMaxWidth(Double.MAX_VALUE);
            b.setMinHeight(36);
            b.getStyleClass().add("cal-day");

            if (date.equals(LocalDate.now())) b.getStyleClass().add("cal-today");
            if (selectedDate != null && date.equals(selectedDate)) b.getStyleClass().add("cal-day-selected");

            b.setOnAction(e -> {
                selectedDate = date;
                lblSelected.setText("Date sélectionnée : " + selectedDate.format(DATE_FMT));
                buildCalendarGrid(grid, monthStart, lblSelected);
            });

            grid.add(b, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    // =========================
    // VALIDATION
    // =========================
    private void setupInputValidation() {
        UnaryOperator<TextFormatter.Change> simpleFilter = change -> {
            String newText = change.getControlNewText();
            if (P_SIMPLE.matcher(newText).matches()) return change;
            showInvalidCharMessage("Caractères interdits.");
            return null;
        };

        if (tfCategory != null) tfCategory.setTextFormatter(new TextFormatter<>(simpleFilter));
        if (tfType != null) tfType.setTextFormatter(new TextFormatter<>(simpleFilter));
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
            if (tfAnnees.getText() == null || tfAnnees.getText().isBlank()) tfAnnees.setText("0");

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

            tfAnnees.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, intFilter));
        }
    }

    private boolean validateInputs() {
        String category = nvl(tfCategory != null ? tfCategory.getText() : "").trim();
        String type = nvl(tfType != null ? tfType.getText() : "").trim();
        String desc = nvl(taDescription != null ? taDescription.getText() : "").trim();
        String email = nvl(tfEmail != null ? tfEmail.getText() : "").trim();

        if (category.isEmpty() || type.isEmpty() || desc.isEmpty() || email.isEmpty()) {
            setError("Complétez les champs obligatoires.");
            return false;
        }

        int annees = parseAnneesOr0();
        if (annees < ANNEES_MIN || annees > ANNEES_MAX) {
            setError("Années invalides (0..60).");
            return false;
        }

        if (!P_EMAIL.matcher(email).matches()) {
            setError("Email invalide.");
            return false;
        }

        if (!P_SIMPLE.matcher(category).matches()) { setError("Catégorie : caractères interdits."); return false; }
        if (!P_SIMPLE.matcher(type).matches()) { setError("Type : caractères interdits."); return false; }

        setError("");
        return true;
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
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // SELECTION HELPERS
    // =========================
    private int indexOfById(int dbId) {
        for (int i = 0; i < data.size(); i++) {
            Competence c = data.get(i);
            if (c != null && c.getId() == dbId) return i;
        }
        return -1;
    }

    private void selectRowByDbId(int dbId) {
        if (table == null) return;

        Optional<Competence> match = table.getItems().stream()
                .filter(c -> c != null && c.getId() == dbId)
                .findFirst();

        match.ifPresent(c -> {
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(c);
            table.scrollTo(c);
        });
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
    // UI HELPERS
    // =========================
    private void showInvalidCharMessage(String msg) {
        if (lblError != null) lblError.setText(msg);
    }

    private void setError(String msg) {
        if (lblError != null) lblError.setText(msg == null ? "" : msg);
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static String normalize(String s) {
        if (s == null) return "";
        String lower = s.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String normalizeComboValue(ComboBox<String> combo, String value, String fallback) {
        if (combo == null) return value;
        String v = nvl(value).trim();
        if (v.isEmpty()) return fallback;
        if (combo.getItems() != null && combo.getItems().contains(v)) return v;
        return fallback;
    }

    // (optionnel) si ton FXML appelle ça
    public void onToggleMusic(ActionEvent actionEvent) {
        // pas utilisé ici
    }
}
