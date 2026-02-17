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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.model.Competences.HistoriqueCompetence;
import org.service.Competences.Historiques.HistoriqueCompetenceService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class HistoriqueCompetencesController {

    @FXML private BorderPane root;

    @FXML private TableView<HistoriqueCompetence> table;

    @FXML private TableColumn<HistoriqueCompetence, String> colAction;
    @FXML private TableColumn<HistoriqueCompetence, String> colDate;
    @FXML private TableColumn<HistoriqueCompetence, String> colEmail;

    @FXML private TableColumn<HistoriqueCompetence, String> colCategory;
    @FXML private TableColumn<HistoriqueCompetence, String> colType;
    @FXML private TableColumn<HistoriqueCompetence, String> colDescription;

    @FXML private TableColumn<HistoriqueCompetence, String> colNiveau;
    @FXML private TableColumn<HistoriqueCompetence, Number> colAnnees;
    @FXML private TableColumn<HistoriqueCompetence, String> colCertification;
    @FXML private TableColumn<HistoriqueCompetence, String> colStatut;

    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private TextField tfSearchValue;

    @FXML private ComboBox<String> cbMonth;
    @FXML private Spinner<Integer> spYear;
    @FXML private TableColumn<HistoriqueCompetence, Boolean> colSelect;


    @FXML private Label lblError;

    // Optionnel : fx:id="btnDelete" dans le FXML => auto-disable
    @FXML private Button btnDelete;

    private final HistoriqueCompetenceService service = new HistoriqueCompetenceService();
    private final ObservableList<HistoriqueCompetence> data = FXCollections.observableArrayList();
    private FilteredList<HistoriqueCompetence> filteredData;

    private static final String ATTR_ALL = "Tous";
    private static final String ATTR_ACTION = "Action";
    private static final String ATTR_DATE = "Date";
    private static final String ATTR_EMAIL = "Email";
    private static final String ATTR_TYPE = "Type";
    private static final String ATTR_CATEGORY = "Catégorie";
    private static final String ATTR_NIVEAU = "Niveau";
    private static final String ATTR_ANNEES = "Expérience";
    private static final String ATTR_STATUT = "Statut";
    private static final String ATTR_CERTIF = "Certification";
    private static final String ATTR_DESC = "Description";

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String CSS_PATH = "/view/css/competences/style.css";

    @FXML
    public void initialize() {
        setupSearchUI();
        setupDateNavigator();
        setupTable();

        if (btnDelete != null && table != null) {
            btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        }

        refresh();
    }

    private void setupSearchUI() {
        if (cbTypeFilter != null) {
            cbTypeFilter.setItems(FXCollections.observableArrayList(
                    ATTR_ALL, ATTR_ACTION, ATTR_DATE, ATTR_EMAIL,
                    ATTR_TYPE, ATTR_CATEGORY, ATTR_NIVEAU,
                    ATTR_ANNEES, ATTR_STATUT, ATTR_CERTIF, ATTR_DESC
            ));
            cbTypeFilter.setValue(ATTR_ALL);
            cbTypeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        if (tfSearchValue != null) {
            tfSearchValue.textProperty().addListener((obs, o, n) -> applyFilters());
        }
    }

    private void setupDateNavigator() {
        if (cbMonth != null) {
            List<String> months = new ArrayList<>();
            for (Month m : Month.values()) {
                months.add(m.getDisplayName(TextStyle.FULL, Locale.FRENCH));
            }
            cbMonth.setItems(FXCollections.observableArrayList(months));
            cbMonth.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        if (spYear != null) {
            int currentYear = LocalDate.now().getYear();
            SpinnerValueFactory<Integer> vf =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, currentYear);
            spYear.setValueFactory(vf);
            spYear.setEditable(true);
            spYear.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        YearMonth now = YearMonth.now();
        if (cbMonth != null) cbMonth.getSelectionModel().select(now.getMonthValue() - 1);
        if (spYear != null) spYear.getValueFactory().setValue(now.getYear());
    }

    @FXML
    private void onPrevMonth() { changeMonth(-1); }

    @FXML
    private void onNextMonth() { changeMonth(+1); }

    private void changeMonth(int delta) {
        if (cbMonth == null || spYear == null) return;

        int monthIndex = cbMonth.getSelectionModel().getSelectedIndex();
        int year = (spYear.getValue() == null) ? YearMonth.now().getYear() : spYear.getValue();

        if (monthIndex < 0) monthIndex = YearMonth.now().getMonthValue() - 1;

        YearMonth ym = YearMonth.of(year, monthIndex + 1).plusMonths(delta);

        cbMonth.getSelectionModel().select(ym.getMonthValue() - 1);
        spYear.getValueFactory().setValue(ym.getYear());
        applyFilters();
    }

    private void setupTable() {

        // ✅ MULTI SELECTION PAR CHECKBOX + SELECT ALL
        if (colSelect != null) {

            table.setEditable(true);

            // checkbox ligne
            colSelect.setCellValueFactory(
                    cellData -> cellData.getValue().selectedProperty()
            );

            colSelect.setCellFactory(
                    CheckBoxTableCell.forTableColumn(colSelect)
            );

            // ✅ CHECKBOX HEADER (SELECT ALL)
            CheckBox selectAll = new CheckBox();
            selectAll.setStyle("-fx-padding:0 0 0 5;");

            selectAll.setOnAction(e -> {
                boolean selected = selectAll.isSelected();
                for (HistoriqueCompetence h : data) {
                    h.setSelected(selected);
                }
                table.refresh();
            });

            colSelect.setGraphic(selectAll);
        }


        colAction.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getStatusChange()))
        );

        colDate.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getDateModification() == null
                                ? ""
                                : c.getValue().getDateModification().format(DT_FMT))
        );

        colEmail.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getEmail()))
        );

        colCategory.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getCategory()))
        );

        colType.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getType()))
        );

        colDescription.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getDescription()))
        );

        colNiveau.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getNiveau()))
        );

        colAnnees.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(c.getValue().getAnneesExperience())
        );

        colCertification.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getCertification()))
        );

        colStatut.setCellValueFactory(
                c -> new SimpleStringProperty(nvl(c.getValue().getStatut()))
        );



        filteredData = new FilteredList<>(data, p -> true);
        SortedList<HistoriqueCompetence> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void refresh() {
        try {
            data.setAll(service.getAll());
        } catch (Exception e) {
            setError("Erreur chargement historique");
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String attr = nvl(cbTypeFilter != null ? cbTypeFilter.getValue() : ATTR_ALL).trim();
        String q = nvl(tfSearchValue != null ? tfSearchValue.getText() : "").trim().toLowerCase(Locale.ROOT);

        int selectedMonth = (cbMonth != null && cbMonth.getSelectionModel().getSelectedIndex() >= 0)
                ? cbMonth.getSelectionModel().getSelectedIndex() + 1
                : YearMonth.now().getMonthValue();

        int selectedYear = (spYear != null && spYear.getValue() != null)
                ? spYear.getValue()
                : YearMonth.now().getYear();

        filteredData.setPredicate(h -> {
            if (h == null) return false;

            if (h.getDateModification() == null) return false;
            if (h.getDateModification().getMonthValue() != selectedMonth) return false;
            if (h.getDateModification().getYear() != selectedYear) return false;

            if (q.isEmpty()) return true;

            java.util.function.Function<String, Boolean> contains =
                    s -> nvl(s).toLowerCase(Locale.ROOT).contains(q);

            String dateStr = h.getDateModification().format(DT_FMT).toLowerCase(Locale.ROOT);
            int annees = h.getAnneesExperience();

            switch (attr) {
                case ATTR_ACTION: return contains.apply(h.getStatusChange());
                case ATTR_DATE:   return dateStr.contains(q);
                case ATTR_EMAIL:  return contains.apply(h.getEmail());
                case ATTR_TYPE:   return contains.apply(h.getType());
                case ATTR_CATEGORY: return contains.apply(h.getCategory());
                case ATTR_NIVEAU: return contains.apply(h.getNiveau());
                case ATTR_STATUT: return contains.apply(h.getStatut());
                case ATTR_CERTIF: return contains.apply(h.getCertification());
                case ATTR_DESC:   return contains.apply(h.getDescription());
                case ATTR_ANNEES:
                    if (!q.matches("\\d+")) return false;
                    return annees >= Integer.parseInt(q);

                case ATTR_ALL:
                default:
                    boolean matchAnnees = q.matches("\\d+")
                            ? annees >= Integer.parseInt(q)
                            : String.valueOf(annees).contains(q);

                    return contains.apply(h.getStatusChange())
                            || dateStr.contains(q)
                            || contains.apply(h.getEmail())
                            || contains.apply(h.getType())
                            || contains.apply(h.getCategory())
                            || contains.apply(h.getNiveau())
                            || matchAnnees
                            || contains.apply(h.getCertification())
                            || contains.apply(h.getStatut())
                            || contains.apply(h.getDescription());
            }
        });
    }

    @FXML
    private void onDeleteSelected() {

        List<HistoriqueCompetence> selectedItems = data.stream()
                .filter(HistoriqueCompetence::isSelected)
                .toList();

        if (selectedItems.isEmpty()) {
            setError("Veuillez sélectionner au moins une ligne.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");

        // ✅ IMPORTANT : supprime le header JavaFX (zone blanche)
        alert.setHeaderText(null);

        alert.setContentText(
                "Supprimer " + selectedItems.size() + " ligne(s) sélectionnée(s) ?"
        );

        ButtonType btnYes =
                new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo =
                new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnYes, btnNo);

    /* ===============================
       ✅ APPLICATION DU CSS DARK
       =============================== */

        DialogPane pane = alert.getDialogPane();

        // charger ton CSS
        var css = getClass().getResource(CSS_PATH);
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        // classe principale du CSS fourni
        pane.getStyleClass().add("dark-dialog-pane");

        // IMPORTANT → force la scene aussi en dark
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().setStyle("-fx-background-color:#0b1220;");
            }
        });

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == btnYes) {
            try {
                for (HistoriqueCompetence h : selectedItems) {
                    service.delete(h.getId());
                }
                data.removeAll(selectedItems);
                setError("Suppression effectuée.");
            } catch (Exception e) {
                setError("Erreur suppression : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }




    @FXML
    private void onResetSearch() {
        if (cbTypeFilter != null) cbTypeFilter.setValue(ATTR_ALL);
        if (tfSearchValue != null) tfSearchValue.clear();

        YearMonth now = YearMonth.now();
        if (cbMonth != null) cbMonth.getSelectionModel().select(now.getMonthValue() - 1);
        if (spYear != null) spYear.getValueFactory().setValue(now.getYear());

        applyFilters();
    }

    @FXML
    private void onRefresh() { refresh(); }

    @FXML
    private void onBackToCompetences() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/fxml/Competences/Competences.fxml"));
            Scene scene = new Scene(root);

            var css = getClass().getResource(CSS_PATH);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) this.root.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            setError("Erreur retour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Image loadLogoFromResources(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int read;

            while ((read = is.read(tmp)) != -1) {
                buffer.write(tmp, 0, read);
            }

            ImageData imageData = ImageDataFactory.create(buffer.toByteArray());
            return new Image(imageData);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void onExportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF (Historique)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("historique_competences.pdf");

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

            Paragraph subtitle = new Paragraph("Historique des compétences")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT);

            header.addCell(new com.itextpdf.layout.element.Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(title)
                    .add(subtitle));

            document.add(header);
            document.add(new Paragraph(" "));

            Table pdfTable = new Table(6);
            pdfTable.setWidth(UnitValue.createPercentValue(100));

            pdfTable.addHeaderCell("Action");
            pdfTable.addHeaderCell("Date");
            pdfTable.addHeaderCell("Email");
            pdfTable.addHeaderCell("Type");
            pdfTable.addHeaderCell("Catégorie");
            pdfTable.addHeaderCell("Niveau");

            for (HistoriqueCompetence h : table.getItems()) {
                if (h == null) continue;

                String dateStr = (h.getDateModification() == null)
                        ? ""
                        : h.getDateModification().format(DT_FMT);

                pdfTable.addCell(nvl(h.getStatusChange()));
                pdfTable.addCell(dateStr);
                pdfTable.addCell(nvl(h.getEmail()));
                pdfTable.addCell(nvl(h.getType()));
                pdfTable.addCell(nvl(h.getCategory()));
                pdfTable.addCell(nvl(h.getNiveau()));
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
            setError("PDF historique généré avec succès.");
        } catch (Exception e) {
            setError("Erreur PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setError(String msg) {
        if (lblError != null)
            lblError.setText(msg);
    }

    private static String nvl(String s) { return (s == null) ? "" : s; }
}
