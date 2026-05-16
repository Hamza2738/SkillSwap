package org.controller.Offres.back;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.model.Offres.Candidature;
import org.model.Offres.Commentaire;
import org.model.Offres.Offre;
import org.service.Offres.OffreService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class OffreBackController {

    // ── Formulaire ─────────────────────────────────────────────
    @FXML private TextField        tfTitre;
    @FXML private TextArea         taDescription;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField        tfBudget;
    @FXML private TextField        tfPrix;
    @FXML private TextField        tfDuree;
    @FXML private TextField        tfLocalisation;
    @FXML private DatePicker       dpLimite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField        tfTags;
    @FXML private Label            lblMsg;
    @FXML private Label            lblMatricule;
    @FXML private Label            lblCreateur;

    // ── Table Offres ───────────────────────────────────────────
    @FXML private TableView<Offre>          tableOffres;
    @FXML private TableColumn<Offre,String> colMatricule;
    @FXML private TableColumn<Offre,String> colTitre;
    @FXML private TableColumn<Offre,String> colType;
    @FXML private TableColumn<Offre,String> colPrix;
    @FXML private TableColumn<Offre,String> colStatut;
    @FXML private TableColumn<Offre,String> colVues;
    @FXML private TableColumn<Offre,String> colLimite;
    // ✅ Nouvelles colonnes likes & commentaires
    @FXML private TableColumn<Offre,String> colLikes;
    @FXML private TableColumn<Offre,String> colCommentaires;
    @FXML private TableColumn<Offre,String> colActions;

    // ── Table Candidatures ─────────────────────────────────────
    @FXML private TableView<Candidature>           tableCandidatures;
    @FXML private TableColumn<Candidature,String>  cColMatricule;
    @FXML private TableColumn<Candidature,String>  cColCandidat;
    @FXML private TableColumn<Candidature,String>  cColMail;
    @FXML private TableColumn<Candidature,String>  cColOffre;
    @FXML private TableColumn<Candidature,String>  cColPrix;
    @FXML private TableColumn<Candidature,String>  cColPaiement;
    @FXML private TableColumn<Candidature,String>  cColRef;
    @FXML private TableColumn<Candidature,String>  cColAbonnement;
    @FXML private TableColumn<Candidature,String>  cColExpiration;
    @FXML private TableColumn<Candidature,String>  cColStatut;
    @FXML private TableColumn<Candidature,String>  cColActif;
    @FXML private TableColumn<Candidature,String>  cColActions;

    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreType;
    @FXML private Label            lblStats;

    private final OffreService                offreService     = new OffreService();
    private final ObservableList<Offre>       offresData       = FXCollections.observableArrayList();
    private final ObservableList<Candidature> candidaturesData = FXCollections.observableArrayList();
    private static final String SMTP_USER = "skillswapskillswap@gmail.com";
    private static final String SMTP_PASS = "rhmq cwok iwsj jbru";
    private Offre selectedOffre = null;

    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private org.model.Utilisateurs.utilisateur currentUser;

    // ════════════════════════════════════════════════════════════
    //  SET USER
    // ════════════════════════════════════════════════════════════
    public void setUser(org.model.Utilisateurs.utilisateur u) {
        this.currentUser = u;
        if (u != null && lblCreateur != null) {
            lblCreateur.setText("👤 Créateur : " + nvl(u.getEmail()));
        }
    }

    // ════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        initCombos();
        initTableOffres();
        initTableCandidatures();
        chargerOffres();
    }

    private void initCombos() {
        if (cbType       != null) cbType.setItems(FXCollections.observableArrayList("emploi", "projet", "tache"));
        if (cbStatut     != null) cbStatut.setItems(FXCollections.observableArrayList("ouverte", "fermee"));
        if (cbFiltreType != null) cbFiltreType.setItems(FXCollections.observableArrayList("Tous", "emploi", "projet", "tache"));
    }

    // ════════════════════════════════════════════════════════════
    //  TABLE OFFRES
    // ════════════════════════════════════════════════════════════
    private void initTableOffres() {
        if (tableOffres == null) return;

        if (colMatricule != null) colMatricule.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getMatricule())));
        if (colTitre != null) colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getTitre())));
        if (colType != null) colType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypeIcon() + " " + nvl(d.getValue().getTypeOffre())));
        if (colPrix != null) colPrix.setCellValueFactory(d ->
                new SimpleStringProperty(formatPrix(d.getValue().getPrix())));
        if (colStatut != null) colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getStatut())));
        if (colVues != null) colVues.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getVues())));
        if (colLimite != null) colLimite.setCellValueFactory(d -> {
            LocalDate dl = d.getValue().getDateLimite();
            return new SimpleStringProperty(dl != null ? dl.format(D_FMT) : "—");
        });

        // ✅ Colonne Likes
        if (colLikes != null) {
            colLikes.setCellValueFactory(d ->
                    new SimpleStringProperty("❤️ " + d.getValue().getNbLikes()));
            colLikes.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item);
                    setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            });
        }

        // ✅ Colonne Commentaires (cliquable pour voir la liste)
        if (colCommentaires != null) {
            colCommentaires.setCellValueFactory(d ->
                    new SimpleStringProperty("💬 " + d.getValue().getNbCommentaires()));
            colCommentaires.setCellFactory(col -> new TableCell<>() {
                final Button btnVoir = new Button();
                {
                    btnVoir.setStyle("""
                        -fx-background-color: #0d9488;
                        -fx-text-fill: white; -fx-font-size: 11px;
                        -fx-background-radius: 6; -fx-padding: 3 8; -fx-cursor: hand;
                        """);
                    btnVoir.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx < 0 || getTableView() == null) return;
                        var items = getTableView().getItems();
                        if (items == null || idx >= items.size()) return;
                        ouvrirVoirCommentaires(items.get(idx));
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    btnVoir.setText(item);
                    setGraphic(btnVoir);
                    setText(null);
                }
            });
        }

        // Colonne Actions
        if (colActions != null) {
            colActions.setCellFactory(col -> new TableCell<>() {
                final Button btnEdit   = new Button("✏️");
                final Button btnDelete = new Button("🗑️");
                final Button btnCands  = new Button("👥");
                final HBox   box       = new HBox(4, btnEdit, btnDelete, btnCands);
                {
                    styleBtn(btnEdit,   "#3b5bdb");
                    styleBtn(btnDelete, "#ef4444");
                    styleBtn(btnCands,  "#0d9488");
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Offre o = getTableView().getItems().get(getIndex());
                    btnEdit.setOnAction(e   -> chargerDansFormulaire(o));
                    btnDelete.setOnAction(e -> supprimerOffre(o));
                    btnCands.setOnAction(e  -> chargerCandidaturesOffre(o));
                    setGraphic(box);
                }
            });
        }

        tableOffres.setItems(offresData);
        tableOffres.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if (n != null) chargerDansFormulaire(n);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  DIALOG COMMENTAIRES (côté BACK — lecture + suppression)
    // ════════════════════════════════════════════════════════════
    private void ouvrirVoirCommentaires(Offre offre) {
        Stage dialog = new Stage();
        dialog.setTitle("💬 Commentaires — " + offre.getTitre());

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: #0f172a; -fx-padding: 22;");
        root.setPrefWidth(560);

        Label titre = new Label("💬 Commentaires de : " + offre.getTitre()
                + " (" + offre.getNbCommentaires() + ")");
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        titre.setWrapText(true);

        VBox commentairesBox = new VBox(10);
        commentairesBox.setStyle("-fx-padding: 4;");

        ScrollPane scroll = new ScrollPane(commentairesBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color: #0d1220; -fx-background: #0d1220;");

        // Charger les commentaires
        chargerCommentairesBack(offre, commentairesBox, dialog);

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle("""
            -fx-background-color: #334155; -fx-text-fill: white;
            -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;
            """);
        btnFermer.setOnAction(e -> dialog.close());

        root.getChildren().addAll(titre, scroll, btnFermer);

        ScrollPane rootScroll = new ScrollPane(root);
        rootScroll.setFitToWidth(true);
        rootScroll.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a;");
        dialog.setScene(new Scene(rootScroll, 580, 520));
        dialog.show();
    }

    /** Charge les commentaires dans la VBox du back-office, avec bouton supprimer */
    private void chargerCommentairesBack(Offre offre, VBox container, Stage parentDialog) {
        container.getChildren().clear();
        try {
            List<Commentaire> comments = offreService.getCommentairesParOffre(offre.getIdOffre());
            if (comments.isEmpty()) {
                Label vide = new Label("Aucun commentaire pour cette offre.");
                vide.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-padding: 10;");
                container.getChildren().add(vide);
                return;
            }
            for (Commentaire c : comments) {
                VBox bulle = buildBulleCommentaireBack(c, offre, container, parentDialog);
                container.getChildren().add(bulle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Erreur chargement commentaires : " + e.getMessage());
            err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            container.getChildren().add(err);
        }
    }

    private VBox buildBulleCommentaireBack(Commentaire c, Offre offre,
                                           VBox container, Stage parentDialog) {
        VBox bulle = new VBox(5);
        bulle.setStyle("""
            -fx-background-color: #1e293b;
            -fx-background-radius: 10;
            -fx-padding: 10 14;
            -fx-border-color: #334155;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            """);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 14px;");
        Label auteur = new Label(c.getAuteurLabel().isBlank() ? "Utilisateur #" + c.getIdUtilisateur() : c.getAuteurLabel());
        auteur.setStyle("-fx-text-fill: #6c8cff; -fx-font-weight: bold; -fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(c.getDateLabel());
        date.setStyle("-fx-text-fill: #475569; -fx-font-size: 10px;");

        // ✅ Bouton supprimer (admin)
        Button btnSupp = new Button("🗑️");
        btnSupp.setStyle("""
            -fx-background-color: #ef4444; -fx-text-fill: white;
            -fx-font-size: 10px; -fx-background-radius: 6;
            -fx-padding: 2 6; -fx-cursor: hand;
            """);
        btnSupp.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Supprimer ce commentaire ?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirmation");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    offreService.supprimerCommentaire(c.getIdCommentaire(), offre.getIdOffre());
                    offre.setNbCommentaires(Math.max(0, offre.getNbCommentaires() - 1));
                    showMsg("Commentaire supprimé ✅", true);
                    chargerOffres(); // rafraîchir le compteur dans la table
                    chargerCommentairesBack(offre, container, parentDialog);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showMsg("Erreur suppression : " + ex.getMessage(), false);
                }
            }
        });

        meta.getChildren().addAll(avatar, auteur, spacer, date, btnSupp);

        Label contenu = new Label(c.getContenu());
        contenu.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        contenu.setWrapText(true);

        bulle.getChildren().addAll(meta, contenu);
        return bulle;
    }

    // ════════════════════════════════════════════════════════════
    //  TABLE CANDIDATURES
    // ════════════════════════════════════════════════════════════
    private void initTableCandidatures() {
        if (tableCandidatures == null) return;

        if (cColMatricule  != null) cColMatricule.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getMatriculeOffre())));
        if (cColCandidat   != null) cColCandidat.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getNomCandidat())
                        + " " + nvl(d.getValue().getPrenomCandidat())));
        if (cColMail       != null) cColMail.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getMailAcheteur())));
        if (cColOffre      != null) cColOffre.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getTitreOffre())));
        if (cColPrix       != null) cColPrix.setCellValueFactory(d ->
                new SimpleStringProperty(formatPrix(d.getValue().getPrixPaye())));
        if (cColPaiement   != null) cColPaiement.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getModePaiement())));
        if (cColRef        != null) cColRef.setCellValueFactory(d ->
                new SimpleStringProperty(nvl(d.getValue().getReferencePaiement())));
        if (cColAbonnement != null) cColAbonnement.setCellValueFactory(d -> {
            var da = d.getValue().getDateAbonnement();
            return new SimpleStringProperty(da != null ? da.format(FMT) : "—");
        });
        if (cColExpiration != null) cColExpiration.setCellValueFactory(d -> {
            var de = d.getValue().getDateExpiration();
            return new SimpleStringProperty(de != null ? de.format(FMT) : "—");
        });
        if (cColStatut != null) cColStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatutLabel()));
        if (cColActif  != null) cColActif.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAbonnementLabel()));

        if (cColActions != null) {
            cColActions.setCellFactory(col -> new TableCell<Candidature, String>() {

                private final Button btnAccept = new Button("✅");
                private final Button btnRefuse = new Button("❌");
                private final Button btnNote   = new Button("📝");
                private final HBox   box       = new HBox(4, btnAccept, btnRefuse, btnNote);

                {
                    styleBtn(btnAccept, "#22c55e");
                    styleBtn(btnRefuse, "#ef4444");
                    styleBtn(btnNote,   "#f59e0b");

                    btnAccept.setOnAction(e -> {
                        Candidature c = getCandidatureCourante();
                        if (c == null) return;
                        changerStatutCandidature(c, "acceptee");
                    });

                    btnRefuse.setOnAction(e -> {
                        Candidature c = getCandidatureCourante();
                        if (c == null) return;
                        changerStatutCandidature(c, "refusee");
                    });

                    btnNote.setOnAction(e -> {
                        Candidature c = getCandidatureCourante();
                        if (c == null) return;
                        ouvrirDialogNote(c);
                    });
                }

                private Candidature getCandidatureCourante() {
                    int idx = getIndex();
                    if (idx < 0 || getTableView() == null) return null;
                    var items = getTableView().getItems();
                    if (items == null || idx >= items.size()) return null;
                    return items.get(idx);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() < 0
                            || getTableView() == null
                            || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }
                    Candidature c = getCandidatureCourante();
                    if (c != null) {
                        String statut = nvl(c.getStatut());
                        btnAccept.setDisable("acceptee".equals(statut));
                        btnRefuse.setDisable("refusee".equals(statut));
                        btnAccept.setOpacity("acceptee".equals(statut) ? 0.4 : 1.0);
                        btnRefuse.setOpacity("refusee".equals(statut) ? 0.4 : 1.0);
                    }
                    setGraphic(box);
                    setText(null);
                }
            });
        }

        tableCandidatures.setItems(candidaturesData);
    }

    // ════════════════════════════════════════════════════════════
    //  CHARGEMENT DONNÉES
    // ════════════════════════════════════════════════════════════
    private void chargerOffres() {
        try {
            offresData.setAll(offreService.afficherToutes());
            majStats();
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur chargement : " + e.getMessage(), false);
        }
    }

    private void chargerCandidaturesOffre(Offre o) {
        selectedOffre = o;
        try {
            candidaturesData.setAll(offreService.getCandidaturesParOffre(o.getIdOffre()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void majStats() {
        if (lblStats == null) return;
        long ouvertes = offresData.stream().filter(Offre::isOuverte).count();
        long totalLikes = offresData.stream().mapToLong(Offre::getNbLikes).sum();
        long totalComm  = offresData.stream().mapToLong(Offre::getNbCommentaires).sum();
        long total      = offresData.size();
        lblStats.setText("📊 " + total + " offres · " + ouvertes + " ouvertes · ❤️ " + totalLikes + " · 💬 " + totalComm);
    }

    // ════════════════════════════════════════════════════════════
    //  FORMULAIRE CRUD
    // ════════════════════════════════════════════════════════════
    private void chargerDansFormulaire(Offre o) {
        selectedOffre = o;
        if (tfTitre        != null) tfTitre.setText(nvl(o.getTitre()));
        if (taDescription  != null) taDescription.setText(nvl(o.getDescription()));
        if (cbType         != null) cbType.setValue(o.getTypeOffre());
        if (tfBudget       != null) tfBudget.setText(o.getBudget() != null ? o.getBudget().toPlainString() : "");
        if (tfPrix         != null) tfPrix.setText(o.getPrix() != null ? o.getPrix().toPlainString() : "");
        if (tfDuree        != null) tfDuree.setText(String.valueOf(o.getDuree()));
        if (tfLocalisation != null) tfLocalisation.setText(nvl(o.getLocalisation()));
        if (dpLimite       != null) dpLimite.setValue(o.getDateLimite());
        if (cbStatut       != null) cbStatut.setValue(o.getStatut());
        if (tfTags         != null) tfTags.setText(nvl(o.getTags()));
        if (lblMatricule   != null) lblMatricule.setText("Matricule : " + nvl(o.getMatricule()));
        if (lblMsg         != null) lblMsg.setText("");
    }

    @FXML
    private void onAjouter() {
        try {
            Offre o = buildOffreFromForm();
            if (currentUser != null) o.setIdUtilisateur(currentUser.getId_utilisateur());
            offreService.ajouter(o);
            showMsg("Offre ajoutée ✅  — " + o.getMatricule(), true);
            chargerOffres();
            viderFormulaire();
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void onModifier() {
        if (selectedOffre == null) { showMsg("Sélectionnez une offre.", false); return; }
        try {
            Offre o = buildOffreFromForm();
            o.setIdOffre(selectedOffre.getIdOffre());
            o.setMatricule(selectedOffre.getMatricule());
            if (currentUser != null) o.setIdUtilisateur(currentUser.getId_utilisateur());
            offreService.modifier(o);
            showMsg("Offre modifiée ✅", true);
            chargerOffres();
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void onVider() {
        viderFormulaire();
        selectedOffre = null;
    }

    @FXML
    private void onRechercher() {
        try {
            String q = tfRecherche  != null ? tfRecherche.getText().trim() : "";
            String t = cbFiltreType != null ? cbFiltreType.getValue()       : "Tous";
            List<Offre> result = q.length() >= 2
                    ? offreService.rechercherParTitre(q)
                    : offreService.afficherToutes();
            if (!"Tous".equals(t) && t != null)
                result = result.stream().filter(o -> t.equals(o.getTypeOffre())).toList();
            offresData.setAll(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void supprimerOffre(Offre o) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'offre « " + o.getTitre() + " » ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                offreService.supprimer(o.getIdOffre());
                chargerOffres();
                showMsg("Offre supprimée.", true);
            } catch (Exception e) {
                showMsg("Erreur : " + e.getMessage(), false);
            }
        }
    }

    private void changerStatutCandidature(Candidature c, String statut) {
        try {
            offreService.changerStatutCandidature(c.getIdCandidature(), statut);
            showMsg("Statut mis à jour : " + statut + " ✅", true);
            envoyerMailCandidature(c, statut);
            if (selectedOffre != null) chargerCandidaturesOffre(selectedOffre);
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur changement statut : " + e.getMessage(), false);
        }
    }

    private void ouvrirDialogNote(Candidature c) {
        TextInputDialog dialog = new TextInputDialog(nvl(c.getNoteRecruteur()));
        dialog.setTitle("Note recruteur");
        dialog.setHeaderText("Candidat : "
                + nvl(c.getNomCandidat()) + " " + nvl(c.getPrenomCandidat()));
        dialog.setContentText("Note / Commentaire :");

        DialogPane dp = dialog.getDialogPane();
        var css = getClass().getResource("/view/css/suiviTache/back/style.css");
        if (css != null) dp.getStylesheets().add(css.toExternalForm());

        dialog.showAndWait().ifPresent(note -> {
            if (note.isBlank()) return;
            try {
                offreService.ajouterNoteRecruteur(c.getIdCandidature(), note);
                showMsg("Note enregistrée ✅", true);
                if (selectedOffre != null) chargerCandidaturesOffre(selectedOffre);
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Erreur note : " + e.getMessage(), false);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════
    private Offre buildOffreFromForm() {
        Offre o = new Offre();
        if (tfTitre        != null) o.setTitre(tfTitre.getText().trim());
        if (taDescription  != null) o.setDescription(taDescription.getText().trim());
        if (cbType         != null) o.setTypeOffre(cbType.getValue());
        if (tfBudget       != null && !tfBudget.getText().isBlank())
            o.setBudget(new BigDecimal(tfBudget.getText().trim()));
        if (tfPrix         != null && !tfPrix.getText().isBlank())
            o.setPrix(new BigDecimal(tfPrix.getText().trim()));
        else
            o.setPrix(BigDecimal.ZERO);
        if (tfDuree        != null && !tfDuree.getText().isBlank())
            o.setDuree(Integer.parseInt(tfDuree.getText().trim()));
        if (tfLocalisation != null) o.setLocalisation(tfLocalisation.getText().trim());
        if (dpLimite       != null) o.setDateLimite(dpLimite.getValue());
        if (cbStatut       != null) o.setStatut(cbStatut.getValue());
        if (tfTags         != null) o.setTags(tfTags.getText().trim());
        o.setDatePublication(LocalDate.now());
        return o;
    }

    private void viderFormulaire() {
        if (tfTitre        != null) tfTitre.clear();
        if (taDescription  != null) taDescription.clear();
        if (cbType         != null) cbType.setValue(null);
        if (tfBudget       != null) tfBudget.clear();
        if (tfPrix         != null) tfPrix.clear();
        if (tfDuree        != null) tfDuree.clear();
        if (tfLocalisation != null) tfLocalisation.clear();
        if (dpLimite       != null) dpLimite.setValue(null);
        if (cbStatut       != null) cbStatut.setValue("ouverte");
        if (tfTags         != null) tfTags.clear();
        if (lblMatricule   != null) lblMatricule.setText("Matricule : (auto)");
        if (lblMsg         != null) lblMsg.setText("");
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/utilisateur/profilAdmin.fxml"));
            Parent root = loader.load();
            org.controller.utilisateur.ProfilAdminController ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setUser(currentUser);
            Scene scene = new Scene(root, 1920, 1000);
            var css = getClass().getResource("/view/css/utilisateur/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SkillSwap - Admin");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur retour : " + e.getMessage(), false);
        }
    }

    private void showMsg(String msg, boolean ok) {
        if (lblMsg == null) return;
        lblMsg.setStyle("-fx-text-fill: " + (ok ? "#22c55e" : "#ef4444") + "; -fx-font-size: 12px;");
        lblMsg.setText(msg);
    }

    private void styleBtn(Button b, String color) {
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11px;");
    }

    private String formatPrix(BigDecimal p) {
        if (p == null) return "Gratuit";
        return p.compareTo(BigDecimal.ZERO) == 0 ? "Gratuit" : p.toPlainString() + " TND";
    }

    private void envoyerMailCandidature(Candidature c, String statut) {
        String to = nvl(c.getMailAcheteur()).trim();
        if (to.isEmpty()) return;

        String nomCandidat  = nvl(c.getNomCandidat()) + " " + nvl(c.getPrenomCandidat());
        String titreOffre   = nvl(c.getTitreOffre());
        String matricule    = nvl(c.getMatriculeOffre());
        boolean accepte = "acceptee".equals(statut);

        String subject = accepte
                ? "✅ Votre candidature a été acceptée — " + titreOffre
                : "❌ Votre candidature a été refusée — " + titreOffre;

        String body = accepte
                ? "Bonjour " + nomCandidat + ",\n\nNous avons le plaisir de vous informer que votre candidature pour l'offre :\n"
                + "  📌 " + titreOffre + " (" + matricule + ")\n\na été ACCEPTÉE ✅.\n\n"
                + "Notre équipe vous contactera prochainement.\n\nCordialement,\nL'équipe SkillSwap"
                : "Bonjour " + nomCandidat + ",\n\nNous avons examiné votre candidature pour l'offre :\n"
                + "  📌 " + titreOffre + " (" + matricule + ")\n\nElle n'a pas été retenue ❌.\n\n"
                + "Bonne chance dans vos recherches.\n\nCordialement,\nL'équipe SkillSwap";

        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            "smtp.gmail.com");
                props.put("mail.smtp.port",            "587");
                props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USER, "SkillSwap"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
                javafx.application.Platform.runLater(() ->
                        showMsg("📧 Email envoyé à " + to + " ✅", true));
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showMsg("⚠️ Mail non envoyé : " + ex.getMessage(), false));
            }
        }, "mail-offre-sender").start();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}