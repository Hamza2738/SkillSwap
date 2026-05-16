package org.controller.utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.model.Utilisateurs.utilisateur;
import org.service.utilisateur.EmailHistory;
import org.service.utilisateur.EmailUtil;
import org.service.utilisateur.UtilisateurService;
import org.utils.Session;
import org.utils.SessionContext;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfPasswordVisible;
    @FXML private Button btnTogglePassword;
    @FXML private Label lblMsg;
    @FXML private CheckBox cbNotRobot;
    @FXML private Button acceuilButton;

    private final UtilisateurService service = new UtilisateurService();

    private ImageView eyeOpenIcon;
    private ImageView eyeOffIcon;

    private String lastOtpCode = null;
    private long lastOtpExpiryMillis = 0L;
    private String lastOtpEmail = null;

    // ✅ Popup pur JavaFX — 0 interférence de style
    private final javafx.stage.Popup suggestionPopup = new javafx.stage.Popup();
    private final VBox suggestionBox = new VBox();

    // =========================================================
    // INIT
    // =========================================================
    @FXML
    public void initialize() {
        setupEmailSuggestions();
        setupPasswordToggleSync();
        setupPasswordIcons();

        tfEmail.setOnAction(e -> seConnecter());
        pfPassword.setOnAction(e -> seConnecter());
        tfPasswordVisible.setOnAction(e -> seConnecter());
    }

    // =========================================================
    // EMAIL SUGGESTIONS (Popup + VBox — sans ContextMenu)
    // =========================================================
    private void setupEmailSuggestions() {
        if (tfEmail == null) return;

        // Style du conteneur VBox (notre dropdown)
        suggestionBox.setStyle(
                "-fx-background-color: #1C2638;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 4 0;" +           // ✅ padding UNIQUEMENT vertical, 0 horizontal
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.25, 0, 6);"
        );

        suggestionPopup.getContent().add(suggestionBox);
        suggestionPopup.setAutoHide(true);
        suggestionPopup.setHideOnEscape(true);

        tfEmail.setOnMouseClicked(e -> showSuggestions(tfEmail.getText()));

        tfEmail.focusedProperty().addListener((obs, oldV, focused) -> {
            if (focused) showSuggestions(tfEmail.getText());
            else suggestionPopup.hide();
        });

        tfEmail.textProperty().addListener((obs, oldV, newV) -> {
            if (tfEmail.isFocused()) showSuggestions(newV);
        });

        tfEmail.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE)
                suggestionPopup.hide();
        });
    }

    private void showSuggestions(String typed) {
        if (tfEmail == null) return;

        String q = typed == null ? "" : typed.trim().toLowerCase();

        List<String> all = EmailHistory.getAll();
        List<String> filtered = all.stream()
                .filter(e -> q.isEmpty() || e.toLowerCase().contains(q))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            suggestionPopup.hide();
            return;
        }

        suggestionBox.getChildren().clear();

        // ✅ Bounds EXACTES du TextField à l'écran
        javafx.geometry.Bounds bounds = tfEmail.localToScreen(tfEmail.getBoundsInLocal());
        if (bounds == null) return;

        double exactWidth = bounds.getWidth();  // ✅ largeur réelle à l'écran

        // ✅ VBox = même largeur EXACTE que le TextField
        suggestionBox.setPrefWidth(exactWidth);
        suggestionBox.setMinWidth(exactWidth);
        suggestionBox.setMaxWidth(exactWidth);

        for (String email : filtered) {
            Label lbl = new Label(email);

            // ✅ Label = 100% largeur du popup (pas de soustraction)
            lbl.setPrefWidth(exactWidth);
            lbl.setMaxWidth(exactWidth);

            lbl.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-padding: 8 14;" +
                            "-fx-background-color: transparent;" +
                            "-fx-background-radius: 0;" +
                            "-fx-cursor: hand;"
            );

            lbl.setOnMouseEntered(e -> lbl.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-padding: 8 14;" +
                            "-fx-background-color: rgba(123,97,255,0.22);" +
                            "-fx-background-radius: 0;" +
                            "-fx-cursor: hand;"
            ));

            lbl.setOnMouseExited(e -> lbl.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-padding: 8 14;" +
                            "-fx-background-color: transparent;" +
                            "-fx-background-radius: 0;" +
                            "-fx-cursor: hand;"
            ));

            lbl.setOnMouseClicked(e -> {
                tfEmail.setText(email);
                tfEmail.positionCaret(email.length());
                suggestionPopup.hide();
            });

            suggestionBox.getChildren().add(lbl);
        }

        // ✅ Alignement pixel-perfect : même X que le TextField, juste en dessous
        suggestionPopup.show(tfEmail, bounds.getMinX(), bounds.getMaxY() + 2);
    }

    // =========================================================
    // PASSWORD ICONS
    // =========================================================
    private void setupPasswordIcons() {
        if (btnTogglePassword == null) return;

        var eyeOpenUrl = getClass().getResource("/view/files/utilisateur/yeux.png");
        var eyeOffUrl  = getClass().getResource("/view/files/utilisateur/yeux_off.png");

        if (eyeOpenUrl == null) {
            System.out.println("❌ Image introuvable: /view/files/utilisateur/yeux.png");
            return;
        }

        if (eyeOffUrl == null) {
            System.out.println("⚠️ Image introuvable: /view/files/utilisateur/yeux_off.png (fallback)");
            eyeOffUrl = eyeOpenUrl;
        }

        Image eyeOpen = new Image(eyeOpenUrl.toExternalForm());
        Image eyeOff  = new Image(eyeOffUrl.toExternalForm());

        eyeOpenIcon = new ImageView(eyeOpen);
        eyeOffIcon  = new ImageView(eyeOff);

        eyeOpenIcon.setFitWidth(18);
        eyeOpenIcon.setFitHeight(18);
        eyeOffIcon.setFitWidth(18);
        eyeOffIcon.setFitHeight(18);

        eyeOpenIcon.setPreserveRatio(true);
        eyeOffIcon.setPreserveRatio(true);
        eyeOpenIcon.setSmooth(true);
        eyeOffIcon.setSmooth(true);

        btnTogglePassword.setMinSize(28, 28);
        btnTogglePassword.setPrefSize(28, 28);
        btnTogglePassword.setMaxSize(28, 28);
        btnTogglePassword.setPadding(javafx.geometry.Insets.EMPTY);
        btnTogglePassword.setText("");
        btnTogglePassword.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnTogglePassword.setGraphic(eyeOpenIcon);
    }

    // =========================================================
    // PASSWORD TOGGLE SYNC
    // =========================================================
    private void setupPasswordToggleSync() {
        if (pfPassword == null || tfPasswordVisible == null) return;

        tfPasswordVisible.setVisible(false);
        tfPasswordVisible.setManaged(false);

        pfPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (!tfPasswordVisible.isVisible()) tfPasswordVisible.setText(newV);
        });

        tfPasswordVisible.textProperty().addListener((obs, oldV, newV) -> {
            if (tfPasswordVisible.isVisible()) pfPassword.setText(newV);
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean showing = tfPasswordVisible.isVisible();

        if (showing) {
            pfPassword.setText(tfPasswordVisible.getText());
            tfPasswordVisible.setVisible(false);
            tfPasswordVisible.setManaged(false);
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            if (btnTogglePassword != null) btnTogglePassword.setGraphic(eyeOpenIcon);
            pfPassword.requestFocus();
            pfPassword.positionCaret(pfPassword.getText().length());
        } else {
            tfPasswordVisible.setText(pfPassword.getText());
            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
            tfPasswordVisible.setVisible(true);
            tfPasswordVisible.setManaged(true);
            if (btnTogglePassword != null) btnTogglePassword.setGraphic(eyeOffIcon);
            tfPasswordVisible.requestFocus();
            tfPasswordVisible.positionCaret(tfPasswordVisible.getText().length());
        }
    }

    // =========================================================
    // CONNEXION
    // =========================================================
    @FXML
    void seConnecter() {
        try {
            String email = tfEmail.getText().trim();

            String pass = (tfPasswordVisible != null && tfPasswordVisible.isVisible())
                    ? tfPasswordVisible.getText().trim()
                    : pfPassword.getText().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                lblMsg.setText("Veuillez saisir email et mot de passe.");
                return;
            }

            if (cbNotRobot == null || !cbNotRobot.isSelected()) {
                lblMsg.setText("Veuillez confirmer que vous n'êtes pas un robot.");
                return;
            }

            utilisateur u = service.login(email, pass);
            if (u == null) {
                lblMsg.setText("Email ou mot de passe incorrect.");
                return;
            }

            EmailHistory.add(email);
            Session.setCurrentUser(u);
            SessionContext.setUser(u.getId_utilisateur(), u.getEmail());
            goProfilSelonRole(u);

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Erreur : " + e.getMessage());
        }
    }

    // =========================================================
    // OTP - MOT DE PASSE OUBLIÉ
    // =========================================================
    @FXML
    void motDePasseOublie() {
        try {
            TextInputDialog emailDialog = new TextInputDialog();
            emailDialog.setTitle("Réinitialisation");
            emailDialog.setHeaderText("Mot de passe oublié");
            emailDialog.setContentText("Entrez votre email :");

            var emailRes = emailDialog.showAndWait();
            if (emailRes.isEmpty()) return;

            String email = emailRes.get().trim();

            utilisateur u = service.findByEmail(email);
            if (u == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun compte trouvé avec cet email.");
                return;
            }

            String otp = generateOtp6();
            lastOtpCode = otp;
            lastOtpEmail = email;
            lastOtpExpiryMillis = Instant.now().toEpochMilli() + (5 * 60 * 1000);

            EmailUtil.sendOtp(email, otp);

            showAlert(Alert.AlertType.INFORMATION, "Email envoyé",
                    "Un code de vérification a été envoyé à : " + email + "\nIl expire dans 5 minutes.");

            TextInputDialog otpDialog = new TextInputDialog();
            otpDialog.setTitle("Vérification");
            otpDialog.setHeaderText("Entrez le code reçu par email");
            otpDialog.setContentText("Code (6 chiffres) :");

            var otpRes = otpDialog.showAndWait();
            if (otpRes.isEmpty()) return;

            String otpUser = otpRes.get().trim();

            if (!isOtpValid(email, otpUser)) {
                showAlert(Alert.AlertType.ERROR, "Code incorrect",
                        "Code invalide ou expiré. Veuillez recommencer.");
                return;
            }

            TextInputDialog passDialog = new TextInputDialog();
            passDialog.setTitle("Nouveau mot de passe");
            passDialog.setHeaderText("Définir un nouveau mot de passe");
            passDialog.setContentText("Nouveau mot de passe (min 6) :");

            var passRes = passDialog.showAndWait();
            if (passRes.isEmpty()) return;

            String newPass = passRes.get().trim();
            if (newPass.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe : 6 caractères minimum.");
                return;
            }

            service.updatePassword(email, newPass);
            invalidateOtp();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe mis à jour avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Erreur réinitialisation : " + e.getMessage());
        }
    }

    private boolean isOtpValid(String email, String otpUser) {
        long now = Instant.now().toEpochMilli();
        if (lastOtpCode == null || lastOtpEmail == null) return false;
        if (!email.equalsIgnoreCase(lastOtpEmail)) return false;
        if (now > lastOtpExpiryMillis) return false;
        return lastOtpCode.equals(otpUser);
    }

    private void invalidateOtp() {
        lastOtpCode = null;
        lastOtpEmail = null;
        lastOtpExpiryMillis = 0L;
    }

    private String generateOtp6() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    @FXML
    void goInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/inscription.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("SkillSwap - Inscription");
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Erreur chargement inscription.");
        }
    }

    private void goProfilSelonRole(utilisateur u) throws Exception {
        String role = (u.getRole() == null) ? "" : u.getRole().trim().toLowerCase();

        if ("admin".equals(role)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/ProfilAdmin.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());

            ProfilAdminController controller = loader.getController();
            controller.setUser(u);

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("SkillSwap - Admin");
            stage.setScene(scene);

        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/profil.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());

            ProfilController controller = loader.getController();
            controller.setUser(u);

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("SkillSwap - Profil");
            stage.setScene(scene);
        }
    }

    @FXML
    void onAcceuilClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/accueil.fxml")
            );
            Scene accueilScene = new Scene(loader.load(), 1920, 1000);

            var cssUrl = getClass().getResource("/view/css/style.css");
            if (cssUrl != null) accueilScene.getStylesheets().add(cssUrl.toExternalForm());

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("SkillSwap - Accueil");
            stage.setScene(accueilScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblMsg.setText("Erreur chargement accueil : " + e.getMessage());
        }
    }
}