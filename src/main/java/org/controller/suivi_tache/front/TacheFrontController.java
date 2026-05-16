package org.controller.suivi_tache.front;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.model.suiviTache.Projet;
import org.model.suiviTache.Tache;
import org.service.suivi_tache.ProjetService;
import org.service.suivi_tache.TacheService;
import org.utils.Session;
import org.utils.SessionContext;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TacheFrontController {

    private final TacheService tacheService = new TacheService();
    private final ProjetService projetService = new ProjetService();

    @FXML private ComboBox<Projet> projetBox;
    @FXML private Label lblProjetSelected;

    @FXML private TableView<Tache> tableTache;
    @FXML private TableColumn<Tache, String> colTitre;
    @FXML private TableColumn<Tache, String> colStatut;
    @FXML private TableColumn<Tache, String> colPriorite;
    @FXML private TableColumn<Tache, LocalDate> colEcheance;
    @FXML private TableColumn<Tache, String> colMailUser;

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statutBox;
    @FXML private ComboBox<String> prioriteBox;
    @FXML private DatePicker echeancePicker;

    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Label lblMailUser;

    private int currentUserId = -1;
    private String currentUserEmail = "";
    private int currentProjetId = -1;

    @FXML
    public void initialize() {

        // ✅ IMPORTANT: ne pas bloquer sur isLoggedIn() pour charger les projets
        // car isLoggedIn() exige email non vide. Ici on utilise userId d’abord.
        currentUserId = SessionContext.getUserId();
        currentUserEmail = safeStr(SessionContext.getEmail());

        System.out.println("✅ SessionContext userId = " + currentUserId);
        System.out.println("✅ SessionContext email  = " + currentUserEmail);

        // ===== Colonnes Table =====
        if (colTitre != null) {
            colTitre.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue() == null ? "" : safeStr(d.getValue().getTitre())
                    ));
        }
        if (colStatut != null) {
            colStatut.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue() == null ? "" : safeStr(d.getValue().getStatut())
                    ));
        }
        if (colPriorite != null) {
            colPriorite.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue() == null ? "" : safeStr(d.getValue().getPriorite())
                    ));
        }
        if (colEcheance != null) {
            colEcheance.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            d.getValue() == null ? null : d.getValue().getEcheance()
                    ));
        }

        if (colMailUser != null) {
            colMailUser.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue() == null ? "" : safeStr(d.getValue().getMailUser())
                    ));
        }

        // ===== Combos tâche =====
        if (statutBox != null) {
            statutBox.setItems(FXCollections.observableArrayList("À faire", "En cours", "Terminée"));
        }
        if (prioriteBox != null) {
            prioriteBox.setItems(FXCollections.observableArrayList("Basse", "Moyenne", "Haute"));
        }

        // ===== Boutons (désactivés au départ) =====
        if (btnModifier != null) btnModifier.setDisable(true);
        if (btnSupprimer != null) btnSupprimer.setDisable(true);

        // ===== Double-clic tâche => charger =====
        if (tableTache != null) {
            tableTache.setRowFactory(tv -> {
                TableRow<Tache> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        remplirFormDepuisSelection();
                        refreshActionButtons();
                    }
                });
                return row;
            });

            // ✅ mise à jour boutons quand la sélection change
            tableTache.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
                refreshActionButtons();
            });
        }

        // ===== ComboBox projets =====
        configProjetBox();
        chargerProjetsUtilisateur();

        // init label mail
        if (lblMailUser != null) lblMailUser.setText(currentUserEmail);

        refreshActionButtons();

        System.out.println("✅ TacheFrontController chargé");
    }

    private void configProjetBox() {
        if (projetBox == null) return;

        // ✅ Ce qui s'affiche dans la zone sélectionnée
        projetBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Projet p) {
                return (p == null) ? "" : safeStr(p.getTitre());
            }
            @Override
            public Projet fromString(String s) {
                return null;
            }
        });

        // ✅ Ce qui s'affiche dans la liste dropdown
        projetBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Projet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : safeStr(item.getTitre()));
            }
        });

        // ✅ Ce qui s'affiche sur le "bouton" de la combo (très important)
        projetBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Projet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : safeStr(item.getTitre()));
            }
        });

        projetBox.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                currentProjetId = newP.getId();

                if (lblProjetSelected != null) {
                    lblProjetSelected.setText("Projet sélectionné : " + safeStr(newP.getTitre()));
                }

                chargerTaches();
                clearFormTache();
            } else {
                currentProjetId = -1;
                if (lblProjetSelected != null) lblProjetSelected.setText("");
                if (tableTache != null) tableTache.getItems().clear();
                refreshActionButtons();
            }
        });
    }

    private boolean isOwner(Tache t) {
        if (t == null) return false;

        // Le plus fiable : userId (si tu le remplis bien depuis la BD)
        if (t.getUserId() > 0) {
            return t.getUserId() == currentUserId;
        }

        // Sinon fallback sur mail_user
        return safeStr(t.getMailUser()).equalsIgnoreCase(safeStr(currentUserEmail));
    }

    private void refreshActionButtons() {
        Tache selected = tableTache == null ? null : tableTache.getSelectionModel().getSelectedItem();
        boolean owner = isOwner(selected);

        if (btnModifier != null) btnModifier.setDisable(selected == null || !owner);
        if (btnSupprimer != null) btnSupprimer.setDisable(selected == null || !owner);
    }

    private void chargerProjetsUtilisateur() {
        try {
            List<Projet> projets = projetService.afficherTous(); // ⚠️ PAS afficherParUtilisateur

            System.out.println("TEST afficherTous(): " + projets.size());

            for (Projet p : projets) {
                System.out.println("ID=" + p.getId() + " | titre=" + p.getTitre());
            }

            if (projetBox != null) {
                projetBox.setItems(FXCollections.observableArrayList(projets));
                projetBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerTaches() {
        if (tableTache == null) return;

        if (currentProjetId <= 0) {
            tableTache.getItems().clear();
            refreshActionButtons();
            return;
        }
        try {
            List<Tache> taches = tacheService.afficherParProjet(currentProjetId);
            tableTache.setItems(FXCollections.observableArrayList(taches));
            refreshActionButtons();
        } catch (SQLException e) {
            showError("Erreur chargement tâches : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterTache() {
        if (currentProjetId <= 0) {
            showWarning("Veuillez sélectionner un projet avant d'ajouter une tâche.");
            return;
        }
        if (!validerFormTache()) return;

        try {
            Tache t = new Tache(
                    0,
                    titreField.getText().trim(),
                    emptyToNull(getText(descriptionField)),
                    statutBox.getValue(),
                    prioriteBox.getValue(),
                    echeancePicker.getValue(),
                    currentProjetId,
                    currentUserId
            );

            t.setMailUser(currentUserEmail);

            tacheService.ajouter(t);
            chargerTaches();
            clearFormTache();
            showInfo("Tâche ajoutée avec succès ✅");

        } catch (SQLException e) {
            showError("Erreur ajout tâche : " + e.getMessage());
        }
    }

    @FXML
    private void modifierTache() {
        Tache selected = tableTache.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Veuillez sélectionner une tâche à modifier.");
            return;
        }

        if (!isOwner(selected)) {
            showWarning("Vous ne pouvez modifier que vos propres tâches.");
            return;
        }

        if (!validerFormTache()) return;

        try {
            selected.setTitre(titreField.getText().trim());
            selected.setDescription(emptyToNull(getText(descriptionField)));
            selected.setStatut(statutBox.getValue());
            selected.setPriorite(prioriteBox.getValue());
            selected.setEcheance(echeancePicker.getValue());
            selected.setMailUser(currentUserEmail);

            tacheService.modifier(selected);

            chargerTaches();
            clearFormTache();
            showInfo("Tâche modifiée avec succès ✅");

        } catch (SQLException e) {
            showError("Erreur modification tâche : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerTache() {
        Tache selected = tableTache.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Veuillez sélectionner une tâche à supprimer.");
            return;
        }

        if (!isOwner(selected)) {
            showWarning("Vous ne pouvez supprimer que vos propres tâches.");
            return;
        }

        if (!confirm("Supprimer la tâche", "Voulez-vous vraiment supprimer cette tâche ?")) return;

        try {
            tacheService.supprimer(selected.getId());
            tableTache.getItems().remove(selected);
            clearFormTache();
            showInfo("Tâche supprimée ✅");
        } catch (SQLException e) {
            showError("Erreur suppression tâche : " + e.getMessage());
        }
    }

    @FXML
    private void remplirFormDepuisSelection() {
        Tache selected = tableTache.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionne une tâche d'abord.");
            return;
        }

        titreField.setText(safeStr(selected.getTitre()));
        descriptionField.setText(selected.getDescription() == null ? "" : selected.getDescription());
        statutBox.setValue(selected.getStatut());
        prioriteBox.setValue(selected.getPriorite());
        echeancePicker.setValue(selected.getEcheance());

        if (lblMailUser != null) {
            lblMailUser.setText(safeStr(selected.getMailUser()));
        }

        refreshActionButtons();
    }

    @FXML
    private void viderForm() {
        clearFormTache();
    }

    private boolean validerFormTache() {
        String titre = (titreField.getText() == null) ? "" : titreField.getText().trim();

        if (titre.isEmpty()) {
            showWarning("Le titre de la tâche est obligatoire.");
            return false;
        }
        if (titre.length() < 3) {
            showWarning("Le titre de la tâche doit contenir au moins 3 caractères.");
            return false;
        }
        if (statutBox.getValue() == null) {
            showWarning("Veuillez choisir un statut.");
            return false;
        }
        if (prioriteBox.getValue() == null) {
            showWarning("Veuillez choisir une priorité.");
            return false;
        }

        LocalDate ech = echeancePicker.getValue();
        if (ech != null && ech.isBefore(LocalDate.now())) {
            showWarning("La date d'échéance ne peut pas être dans le passé.");
            return false;
        }

        return true;
    }

    private void clearFormTache() {
        titreField.clear();
        descriptionField.clear();

        statutBox.setValue(null);
        prioriteBox.setValue(null);
        echeancePicker.setValue(null);

        if (lblMailUser != null) lblMailUser.setText(currentUserEmail);

        refreshActionButtons();
    }

    private String getText(TextArea area) {
        return area == null ? "" : area.getText();
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }

    private boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);

        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(ok, cancel);

        setDarkAlert(a);

        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ok;
    }

    private void showWarning(String message) {
        Alert a = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        setDarkAlert(a);
        a.show();
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        setDarkAlert(a);
        a.show();
    }

    private void showInfo(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        setDarkAlert(a);
        a.show();
    }

    private void setDarkAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.getStyleClass().add("dark-dialog-pane");

        var cssUrl = getClass().getResource("/view/css/suiviTache/back/style.css");
        if (cssUrl != null) {
            dp.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
    @FXML
    void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/profil.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1920, 1000);

            var cssUrl = getClass().getResource("/view/css/utilisateur/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // ✅ passe le user connecté au profil
            if (Session.getCurrentUser() != null) {
                org.controller.utilisateur.ProfilController controller = loader.getController();
                controller.setUser(Session.getCurrentUser());
            }

            // ✅ FIX: table -> tableTache
            Stage stage = (Stage) tableTache.getScene().getWindow();
            stage.setTitle("SkillSwap - Profil");
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
                    getClass().getResource("/view/fxml/Competences/front/CompetenceFront.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1000);

            var cssUrl = getClass().getResource("/view/css/competences/front/style.css");
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
}