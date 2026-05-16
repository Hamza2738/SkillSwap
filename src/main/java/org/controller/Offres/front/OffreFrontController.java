package org.controller.Offres.front;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.model.Offres.Candidature;
import org.model.Offres.Commentaire;
import org.model.Offres.Offre;
import org.model.Utilisateurs.utilisateur;
import org.service.Offres.OffreService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OffreFrontController {

    // ── Filtres ────────────────────────────────────────────────
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbTypeFiltre;
    @FXML private Button           btnFiltrer;
    @FXML private Button           btnReset;

    // ── Contenu ────────────────────────────────────────────────
    @FXML private FlowPane         offresContainer;
    @FXML private Label            lblResultats;

    // ── Mes abonnements ────────────────────────────────────────
    @FXML private VBox             mesAbonnementsBox;
    @FXML private Label            lblAucunAbonnement;

    private utilisateur            currentUser;
    private final OffreService     offreService = new OffreService();
    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Gemini ─────────────────────────────────────────────────
    private static final String GEMINI_API_KEY =
            "AIzaSyBI6im6aifWh7Fq3eRim7e6sC8QiN5L9hQ";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + GEMINI_API_KEY;

    // ════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        if (cbTypeFiltre != null) cbTypeFiltre.setItems(FXCollections.observableArrayList(
                "Tous", "emploi", "projet", "tache"));
    }

    public void setUser(utilisateur u) {
        this.currentUser = u;
        chargerOffres();
        chargerMesAbonnements();
    }

    // ════════════════════════════════════════════════════════════
    //  CHARGEMENT OFFRES
    // ════════════════════════════════════════════════════════════
    private void chargerOffres() {
        try {
            List<Offre> offres = offreService.afficherOuvertes();
            // Marquer les offres likées par l'utilisateur courant
            if (currentUser != null) {
                for (Offre o : offres) {
                    try {
                        o.setLikedByCurrentUser(
                                offreService.aDejaLike(o.getIdOffre(), currentUser.getId_utilisateur()));
                    } catch (Exception ignored) {}
                }
            }
            afficherOffres(offres);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onFiltrer() {
        try {
            String q    = tfRecherche  != null ? tfRecherche.getText().trim() : "";
            String type = cbTypeFiltre != null ? cbTypeFiltre.getValue()       : "Tous";

            List<Offre> offres = q.length() >= 2
                    ? offreService.rechercherParTitre(q)
                    : offreService.afficherOuvertes();

            offres = offres.stream()
                    .filter(o -> "Tous".equals(type) || type == null || type.equals(o.getTypeOffre()))
                    .toList();

            if (currentUser != null) {
                for (Offre o : offres) {
                    try {
                        o.setLikedByCurrentUser(
                                offreService.aDejaLike(o.getIdOffre(), currentUser.getId_utilisateur()));
                    } catch (Exception ignored) {}
                }
            }
            afficherOffres(offres);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onReset() {
        if (tfRecherche  != null) tfRecherche.clear();
        if (cbTypeFiltre != null) cbTypeFiltre.setValue("Tous");
        chargerOffres();
    }

    private void afficherOffres(List<Offre> offres) {
        if (offresContainer == null) return;
        offresContainer.getChildren().clear();
        if (lblResultats != null)
            lblResultats.setText(offres.size() + " offre(s) trouvée(s)");
        for (Offre o : offres)
            offresContainer.getChildren().add(buildCarteOffre(o));
    }

    // ════════════════════════════════════════════════════════════
    //  CARTE OFFRE  (avec ❤️ Like + 💬 Commentaires)
    // ════════════════════════════════════════════════════════════
    private VBox buildCarteOffre(Offre o) {
        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setStyle("""
            -fx-background-color: #1e293b;
            -fx-background-radius: 14;
            -fx-padding: 18;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.45),10,0.3,0,4);
            -fx-cursor: hand;
            """);

        // ── Header ──────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(o.getTypeIcon());
        icon.setStyle("-fx-font-size: 26px;");

        VBox titreBox = new VBox(2);
        Label titre = new Label(o.getTitre());
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        titre.setWrapText(true);
        titre.setMaxWidth(240);
        Label matricule = new Label(o.getMatricule());
        matricule.setStyle("-fx-text-fill: #6c8cff; -fx-font-size: 10px;");
        titreBox.getChildren().addAll(titre, matricule);
        header.getChildren().addAll(icon, titreBox);

        Label badgeType = new Label(o.getTypeOffre() != null ? o.getTypeOffre().toUpperCase() : "");
        badgeType.setStyle("""
            -fx-background-color: #3b5bdb;
            -fx-text-fill: white; -fx-font-size: 10px;
            -fx-background-radius: 20; -fx-padding: 2 10;
            """);
        HBox badgeRow = new HBox(6, badgeType);

        // ── Infos ───────────────────────────────────────────────
        VBox infos = new VBox(5);
        infos.getChildren().addAll(
                infoLigne("💰 Prix",   formatPrix(o.getPrix())),
                infoLigne("⏱ Durée",  o.getDuree() + " jours"),
                infoLigne("📍 Lieu",   nvl(o.getLocalisation())),
                infoLigne("📅 Limite", o.getDateLimite() != null ? o.getDateLimite().format(D_FMT) : "—")
        );

        Label desc = new Label(truncate(nvl(o.getDescription()), 100));
        desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        desc.setWrapText(true);

        if (o.getTags() != null && !o.getTags().isBlank()) {
            Label tags = new Label("🏷 " + o.getTags());
            tags.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px;");
            tags.setWrapText(true);
            infos.getChildren().add(tags);
        }

        // ── Barre Likes + Commentaires ───────────────────────────
        HBox socialBar = buildSocialBar(o);

        // ── Bouton Postuler ──────────────────────────────────────
        Button btnPostuler = new Button("✅  Postuler / S'abonner");
        btnPostuler.setMaxWidth(Double.MAX_VALUE);
        btnPostuler.setStyle("""
            -fx-background-color: #3b5bdb;
            -fx-text-fill: white; -fx-font-size: 13px;
            -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;
            """);
        btnPostuler.setOnAction(e -> ouvrirModalPostuler(o));
        btnPostuler.setOnMouseEntered(e -> btnPostuler.setStyle(
                btnPostuler.getStyle().replace("#3b5bdb", "#2563eb")));
        btnPostuler.setOnMouseExited(e  -> btnPostuler.setStyle(
                btnPostuler.getStyle().replace("#2563eb", "#3b5bdb")));

        if (currentUser != null) {
            try {
                if (offreService.dejaCandidat(currentUser.getId_utilisateur(), o.getIdOffre())) {
                    btnPostuler.setText("✔  Déjà souscrit");
                    btnPostuler.setDisable(true);
                    btnPostuler.setStyle(btnPostuler.getStyle().replace("#3b5bdb", "#334155"));
                }
            } catch (Exception ignored) {}
        }

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("#1e293b", "#273549")));
        card.setOnMouseExited(e  -> card.setStyle(card.getStyle().replace("#273549", "#1e293b")));
        card.setOnMouseClicked(e -> offreService.incrementerVues(o.getIdOffre()));

        card.getChildren().addAll(header, badgeRow, new Separator(), infos, desc, socialBar, btnPostuler);
        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  SOCIAL BAR  ❤️ Like   💬 Commentaires
    // ════════════════════════════════════════════════════════════
    private HBox buildSocialBar(Offre o) {
        // ── Bouton Like ──────────────────────────────────────────
        boolean liked = o.isLikedByCurrentUser();
        Label lblLikeCount = new Label(String.valueOf(o.getNbLikes()));
        lblLikeCount.setStyle("-fx-text-fill: " + (liked ? "#f43f5e" : "#94a3b8") + "; -fx-font-size: 12px;");

        Button btnLike = new Button(liked ? "❤️" : "🤍");
        btnLike.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: %s;
            -fx-border-radius: 20; -fx-background-radius: 20;
            -fx-text-fill: white; -fx-font-size: 14px;
            -fx-padding: 3 10; -fx-cursor: hand;
            """.formatted(liked ? "#f43f5e" : "#475569"));

        btnLike.setOnAction(e -> {
            if (currentUser == null) {
                showAlertInfo("Connexion requise", "Vous devez être connecté pour liker une offre.");
                return;
            }
            try {
                boolean nowLiked = offreService.toggleLike(o.getIdOffre(), currentUser.getId_utilisateur());
                o.setLikedByCurrentUser(nowLiked);
                o.setNbLikes(nowLiked ? o.getNbLikes() + 1 : Math.max(0, o.getNbLikes() - 1));
                // Mettre à jour le bouton
                btnLike.setText(nowLiked ? "❤️" : "🤍");
                String borderColor = nowLiked ? "#f43f5e" : "#475569";
                btnLike.setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: %s;
                    -fx-border-radius: 20; -fx-background-radius: 20;
                    -fx-text-fill: white; -fx-font-size: 14px;
                    -fx-padding: 3 10; -fx-cursor: hand;
                    """.formatted(borderColor));
                lblLikeCount.setText(String.valueOf(o.getNbLikes()));
                lblLikeCount.setStyle("-fx-text-fill: " + (nowLiked ? "#f43f5e" : "#94a3b8") + "; -fx-font-size: 12px;");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox likeBox = new HBox(5, btnLike, lblLikeCount);
        likeBox.setAlignment(Pos.CENTER_LEFT);

        // ── Bouton Commentaires ──────────────────────────────────
        Label lblCommentCount = new Label(o.getNbCommentaires() + " commentaire(s)");
        lblCommentCount.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Button btnCommenter = new Button("💬 Commenter");
        btnCommenter.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: #334155;
            -fx-border-radius: 20; -fx-background-radius: 20;
            -fx-text-fill: #94a3b8; -fx-font-size: 12px;
            -fx-padding: 3 10; -fx-cursor: hand;
            """);
        btnCommenter.setOnMouseEntered(ev ->
                btnCommenter.setStyle(btnCommenter.getStyle().replace("#334155", "#3b5bdb").replace("#94a3b8", "white")));
        btnCommenter.setOnMouseExited(ev ->
                btnCommenter.setStyle(btnCommenter.getStyle().replace("#3b5bdb", "#334155").replace("white", "#94a3b8")));

        btnCommenter.setOnAction(e -> ouvrirModalCommentaires(o, lblCommentCount));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, likeBox, spacer, btnCommenter);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 6 0 2 0;");
        return bar;
    }

    // ════════════════════════════════════════════════════════════
    //  MODAL COMMENTAIRES
    // ════════════════════════════════════════════════════════════
    private void ouvrirModalCommentaires(Offre offre, Label lblCommentCount) {
        Stage modal = new Stage();
        modal.setTitle("💬 Commentaires — " + offre.getTitre());

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: #0f172a; -fx-padding: 22;");
        root.setPrefWidth(500);

        Label titre = new Label("💬 Commentaires — " + offre.getTitre());
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        titre.setWrapText(true);

        // ── Zone d'affichage des commentaires ────────────────────
        VBox commentairesBox = new VBox(10);
        commentairesBox.setStyle("-fx-padding: 4;");
        ScrollPane scrollComm = new ScrollPane(commentairesBox);
        scrollComm.setFitToWidth(true);
        scrollComm.setPrefHeight(320);
        scrollComm.setStyle("-fx-background-color: #0d1220; -fx-background: #0d1220;");

        // Charger les commentaires existants
        chargerCommentaires(offre.getIdOffre(), commentairesBox);

        // ── Zone de saisie d'un nouveau commentaire ──────────────
        Label lblNouv = new Label("Laisser un commentaire :");
        lblNouv.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        TextArea taNouveau = new TextArea();
        taNouveau.setPromptText("Écrivez votre commentaire ici...");
        taNouveau.setPrefRowCount(3);
        taNouveau.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-background-radius: 8;");

        Label lblErreur = new Label("");
        lblErreur.setStyle("-fx-font-size: 12px;");
        lblErreur.setWrapText(true);

        Button btnPublier = new Button("📤 Publier");
        btnPublier.setMaxWidth(Double.MAX_VALUE);
        btnPublier.setStyle("""
            -fx-background-color: #3b5bdb; -fx-text-fill: white;
            -fx-font-size: 13px; -fx-font-weight: bold;
            -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;
            """);

        btnPublier.setOnAction(ev -> {
            if (currentUser == null) {
                lblErreur.setStyle("-fx-text-fill: #ef4444;");
                lblErreur.setText("❌ Vous devez être connecté pour commenter.");
                return;
            }
            String contenu = taNouveau.getText();
            if (contenu == null || contenu.isBlank()) {
                lblErreur.setStyle("-fx-text-fill: #f59e0b;");
                lblErreur.setText("⚠️ Le commentaire ne peut pas être vide.");
                return;
            }
            try {
                offreService.ajouterCommentaire(offre.getIdOffre(),
                        currentUser.getId_utilisateur(), contenu);
                // Mettre à jour le compteur dans le modèle et le label externe
                offre.setNbCommentaires(offre.getNbCommentaires() + 1);
                if (lblCommentCount != null)
                    lblCommentCount.setText(offre.getNbCommentaires() + " commentaire(s)");

                taNouveau.clear();
                lblErreur.setStyle("-fx-text-fill: #22c55e;");
                lblErreur.setText("✅ Commentaire publié !");
                // Recharger la liste
                chargerCommentaires(offre.getIdOffre(), commentairesBox);
                scrollComm.setVvalue(0.0); // revenir en haut
            } catch (Exception ex) {
                ex.printStackTrace();
                lblErreur.setStyle("-fx-text-fill: #ef4444;");
                lblErreur.setText("❌ Erreur : " + ex.getMessage());
            }
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle("""
            -fx-background-color: #334155; -fx-text-fill: white;
            -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;
            """);
        btnFermer.setOnAction(ev -> modal.close());

        root.getChildren().addAll(titre, scrollComm, new Separator(),
                lblNouv, taNouveau, lblErreur, btnPublier, btnFermer);

        ScrollPane scrollRoot = new ScrollPane(root);
        scrollRoot.setFitToWidth(true);
        scrollRoot.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a;");
        modal.setScene(new Scene(scrollRoot, 520, 640));
        modal.show();
    }

    /** Charge et affiche les commentaires d'une offre dans une VBox */
    private void chargerCommentaires(int idOffre, VBox container) {
        container.getChildren().clear();
        try {
            List<Commentaire> comments = offreService.getCommentairesParOffre(idOffre);
            if (comments.isEmpty()) {
                Label vide = new Label("Aucun commentaire pour le moment. Soyez le premier ! 😊");
                vide.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-padding: 10;");
                vide.setWrapText(true);
                container.getChildren().add(vide);
                return;
            }
            for (Commentaire c : comments) {
                container.getChildren().add(buildBulleCommentaire(c));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Erreur lors du chargement des commentaires.");
            err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            container.getChildren().add(err);
        }
    }

    /** Construit la bulle visuelle d'un commentaire */
    private VBox buildBulleCommentaire(Commentaire c) {
        VBox bulle = new VBox(4);
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
        Label auteur = new Label(c.getAuteurLabel().isBlank() ? "Utilisateur" : c.getAuteurLabel());
        auteur.setStyle("-fx-text-fill: #6c8cff; -fx-font-weight: bold; -fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(c.getDateLabel());
        date.setStyle("-fx-text-fill: #475569; -fx-font-size: 10px;");
        meta.getChildren().addAll(avatar, auteur, spacer, date);

        Label contenu = new Label(c.getContenu());
        contenu.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        contenu.setWrapText(true);

        bulle.getChildren().addAll(meta, contenu);
        return bulle;
    }

    // ════════════════════════════════════════════════════════════
    //  MODAL POSTULER / S'ABONNER
    // ════════════════════════════════════════════════════════════
    private void ouvrirModalPostuler(Offre offre) {
        Stage modal = new Stage();
        modal.setTitle("Postuler — " + offre.getTitre());

        VBox root = new VBox(14);
        root.setStyle("-fx-background-color: #0f172a; -fx-padding: 28;");
        root.setPrefWidth(520);

        Label titre = new Label("📋  " + offre.getTitre());
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;");

        VBox resumeBox = new VBox(6);
        resumeBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-padding: 14;");
        resumeBox.getChildren().addAll(
                resumeLigne("🔑 Matricule", offre.getMatricule()),
                resumeLigne("💰 Prix",      formatPrix(offre.getPrix())),
                resumeLigne("⏱ Durée",      offre.getDuree() + " jours"),
                resumeLigne("📍 Lieu",       nvl(offre.getLocalisation()))
        );

        Label lblMode = new Label("Mode de paiement");
        lblMode.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        ComboBox<String> cbMode = new ComboBox<>(FXCollections.observableArrayList(
                "carte", "virement", "paypal", "gratuit"));
        cbMode.setValue("carte");
        cbMode.setMaxWidth(Double.MAX_VALUE);
        cbMode.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

        Label lblMsg2 = new Label("Message / Motivation");
        lblMsg2.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        TextArea taMsg = new TextArea();
        taMsg.setPromptText("Décrivez votre motivation...");
        taMsg.setPrefRowCount(3);
        taMsg.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

        Label lblCv = new Label("CV (PDF, optionnel)");
        lblCv.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        HBox cvRow = new HBox(8);
        cvRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfCvPath = new TextField();
        tfCvPath.setPromptText("Aucun fichier sélectionné");
        tfCvPath.setEditable(false);
        tfCvPath.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8;");
        HBox.setHgrow(tfCvPath, Priority.ALWAYS);
        Button btnCv = new Button("📁 Parcourir");
        btnCv.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
        final String[] cvPathHolder = {null};
        btnCv.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showOpenDialog(modal);
            if (f != null) { cvPathHolder[0] = f.getAbsolutePath(); tfCvPath.setText(f.getName()); }
        });
        cvRow.getChildren().addAll(tfCvPath, btnCv);

        Label lblLettre = new Label("Lettre de motivation");
        lblLettre.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        TextArea taLettre = new TextArea();
        taLettre.setPromptText("Votre lettre de motivation...");
        taLettre.setPrefRowCount(4);
        taLettre.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

        Label lblFeedback = new Label("");
        lblFeedback.setStyle("-fx-font-size: 12px;");
        lblFeedback.setWrapText(true);

        Button btnConfirmer = new Button("✅  Confirmer la souscription");
        btnConfirmer.setMaxWidth(Double.MAX_VALUE);
        btnConfirmer.setStyle("""
            -fx-background-color: #22c55e; -fx-text-fill: white;
            -fx-font-size: 14px; -fx-font-weight: bold;
            -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;
            """);
        btnConfirmer.setOnAction(ev -> {
            if (currentUser == null) {
                lblFeedback.setStyle("-fx-text-fill: #ef4444;");
                lblFeedback.setText("❌ Vous devez être connecté.");
                return;
            }
            try {
                Candidature c = offreService.souscrire(
                        currentUser.getId_utilisateur(), offre.getIdOffre(),
                        currentUser.getEmail(), taMsg.getText(),
                        taLettre.getText(), cvPathHolder[0], cbMode.getValue());
                lblFeedback.setStyle("-fx-text-fill: #22c55e;");
                lblFeedback.setText("✅  Souscription confirmée !\n"
                        + "Référence : " + c.getReferencePaiement() + "\n"
                        + "Expiration : " + (c.getDateExpiration() != null
                        ? c.getDateExpiration().format(FMT) : "—"));
                btnConfirmer.setDisable(true);
                chargerOffres();
                chargerMesAbonnements();
            } catch (Exception ex) {
                ex.printStackTrace();
                lblFeedback.setStyle("-fx-text-fill: #ef4444;");
                lblFeedback.setText("❌ Erreur : " + ex.getMessage());
            }
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle("""
            -fx-background-color: #334155; -fx-text-fill: white;
            -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;
            """);
        btnFermer.setOnAction(ev -> modal.close());

        root.getChildren().addAll(titre, resumeBox, lblMode, cbMode,
                lblMsg2, taMsg, lblCv, cvRow, lblLettre, taLettre,
                lblFeedback, btnConfirmer, btnFermer);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a;");
        modal.setScene(new Scene(scroll, 540, 620));
        modal.show();
    }

    // ════════════════════════════════════════════════════════════
    //  MES ABONNEMENTS
    // ════════════════════════════════════════════════════════════
    private void chargerMesAbonnements() {
        if (mesAbonnementsBox == null || currentUser == null) return;
        mesAbonnementsBox.getChildren().removeIf(n ->
                n.getUserData() != null && "dynamic".equals(n.getUserData()));
        try {
            List<Candidature> list = offreService.getCandidaturesParUser(currentUser.getId_utilisateur());
            boolean empty = list.isEmpty();
            if (lblAucunAbonnement != null) {
                lblAucunAbonnement.setVisible(empty);
                lblAucunAbonnement.setManaged(empty);
            }
            for (Candidature c : list)
                mesAbonnementsBox.getChildren().add(buildCarteAbonnement(c));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/utilisateur/profil.fxml"));
            Parent root = loader.load();
            org.controller.utilisateur.ProfilController ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setUser(currentUser);
            Scene scene = new Scene(root, 1920, 1000);
            var css = getClass().getResource("/view/css/utilisateur/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SkillSwap - Profil");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox buildCarteAbonnement(Candidature c) {
        VBox card = new VBox(6);
        card.setUserData("dynamic");
        card.setStyle("""
            -fx-background-color: #1e293b;
            -fx-background-radius: 10;
            -fx-padding: 12;
            -fx-border-color: %s;
            -fx-border-radius: 10; -fx-border-width: 1.5;
            """.formatted(c.isActive() && !c.isExpiree() ? "#22c55e" : "#ef4444"));
        card.getChildren().addAll(
                abonnLigne("📋 Offre",      nvl(c.getTitreOffre())),
                abonnLigne("🔑 Matricule",  nvl(c.getMatriculeOffre())),
                abonnLigne("📧 Email",      nvl(c.getMailAcheteur())),
                abonnLigne("💰 Prix payé",  formatPrix(c.getPrixPaye())),
                abonnLigne("💳 Paiement",   nvl(c.getModePaiement())),
                abonnLigne("🧾 Référence",  nvl(c.getReferencePaiement())),
                abonnLigne("📅 Abonnement", c.getDateAbonnement() != null ? c.getDateAbonnement().format(FMT) : "—"),
                abonnLigne("⏳ Expiration", c.getDateExpiration() != null ? c.getDateExpiration().format(FMT) : "—"),
                abonnLigne("✅ Statut",     c.getStatutLabel()),
                abonnLigne("🟢 Actif",      c.getAbonnementLabel())
        );
        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  GEMINI AI CHAT
    // ════════════════════════════════════════════════════════════
    @FXML
    private void ouvrirChatGemini() {
        if (GEMINI_API_KEY == null || GEMINI_API_KEY.isBlank()) {
            afficherErreurGemini("Clé API non définie",
                    "La variable d'environnement GEMINI_API_KEY n'est pas définie.");
            return;
        }

        Stage chatStage = new Stage();
        chatStage.initStyle(StageStyle.UNDECORATED);
        chatStage.setTitle("✨ Assistant IA — SkillSwap");

        Label lblTitre = new Label("✨ Assistant IA SkillSwap");
        lblTitre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");

        Button btnFermerChat = new Button("✕");
        btnFermerChat.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white;" +
                        "-fx-background-radius: 50%; -fx-min-width: 26; -fx-min-height: 26;" +
                        "-fx-max-width: 26; -fx-max-height: 26; -fx-cursor: hand;");
        btnFermerChat.setOnAction(e -> chatStage.close());

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        HBox header = new HBox(10, lblTitre, spacerH, btnFermerChat);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #0f1623, #1a1050);" +
                        "-fx-border-color: #2d2d44; -fx-border-width: 0 0 1 0;");

        VBox messagesBox = new VBox(8);
        messagesBox.setPadding(new Insets(12));

        ScrollPane scrollMessages = new ScrollPane(messagesBox);
        scrollMessages.setFitToWidth(true);
        scrollMessages.setStyle("-fx-background-color: #0d1220; -fx-background: #0d1220;");
        scrollMessages.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollMessages, Priority.ALWAYS);

        ajouterBulleGemini(messagesBox,
                "Bonjour ! Je suis votre assistant IA SkillSwap.\n" +
                        "Je peux vous aider à trouver des offres, expliquer les abonnements, " +
                        "ou répondre à vos questions. 😊", false);

        TextField tfInput = new TextField();
        tfInput.setPromptText("Posez votre question...");
        tfInput.setStyle(
                "-fx-background-color: #1e293b; -fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #64748b; -fx-background-radius: 20;" +
                        "-fx-padding: 10 16; -fx-font-size: 13px; -fx-border-width: 0;");
        HBox.setHgrow(tfInput, Priority.ALWAYS);

        Button btnEnvoyer = new Button("➤");
        btnEnvoyer.setStyle(
                "-fx-background-color: linear-gradient(to right, #4285f4, #a142f4);" +
                        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 50%; -fx-min-width: 38; -fx-min-height: 38;" +
                        "-fx-max-width: 38; -fx-max-height: 38; -fx-cursor: hand;");

        Label lblTyping = new Label("✨ Gemini réfléchit...");
        lblTyping.setStyle("-fx-text-fill: #a142f4; -fx-font-size: 11px; -fx-padding: 0 0 0 12;");
        lblTyping.setVisible(false);
        lblTyping.setManaged(false);

        Runnable envoyerMessage = () -> {
            String texte = tfInput.getText() == null ? "" : tfInput.getText().trim();
            if (texte.isBlank()) return;
            tfInput.clear();
            btnEnvoyer.setDisable(true);
            ajouterBulleGemini(messagesBox, texte, true);
            Platform.runLater(() -> scrollMessages.setVvalue(1.0));
            lblTyping.setVisible(true);
            lblTyping.setManaged(true);
            new Thread(() -> {
                try {
                    String reponse = appelGemini(texte);
                    Platform.runLater(() -> {
                        lblTyping.setVisible(false);
                        lblTyping.setManaged(false);
                        ajouterBulleGemini(messagesBox, reponse, false);
                        scrollMessages.setVvalue(1.0);
                        btnEnvoyer.setDisable(false);
                        tfInput.requestFocus();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        lblTyping.setVisible(false);
                        lblTyping.setManaged(false);
                        ajouterBulleGemini(messagesBox, "❌ Erreur : " + ex.getMessage(), false);
                        btnEnvoyer.setDisable(false);
                    });
                }
            }, "gemini-thread").start();
        };

        btnEnvoyer.setOnAction(e -> envoyerMessage.run());
        tfInput.setOnAction(e -> envoyerMessage.run());

        HBox inputRow = new HBox(8, tfInput, btnEnvoyer);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(8, 12, 8, 12));
        inputRow.setStyle("-fx-background-color: #0d1220; -fx-border-color: #1e293b; -fx-border-width: 1 0 0 0;");

        VBox bottomBar = new VBox(0, lblTyping, inputRow);
        VBox root = new VBox(header, scrollMessages, bottomBar);
        root.setStyle("-fx-background-color: #0d1220;");

        chatStage.setScene(new Scene(root, 420, 560));

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        chatStage.setX(screen.getMaxX() - 440);
        chatStage.setY(screen.getMaxY() - 580);
        chatStage.show();
        tfInput.requestFocus();
    }

    private void afficherErreurGemini(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText("Erreur Gemini");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String appelGemini(String question) throws Exception {
        String prompt =
                "Tu es un assistant IA intégré dans l'application SkillSwap. " +
                        "SkillSwap est une plateforme de partage de compétences avec des offres d'emploi, " +
                        "de projet et de tâche. Tu aides les utilisateurs à trouver des offres, " +
                        "comprendre les abonnements et naviguer dans l'application. " +
                        "Réponds en français, de façon concise et utile.\n\n" +
                        "Question : " + question;

        String body = "{"
                + "\"contents\":[{"
                + "\"parts\":[{"
                + "\"text\":" + jsonString(prompt)
                + "}]"
                + "}]"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("HTTP " + response.statusCode() + " : " + response.body());

        return extraireTexteGemini(response.body());
    }

    private String jsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private String extraireTexteGemini(String json) {
        Pattern p = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        String last = null;
        while (m.find()) last = m.group(1);
        if (last == null) throw new RuntimeException("Réponse Gemini invalide : " + json);
        return last
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private void ajouterBulleGemini(VBox box, String texte, boolean estMoi) {
        Label bulle = new Label(texte);
        bulle.setWrapText(true);
        bulle.setMaxWidth(300);

        if (estMoi) {
            bulle.setStyle(
                    "-fx-background-color: linear-gradient(to right, #4285f4, #3b5bdb);" +
                            "-fx-text-fill: white; -fx-font-size: 13px;" +
                            "-fx-padding: 10 14; -fx-background-radius: 18 18 4 18;");
            HBox row = new HBox(bulle);
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setPadding(new Insets(2, 8, 2, 50));
            box.getChildren().add(row);
        } else {
            Label avatar = new Label("✨");
            avatar.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-background-color: linear-gradient(to bottom, #4285f4, #a142f4);" +
                            "-fx-background-radius: 50%; -fx-padding: 4 6;" +
                            "-fx-min-width: 30; -fx-min-height: 30;");
            bulle.setStyle(
                    "-fx-background-color: #1e293b;" +
                            "-fx-text-fill: #e2e8f0; -fx-font-size: 13px;" +
                            "-fx-padding: 10 14; -fx-background-radius: 18 18 18 4;" +
                            "-fx-border-color: rgba(161,66,244,0.3); -fx-border-width: 1;" +
                            "-fx-border-radius: 18 18 18 4;");
            HBox row = new HBox(8, avatar, bulle);
            row.setAlignment(Pos.BOTTOM_LEFT);
            row.setPadding(new Insets(2, 50, 2, 4));
            box.getChildren().add(row);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════
    private HBox infoLigne(String cle, String valeur) {
        Label k = new Label(cle + " : ");
        k.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        Label v = new Label(valeur);
        v.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11px;");
        v.setWrapText(true);
        v.setMaxWidth(200);
        HBox row = new HBox(4, k, v);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private HBox resumeLigne(String cle, String valeur) {
        Label k = new Label(cle + " : ");
        k.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-min-width: 130;");
        Label v = new Label(valeur);
        v.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        return new HBox(6, k, v);
    }

    private Label abonnLigne(String cle, String valeur) {
        Label l = new Label(cle + " : " + valeur);
        l.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
        l.setWrapText(true);
        l.setMaxWidth(230);
        return l;
    }

    private String formatPrix(BigDecimal p) {
        if (p == null) return "Gratuit";
        return p.compareTo(BigDecimal.ZERO) == 0 ? "Gratuit" : p.toPlainString() + " TND";
    }

    private String nvl(String s)              { return s == null ? "—" : s; }
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}