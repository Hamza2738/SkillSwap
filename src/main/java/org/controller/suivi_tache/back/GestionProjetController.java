package org.controller.suivi_tache.back;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;

import org.controller.utilisateur.ProfilAdminController;
import org.model.suiviTache.Projet;
import org.model.suiviTache.Tache;
import org.service.suivi_tache.ProjetService;
import org.service.suivi_tache.TacheService;
import org.service.suivi_tache.MailService;
import org.utils.Session;
import org.utils.SessionContext;

import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;


public class GestionProjetController {

    private final ProjetService projetService = new ProjetService();
    private final TacheService tacheService = new TacheService();

    @FXML private TableColumn<Tache, String> colEvaluation;
    @FXML private ComboBox<String> evaluationBox;

    @FXML private TableView<Projet> tableProjet;
    @FXML private TableColumn<Projet, String> colProjetTitre;
    @FXML private TableColumn<Projet, String> colProjetStatut;
    @FXML private TableColumn<Projet, String> colProjetMail;
    @FXML private TableColumn<Projet, LocalDate> colProjetDebut;
    @FXML private TableColumn<Projet, LocalDate> colProjetFin;
    @FXML private TableColumn<Projet, String>    colProjetDescription;
    @FXML private Button btnModifierProjet;
    @FXML private Button btnSupprimerProjet;
    @FXML private Button btnModifierTache;
    @FXML private Button btnSupprimerTache;
    @FXML private Button btnAjouterTache;

    @FXML private TableView<Tache> tableTache;
    @FXML private TableColumn<Tache, String> colTitre;
    @FXML private TableColumn<Tache, String> colStatut;
    @FXML private TableColumn<Tache, String> colPriorite;
    @FXML private TableColumn<Tache, LocalDate> colEcheance;
    @FXML private TableColumn<Tache, String> colTacheMail;

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statutBox;
    @FXML private ComboBox<String> prioriteBox;
    @FXML private DatePicker echeancePicker;

    @FXML private TextField projetTitreField;
    @FXML private TextArea projetDescriptionField;
    @FXML private DatePicker projetDebutPicker;
    @FXML private DatePicker projetFinPicker;
    @FXML private ComboBox<String> projetStatutBox;


    private int currentUserId;
    private String currentUserEmail;

    // ===== MAILING (même logique que Competence) =====
    private final ExecutorService mailExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mail-sender-suivi-tache");
        t.setDaemon(true);
        return t;
    });

    private final Set<String> mailLocks = ConcurrentHashMap.newKeySet();

    private static final Pattern P_EMAIL  = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    public void initialize() {
        if (btnModifierProjet != null) btnModifierProjet.setDisable(true);
        if (btnSupprimerProjet != null) btnSupprimerProjet.setDisable(true);
        if (btnAjouterTache != null) btnAjouterTache.setDisable(true);
        if (btnModifierTache != null) btnModifierTache.setDisable(true);
        if (btnSupprimerTache != null) btnSupprimerTache.setDisable(true);

        // ===== Session =====
        currentUserId = SessionContext.getUserId();
        currentUserEmail = safeStr(SessionContext.getEmail());

        // ===== Evaluation combo =====
        if (evaluationBox != null) {
            evaluationBox.setItems(FXCollections.observableArrayList(
                    "Excellent", "Bravo", "Bien", "Moyen", "Faible"
            ));
        }

        // ===== Vérif user connecté =====
        if (currentUserId <= 0) {
            showError("Aucun utilisateur connecté (userId invalide). Veuillez vous reconnecter.");
            return;
        }
        if (colProjetDebut != null) {
            colProjetDebut.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getDateDebut())
            );
        }

        if (colProjetFin != null) {
            colProjetFin.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getDateFin())
            );
        }

        if (colProjetDescription != null) {
            colProjetDescription.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getDescription()))
            );
        }
        // ===== Colonnes projets =====
        if (colProjetTitre != null) {
            colProjetTitre.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getTitre()))
            );
        }

        if (colProjetStatut != null) {
            colProjetStatut.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getStatut()))
            );
        }

        if (colProjetMail != null) {
            colProjetMail.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getMailUser()))
            );
        }

        // ===== Colonnes tâches =====
        if (colTitre != null) {
            colTitre.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getTitre()))
            );
        }

        if (colStatut != null) {
            colStatut.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getStatut()))
            );
        }

        if (colPriorite != null) {
            colPriorite.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getPriorite()))
            );
        }

        if (colEcheance != null) {
            colEcheance.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue() == null ? null : d.getValue().getEcheance())
            );
        }

        if (colTacheMail != null) {
            colTacheMail.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue() == null ? "" : safeStr(d.getValue().getMailUser()))
            );
        }

        // ===== Evaluation : VALUE FACTORY + CELL FACTORY (Hyperlink cliquable) =====
        if (colEvaluation != null) {
            colEvaluation.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue() == null || d.getValue().getEvaluation() == null ? "" : d.getValue().getEvaluation()
                    )
            );
            setupEvaluationMailing(); // ✅ le point important
        }

        // ===== Combos =====
        if (statutBox != null) {
            statutBox.setItems(FXCollections.observableArrayList("À faire", "En cours", "Terminée"));
        }
        if (prioriteBox != null) {
            prioriteBox.setItems(FXCollections.observableArrayList("Basse", "Moyenne", "Haute"));
        }
        if (projetStatutBox != null) {
            projetStatutBox.setItems(FXCollections.observableArrayList("En attente", "En cours", "Terminé"));
        }

        // ===== Chargement initial =====
        if (tableTache != null) tableTache.getItems().clear();

        if (tableProjet != null) {
            chargerProjets();

            tableProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
                if (newP != null) {
                    try {
                        if (tableTache != null) chargerTaches(newP.getId());
                        Projet full = projetService.findById(newP.getId());
                        remplirFormProjet(full != null ? full : newP);
                    } catch (SQLException e) {
                        showError("Erreur récupération projet : " + e.getMessage());
                        remplirFormProjet(newP);
                    }
                } else {
                    if (tableTache != null) tableTache.getItems().clear();
                    clearFormProjet();
                }
                refreshProjetButtons(); // ✅ active/grise selon ownership
                refreshTacheButtons();
            });

        } else {
            System.out.println("⚠️ tableProjet est null");
        }

        // ===== Listener sélection projet =====
        if (tableProjet != null) {
            chargerProjets(); // ✅ afficher TOUS les projets

            tableProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
                if (newP != null) {
                    try {
                        if (tableTache != null) chargerTaches(newP.getId());
                        Projet full = projetService.findById(newP.getId());
                        remplirFormProjet(full != null ? full : newP);
                    } catch (SQLException e) {
                        showError("Erreur récupération projet : " + e.getMessage());
                        remplirFormProjet(newP);
                    }
                } else {
                    if (tableTache != null) tableTache.getItems().clear();
                    clearFormProjet();
                }
                refreshProjetButtons(); // ✅ grise selon isOwnerProjet()
                refreshTacheButtons();  // ✅ reset tâches aussi
            });
        }

// ===== Listener sélection tâche =====
        if (tableTache != null) {
            tableTache.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
                refreshTacheButtons(); // ✅ grise selon isOwnerTache()
            });

            tableTache.setRowFactory(tv -> {
                TableRow<Tache> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        remplirFormTacheDepuisSelection();
                        refreshTacheButtons();
                    }
                });
                return row;
            });
        }


        // double clic tâche => remplir form tâche
        if (tableTache != null) {
            tableTache.setRowFactory(tv -> {
                TableRow<Tache> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        remplirFormTacheDepuisSelection();
                    }
                });
                return row;
            });
        }

        System.out.println("✅ GestionProjetController chargé (userId=" + currentUserId + ", mail=" + currentUserEmail + ")");
        // À la fin
        refreshProjetButtons();
        refreshTacheButtons();
    }

    // =========================
    // ✅ MAILING LOGIC (comme Competence)
    // =========================
    private void setupEvaluationMailing() {
        colEvaluation.setCellFactory(col -> new TableCell<Tache, String>() {
            private final Hyperlink link = new Hyperlink();

            {
                link.setOnAction(e -> {
                    // ✅ Récupérer la ligne via l'index, pas getTableRow().getItem()
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    Tache rowItem = getTableView().getItems().get(idx);
                    if (rowItem != null) sendEvaluationMailAsync(rowItem);
                });
            }

            @Override
            protected void updateItem(String evaluation, boolean empty) {
                super.updateItem(evaluation, empty);

                // ✅ Cellule vide ou pas de données → rien afficher
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // ✅ Récupérer la tâche via index (fiable)
                Tache rowItem = getTableView().getItems().get(getIndex());
                if (rowItem == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // ✅ Récupérer l'évaluation directement depuis l'objet (plus fiable que le param)
                String ev = rowItem.getEvaluation();

                if (ev == null || ev.isBlank()) {
                    // Afficher "—" si pas d'évaluation
                    setText("—");
                    setGraphic(null);
                    return;
                }

                ev = ev.trim();
                link.setText(ev);

                String norm = normalize(ev);
                boolean evalOk = norm.equals("bravo") || norm.equals("excellent");

                Projet projetSelectionne = tableProjet == null ? null
                        : tableProjet.getSelectionModel().getSelectedItem();
                boolean projetAMoi = isOwnerProjet(projetSelectionne);

                boolean clickable = evalOk && projetAMoi;
                link.setDisable(!clickable);
                link.setOpacity(clickable ? 1.0 : 0.5);

                // ✅ Style selon valeur
                String color = switch (norm) {
                    case "excellent" -> "#22c55e";
                    case "bravo"     -> "#3b82f6";
                    case "bien"      -> "#a78bfa";
                    case "moyen"     -> "#f59e0b";
                    case "faible"    -> "#ef4444";
                    default          -> "#94a3b8";
                };
                link.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

                setGraphic(link);
                setText(null);
            }
        });
    }

    private void sendEvaluationMailAsync(Tache t) {
        if (t == null) return;

        String evalRaw = safeStr(t.getEvaluation()).trim();
        String evalNorm = normalize(evalRaw);

        // on envoie uniquement si Bravo/Excellent (comme on a rendu cliquable)
        if (!(evalNorm.equals("bravo") || evalNorm.equals("excellent"))) {
            showWarning("Aucun mail envoyé : évaluation = '" + evalRaw + "'");
            return;
        }

        String to = safeStr(t.getMailUser()).trim();
        if (to.isEmpty() || !P_EMAIL.matcher(to).matches()) {
            showError("Email utilisateur invalide dans la tâche.");
            return;
        }

        // récupère le titre du projet
        String projetTitre = "";
        try {
            Projet p = projetService.findById(t.getProjetId());
            projetTitre = (p != null) ? safeStr(p.getTitre()) : "";
        } catch (SQLException ignored) {}

        if (projetTitre.isBlank()) {
            // fallback sur la sélection actuelle
            Projet selected = (tableProjet != null) ? tableProjet.getSelectionModel().getSelectedItem() : null;
            if (selected != null) projetTitre = safeStr(selected.getTitre());
        }
        if (projetTitre.isBlank()) projetTitre = "notre projet";

        // anti double-envoi
        String lockKey = t.getId() + "|" + evalNorm + "|" + to;
        if (!mailLocks.add(lockKey)) {
            showWarning("Email déjà en cours d’envoi...");
            return;
        }

        final String subject = "Évaluation de votre tâche - " + projetTitre;
        final String body =
                "Bonjour,\n\n" +
                        "Bravo pour votre proposition de tâche dans notre projet : \"" + projetTitre + "\".\n" +
                        "Cordialement,\n" +
                        "L'équipe SkillSwap";

        mailExecutor.submit(() -> {
            try {
                MailService.send(to, subject, body);
                Platform.runLater(() -> showInfo("✅ Email envoyé à " + to));
            } catch (Exception ex) {
                Platform.runLater(() -> showError("❌ Erreur email : " + ex.getMessage()));
                ex.printStackTrace();
            } finally {
                mailLocks.remove(lockKey);
            }
        });
    }

    // =================
    // LOAD DATA
    // =================
    private void chargerProjets() {
        try {
            // ✅ Afficher TOUS les projets (pas seulement les tiens)
            List<Projet> projets = projetService.afficherTous(); // remplace afficherParUtilisateur
            tableProjet.setItems(FXCollections.observableArrayList(projets));
        } catch (SQLException e) {
            showError("Erreur chargement projets : " + e.getMessage());
        }
    }

    private void chargerTaches(int projetId) {
        try {
            List<Tache> taches = tacheService.afficherParProjet(projetId);
            tableTache.setItems(FXCollections.observableArrayList(taches));
        } catch (SQLException e) {
            showError("Erreur chargement tâches : " + e.getMessage());
        }
    }

    // ================= PROJET CRUD =================

    @FXML
    private void modifierProjet() {
        Projet selected = tableProjet.getSelectionModel().getSelectedItem();
        if (selected == null || !isOwnerProjet(selected)) return;
        if (!validerFormProjet()) return;

        try {
            selected.setTitre(projetTitreField.getText().trim());
            selected.setDescription(emptyToNull(getText(projetDescriptionField)));
            selected.setDateDebut(projetDebutPicker.getValue());
            selected.setDateFin(projetFinPicker.getValue());
            selected.setStatut(projetStatutBox.getValue());

            // ✅ Conserver le mail_user existant
            if (selected.getMailUser() == null || selected.getMailUser().isBlank()) {
                selected.setMailUser(currentUserEmail);
            }

            projetService.modifier(selected);
            chargerProjets();
            clearFormProjet();
            showInfo("Projet modifié avec succès ✅");
            notifyProjetChanged();

        } catch (SQLException e) {
            showError("Erreur modification projet : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerProjet() {
        Projet selected = tableProjet.getSelectionModel().getSelectedItem();
        if (selected == null || !isOwnerProjet(selected)) return;
        if (!confirm("Supprimer le projet",
                "Supprimer ce projet supprimera aussi toutes ses tâches. Continuer ?")) return;

        try {
            projetService.supprimer(selected.getId());
            tableProjet.getItems().remove(selected);

            // ✅ Vider la table des tâches liées
            if (tableTache != null) tableTache.getItems().clear();

            clearFormProjet();
            showInfo("Projet supprimé ✅");
            notifyProjetChanged();

        } catch (SQLException e) {
            showError("Erreur suppression projet : " + e.getMessage());
        }
    }


    @FXML
    private void ajouterTache() {
        Projet projet = tableProjet.getSelectionModel().getSelectedItem();
        if (projet == null) return; // ✅ pas de vérification ownership projet
        if (!validerFormTache()) return;

        try {
            Tache t = new Tache(
                    0,
                    titreField.getText().trim(),
                    emptyToNull(getText(descriptionField)),
                    statutBox.getValue(),
                    prioriteBox.getValue(),
                    echeancePicker.getValue(),
                    projet.getId(),
                    currentUserId
            );
            t.setMailUser(currentUserEmail);
            t.setEvaluation(evaluationBox.getValue());
            tacheService.ajouter(t);

            chargerTaches(projet.getId());
            clearFormTache();
            showInfo("Tâche ajoutée avec succès ✅");

        } catch (SQLException e) {
            showError("Erreur ajout tâche : " + e.getMessage());
        }
    }

    @FXML
    private void modifierTache() {
        Tache selected = tableTache.getSelectionModel().getSelectedItem();
        if (selected == null || !isOwnerTache(selected)) return; // ✅ ma tâche, peu importe le projet
        if (!validerFormTache()) return;

        try {
            selected.setTitre(titreField.getText().trim());
            selected.setDescription(emptyToNull(getText(descriptionField)));
            selected.setStatut(statutBox.getValue());
            selected.setPriorite(prioriteBox.getValue());
            selected.setEcheance(echeancePicker.getValue());
            selected.setEvaluation(evaluationBox.getValue());

            if (selected.getMailUser() == null || selected.getMailUser().isBlank()) {
                selected.setMailUser(currentUserEmail);
            }

            tacheService.modifier(selected);
            chargerTaches(selected.getProjetId());
            clearFormTache();
            showInfo("Tâche modifiée avec succès ✅");

        } catch (SQLException e) {
            showError("Erreur modification tâche : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerTache() {
        Tache selected = tableTache.getSelectionModel().getSelectedItem();
        if (selected == null || !isOwnerTache(selected)) return; // ✅ ma tâche seulement
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
    private void ajouterProjet() {
        if (!validerFormProjet()) return;

        // ✅ Debug userId au moment de l'insertion
        System.out.println("🔍 ajouterProjet() → currentUserId=" + currentUserId
                + " | email=" + currentUserEmail);

        try {
            Projet p = new Projet(
                    0,
                    projetTitreField.getText().trim(),
                    emptyToNull(getText(projetDescriptionField)),
                    projetDebutPicker.getValue(),
                    projetFinPicker.getValue(),
                    projetStatutBox.getValue(),
                    currentUserId
            );
            p.setMailUser(currentUserEmail);

            projetService.ajouter(p);
            System.out.println("✅ Projet inséré avec user_id=" + currentUserId);

            chargerProjets();
            clearFormProjet();
            showInfo("Projet ajouté avec succès ✅");
            notifyProjetChanged();

        } catch (SQLException e) {
            showError("Erreur ajout projet : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void viderFormProjet() {
        clearFormProjet();
    }

    private void remplirFormProjet(Projet p) {
        if (p == null) return;

        projetTitreField.setText(safeStr(p.getTitre()));
        projetDescriptionField.setText(p.getDescription() == null ? "" : p.getDescription());
        projetDebutPicker.setValue(p.getDateDebut());
        projetFinPicker.setValue(p.getDateFin());
        projetStatutBox.setValue(p.getStatut());
    }

    // ================= TACHE CRUD =================
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

            org.model.Utilisateurs.utilisateur user = Session.getCurrentUser();

            // ✅ Fallback : reconstruire depuis SessionContext si Session est vide
            if (user == null && currentUserId > 0) {
                org.service.utilisateur.UtilisateurService us =
                        new org.service.utilisateur.UtilisateurService();
                user = us.findById(currentUserId);
                if (user != null) Session.setCurrentUser(user);
            }

            if (user != null) {
                controller.setUser(user); // ✅ recharge projets, tâches, etc.
            } else {
                System.out.println("❌ goProfil : aucun utilisateur en session");
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
    private void viderForm() {
        clearFormTache();
    }

    private void remplirFormTacheDepuisSelection() {
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
        evaluationBox.setValue(selected.getEvaluation());
    }

    // ================= VALIDATION =================
    private boolean validerFormProjet() {
        String titre = projetTitreField.getText() == null ? "" : projetTitreField.getText().trim();

        if (titre.isEmpty()) {
            showWarning("Le titre du projet est obligatoire.");
            return false;
        }
        if (titre.length() < 3) {
            showWarning("Le titre du projet doit contenir au moins 3 caractères.");
            return false;
        }
        if (projetStatutBox.getValue() == null) {
            showWarning("Veuillez choisir un statut du projet.");
            return false;
        }

        LocalDate d1 = projetDebutPicker.getValue();
        LocalDate d2 = projetFinPicker.getValue();
        if (d1 != null && d2 != null && d2.isBefore(d1)) {
            showWarning("La date de fin ne peut pas être avant la date de début.");
            return false;
        }
        return true;
    }

    private boolean validerFormTache() {
        String titre = titreField.getText() == null ? "" : titreField.getText().trim();

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

    // ================= UTIL =================
    private void clearFormProjet() {
        projetTitreField.clear();
        projetDescriptionField.clear();
        projetDebutPicker.setValue(null);
        projetFinPicker.setValue(null);
        projetStatutBox.setValue(null);
    }

    private void clearFormTache() {
        titreField.clear();
        descriptionField.clear();
        statutBox.setValue(null);
        prioriteBox.setValue(null);
        echeancePicker.setValue(null);
        if (evaluationBox != null) evaluationBox.setValue(null);
    }

    private String getText(TextArea area) { return area == null ? "" : area.getText(); }
    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private String safeStr(String s) { return s == null ? "" : s; }

    private static String normalize(String s) {
        if (s == null) return "";
        String lower = s.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    // ================= ALERTS =================
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
        if (cssUrl != null) dp.getStylesheets().add(cssUrl.toExternalForm());
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
    // Ajouter ce champ en haut de la classe
    private Runnable onProjetChangedCallback;

    // Ajouter ce setter
    public void setOnProjetChangedCallback(Runnable callback) {
        this.onProjetChangedCallback = callback;
    }

    // Méthode utilitaire à appeler après chaque CRUD
    private void notifyProjetChanged() {
        if (onProjetChangedCallback != null) {
            onProjetChangedCallback.run();
        }
    }
    private boolean isOwnerProjet(Projet p) {
        if (p == null) return false;
        return p.getUserId() == currentUserId;
        // ou fallback mail : safeStr(p.getMailUser()).equalsIgnoreCase(currentUserEmail)
    }

    private boolean isOwnerTache(Tache t) {
        if (t == null) return false;
        // ✅ Je peux modifier/supprimer si le PROJET m'appartient
        Projet projetSelectionne = tableProjet == null ? null
                : tableProjet.getSelectionModel().getSelectedItem();
        return isOwnerProjet(projetSelectionne);
    }
    private void refreshProjetButtons() {
        Projet selected = tableProjet == null ? null
                : tableProjet.getSelectionModel().getSelectedItem();
        boolean owner = isOwnerProjet(selected);

        if (btnModifierProjet != null) btnModifierProjet.setDisable(selected == null || !owner);
        if (btnSupprimerProjet != null) btnSupprimerProjet.setDisable(selected == null || !owner);
        // ✅ btnAjouterTache toujours actif si un projet est sélectionné
        if (btnAjouterTache != null) btnAjouterTache.setDisable(selected == null);
    }
    private void refreshTacheButtons() {
        Tache selected = tableTache == null ? null
                : tableTache.getSelectionModel().getSelectedItem();
        boolean ownerTache = isOwnerTache(selected);

        if (btnModifierTache != null) btnModifierTache.setDisable(selected == null || !ownerTache);
        if (btnSupprimerTache != null) btnSupprimerTache.setDisable(selected == null || !ownerTache);
    }
}