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
import org.service.utilisateur.UtilisateurService;
import org.utils.Session;

import java.util.regex.Pattern;

public class InscriptionController {

    // =========================
    // Champs inscription
    // =========================
    @FXML private PasswordField tfConfirmPassword;
    @FXML private TextField tfConfirmPasswordVisible;
    @FXML private Button btnToggleConfirmPassword;

    @FXML private TextField tfNom, tfPrenom, tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMsg;
    @FXML private TextField tfPasswordVisible;
    @FXML private Button btnTogglePassword;

    // =========================
    // Téléphone (UI style image)
    // =========================
    @FXML private ComboBox<CountryItem> cbCountry;
    @FXML private TextField tfPhone;
    @FXML private Button btnVerifyPhone;

    // =========================
    // Clé d'accès (admin)
    // =========================
    @FXML private TextField tfCleAcces;
    @FXML private VBox boxCleAcces;

    // =========================
    // Not Robot
    // =========================
    @FXML private CheckBox cbNotRobot;

    private final UtilisateurService service = new UtilisateurService();
    private ImageView eyeOpenIcon;
    private ImageView eyeOffIcon;
    private ImageView eyeOpenIconConfirm;
    private ImageView eyeOffIconConfirm;
    // ✅ Valeurs autorisées (ENUM DB)
    private static final String[] CLES_AUTORISEES = {"123456", "789456", "78963"};
    private static final String CLE_DEFAUT_NON_ADMIN = "123456";

    // ✅ Pays (drapeau + code)
    private record CountryItem(String name, String code, String flagEmoji) {
        @Override public String toString() { return flagEmoji + " " + code; }
    }

    @FXML
    public void initialize() {
        setupConfirmPasswordToggleSync();
        setupConfirmPasswordIcons();
        setupPasswordToggleSync();
        setupPasswordIcons();

        // Roles
        if (cbRole != null) {
            cbRole.getItems().addAll("freelance", "entrepreneur", "admin");
        }

        // Téléphone style image
        setupPhoneWidget();

        // Clé accès visible seulement si admin
        setupCleAccesVisibility();
    }
    private void setupPasswordIcons() {
        if (btnTogglePassword == null) return;

        var eyeOpenUrl = getClass().getResource("/view/files/utilisateur/yeux.png");
        var eyeOffUrl  = getClass().getResource("/view/files/utilisateur/yeux_off.png");

        if (eyeOpenUrl == null) {
            System.out.println("❌ Image introuvable: /view/files/utilisateur/yeux.png");
            return;
        }
        if (eyeOffUrl == null) {
            System.out.println("⚠️ Image introuvable: yeux_off.png (fallback)");
            eyeOffUrl = eyeOpenUrl;
        }

        Image eyeOpen = new Image(eyeOpenUrl.toExternalForm());
        Image eyeOff  = new Image(eyeOffUrl.toExternalForm());

        eyeOpenIcon = new ImageView(eyeOpen);
        eyeOffIcon  = new ImageView(eyeOff);

        eyeOpenIcon.setFitWidth(18);  eyeOpenIcon.setFitHeight(18);
        eyeOffIcon.setFitWidth(18);   eyeOffIcon.setFitHeight(18);
        eyeOpenIcon.setPreserveRatio(true); eyeOpenIcon.setSmooth(true);
        eyeOffIcon.setPreserveRatio(true);  eyeOffIcon.setSmooth(true);

        btnTogglePassword.setMinSize(28, 28);
        btnTogglePassword.setPrefSize(28, 28);
        btnTogglePassword.setMaxSize(28, 28);
        btnTogglePassword.setPadding(javafx.geometry.Insets.EMPTY);
        btnTogglePassword.setText("");
        btnTogglePassword.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnTogglePassword.setGraphic(eyeOpenIcon);
    }
    private void setupConfirmPasswordIcons() {
        if (btnToggleConfirmPassword == null) return;

        var eyeOpenUrl = getClass().getResource("/view/files/utilisateur/yeux.png");
        var eyeOffUrl  = getClass().getResource("/view/files/utilisateur/yeux_off.png");

        if (eyeOpenUrl == null) return;
        if (eyeOffUrl == null) eyeOffUrl = eyeOpenUrl;

        Image eyeOpen = new Image(eyeOpenUrl.toExternalForm());
        Image eyeOff  = new Image(eyeOffUrl.toExternalForm());

        eyeOpenIconConfirm = new ImageView(eyeOpen);
        eyeOffIconConfirm  = new ImageView(eyeOff);

        eyeOpenIconConfirm.setFitWidth(18);  eyeOpenIconConfirm.setFitHeight(18);
        eyeOffIconConfirm.setFitWidth(18);   eyeOffIconConfirm.setFitHeight(18);
        eyeOpenIconConfirm.setPreserveRatio(true); eyeOpenIconConfirm.setSmooth(true);
        eyeOffIconConfirm.setPreserveRatio(true);  eyeOffIconConfirm.setSmooth(true);

        btnToggleConfirmPassword.setMinSize(28, 28);
        btnToggleConfirmPassword.setPrefSize(28, 28);
        btnToggleConfirmPassword.setMaxSize(28, 28);
        btnToggleConfirmPassword.setPadding(javafx.geometry.Insets.EMPTY);
        btnToggleConfirmPassword.setText("");
        btnToggleConfirmPassword.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnToggleConfirmPassword.setGraphic(eyeOpenIconConfirm);
    }

    private void setupConfirmPasswordToggleSync() {
        if (tfConfirmPassword == null || tfConfirmPasswordVisible == null) return;

        tfConfirmPasswordVisible.setVisible(false);
        tfConfirmPasswordVisible.setManaged(false);

        // Sync PasswordField → TextField visible (sens unique quand caché)
        tfConfirmPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (!tfConfirmPasswordVisible.isVisible()) tfConfirmPasswordVisible.setText(newV);
        });

        // Sync TextField visible → PasswordField (sens unique quand visible)
        tfConfirmPasswordVisible.textProperty().addListener((obs, oldV, newV) -> {
            if (tfConfirmPasswordVisible.isVisible()) tfConfirmPassword.setText(newV);
        });
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        boolean showing = tfConfirmPasswordVisible.isVisible();

        if (showing) {
            // Masquer → repasser au PasswordField
            tfConfirmPassword.setText(tfConfirmPasswordVisible.getText());
            tfConfirmPasswordVisible.setVisible(false);
            tfConfirmPasswordVisible.setManaged(false);
            tfConfirmPassword.setVisible(true);
            tfConfirmPassword.setManaged(true);
            if (btnToggleConfirmPassword != null) btnToggleConfirmPassword.setGraphic(eyeOpenIconConfirm);
            tfConfirmPassword.requestFocus();
            tfConfirmPassword.positionCaret(tfConfirmPassword.getText().length());
        } else {
            // Afficher → passer au TextField visible
            tfConfirmPasswordVisible.setText(tfConfirmPassword.getText());
            tfConfirmPassword.setVisible(false);
            tfConfirmPassword.setManaged(false);
            tfConfirmPasswordVisible.setVisible(true);
            tfConfirmPasswordVisible.setManaged(true);
            if (btnToggleConfirmPassword != null) btnToggleConfirmPassword.setGraphic(eyeOffIconConfirm);
            tfConfirmPasswordVisible.requestFocus();
            tfConfirmPasswordVisible.positionCaret(tfConfirmPasswordVisible.getText().length());
        }
    }
    private void setupPasswordToggleSync() {
        if (tfPassword == null || tfPasswordVisible == null) return;

        tfPasswordVisible.setVisible(false);
        tfPasswordVisible.setManaged(false);

        tfPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (!tfPasswordVisible.isVisible()) tfPasswordVisible.setText(newV);
        });

        tfPasswordVisible.textProperty().addListener((obs, oldV, newV) -> {
            if (tfPasswordVisible.isVisible()) tfPassword.setText(newV);
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean showing = tfPasswordVisible.isVisible();

        if (showing) {
            tfPassword.setText(tfPasswordVisible.getText());
            tfPasswordVisible.setVisible(false);
            tfPasswordVisible.setManaged(false);
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);
            if (btnTogglePassword != null) btnTogglePassword.setGraphic(eyeOpenIcon);
            tfPassword.requestFocus();
            tfPassword.positionCaret(tfPassword.getText().length());
        } else {
            tfPasswordVisible.setText(tfPassword.getText());
            tfPassword.setVisible(false);
            tfPassword.setManaged(false);
            tfPasswordVisible.setVisible(true);
            tfPasswordVisible.setManaged(true);
            if (btnTogglePassword != null) btnTogglePassword.setGraphic(eyeOffIcon);
            tfPasswordVisible.requestFocus();
            tfPasswordVisible.positionCaret(tfPasswordVisible.getText().length());
        }
    }

    // =========================
    // ✅ Téléphone widget (drapeau + numéro + verify)
    // =========================
    private void setupPhoneWidget() {
        if (cbCountry == null || tfPhone == null) return;

        cbCountry.getItems().addAll(
                new CountryItem("United States", "+1", "🇺🇸"),
                new CountryItem("Tunisie", "+216", "🇹🇳"),
                new CountryItem("France", "+33", "🇫🇷"),
                new CountryItem("Algérie", "+213", "🇩🇿"),
                new CountryItem("Maroc", "+212", "🇲🇦"),
                new CountryItem("Belgique", "+32", "🇧🇪"),
                new CountryItem("Suisse", "+41", "🇨🇭"),
                new CountryItem("Canada", "+1", "🇨🇦")
        );

        cbCountry.getSelectionModel().select(1); // 🇹🇳 par défaut (index 1)

        // Affichage drapeau + code dans la combo
        cbCountry.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(CountryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.flagEmoji() + " " + item.code());
            }
        });
        cbCountry.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(CountryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.flagEmoji() + " " + item.code());
            }
        });

        // Formatter: si +1 => (201) 555-0123, sinon digits simples
        tfPhone.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // autoriser suppression
            if (newText.isEmpty()) return change;

            String digits = newText.replaceAll("\\D", "");
            CountryItem c = cbCountry.getValue();

            if (c != null && "+1".equals(c.code())) {
                if (digits.length() > 10) digits = digits.substring(0, 10);
                change.setText(formatUSPhone(digits));
            } else {
                if (digits.length() > 15) digits = digits.substring(0, 15);
                change.setText(digits);
            }

            // Remplace tout le texte du champ par la version formatée
            change.setRange(0, change.getControlText().length());
            return change;
        }));

        // Reformat si on change de pays
        cbCountry.valueProperty().addListener((obs, o, n) -> {
            String digits = (tfPhone.getText() == null) ? "" : tfPhone.getText().replaceAll("\\D", "");
            if (n != null && "+1".equals(n.code())) tfPhone.setText(formatUSPhone(digits));
            else tfPhone.setText(digits);
        });
    }

    private String formatUSPhone(String digits) {
        int n = digits.length();
        if (n <= 3) return digits;
        if (n <= 6) return "(" + digits.substring(0, 3) + ") " + digits.substring(3);
        return "(" + digits.substring(0, 3) + ") " + digits.substring(3, 6) + "-" + digits.substring(6);
    }

    // Numéro final pour DB: +216 22555123
    private String buildFullPhone() {
        CountryItem c = (cbCountry == null) ? null : cbCountry.getValue();
        String code = (c == null) ? "" : c.code();
        String digits = (tfPhone == null || tfPhone.getText() == null) ? "" : tfPhone.getText().replaceAll("\\D", "");
        return (code + " " + digits).trim();
    }

    @FXML
    private void verifyPhone() {
        // Ici tu peux faire OTP SMS plus tard
        String full = buildFullPhone();
        lblMsg.setText("Numéro à vérifier : " + full);
    }

    // =========================
    // ✅ Clé admin visible seulement si admin
    // =========================
    private void setupCleAccesVisibility() {
        if (cbRole == null) return;

        setCleAccesVisible(false);

        cbRole.valueProperty().addListener((obs, oldV, newV) -> {
            boolean isAdmin = "admin".equalsIgnoreCase(newV);
            setCleAccesVisible(isAdmin);

            if (!isAdmin && tfCleAcces != null) {
                tfCleAcces.clear();
            }
        });
    }

    private void setCleAccesVisible(boolean visible) {
        if (boxCleAcces != null) {
            boxCleAcces.setVisible(visible);
            boxCleAcces.setManaged(visible);
            return;
        }
        if (tfCleAcces != null) {
            tfCleAcces.setVisible(visible);
            tfCleAcces.setManaged(visible);
        }
    }

    private boolean isCleValide(String cle) {
        if (cle == null) return false;
        String c = cle.trim();
        for (String allowed : CLES_AUTORISEES) {
            if (allowed.equals(c)) return true;
        }
        return false;
    }

    // =========================
    // ✅ Inscription
    // =========================
    @FXML
    void inscrire() {
        try {
            String nom = tfNom == null ? "" : tfNom.getText().trim();
            String prenom = tfPrenom == null ? "" : tfPrenom.getText().trim();
            String email = tfEmail == null ? "" : tfEmail.getText().trim();
            String pass = (tfPasswordVisible != null && tfPasswordVisible.isVisible())
                    ? tfPasswordVisible.getText().trim()
                    : (tfPassword == null ? "" : tfPassword.getText().trim());

            String confirmPass = (tfConfirmPasswordVisible != null && tfConfirmPasswordVisible.isVisible())
                    ? tfConfirmPasswordVisible.getText().trim()
                    : (tfConfirmPassword == null ? "" : tfConfirmPassword.getText().trim());
            String tel = buildFullPhone();

            String role = (cbRole == null) ? null : cbRole.getValue();

            String cle = (tfCleAcces == null || tfCleAcces.getText() == null)
                    ? ""
                    : tfCleAcces.getText().trim();

            // Champs obligatoires
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pass.isEmpty() || role == null) {
                lblMsg.setText("Veuillez remplir tous les champs obligatoires.");
                return;
            }
            if (confirmPass.isEmpty()) {
                lblMsg.setText("Veuillez confirmer le mot de passe.");
                return;
            }
            if (!pass.equals(confirmPass)) {
                lblMsg.setText("Les mots de passe ne correspondent pas.");
                return;
            }
            if (!isValidEmail(email)) {
                lblMsg.setText("Email invalide.");
                return;
            }

            if (pass.length() < 8) {
                lblMsg.setText("Mot de passe : 8 caractères minimum.");
                return;
            }

            if (!pass.matches(".*[A-Z].*")) {
                lblMsg.setText("Le mot de passe doit contenir au moins une lettre majuscule.");
                return;
            }

            if (!pass.matches(".*[a-z].*")) {
                lblMsg.setText("Le mot de passe doit contenir au moins une lettre minuscule.");
                return;
            }

            if (!pass.matches(".*\\d.*")) {
                lblMsg.setText("Le mot de passe doit contenir au moins un chiffre.");
                return;
            }

            if (!pass.matches(".*[@$!%*?&].*")) {
                lblMsg.setText("Le mot de passe doit contenir au moins un caractère spécial (@$!%*?&).");
                return;
            }

            // Téléphone obligatoire ? (si tu veux)
            if (tel.isBlank()) {
                lblMsg.setText("Veuillez saisir un numéro de téléphone.");
                return;
            }

            // ✅ Clé obligatoire seulement si admin
            boolean isAdmin = "admin".equalsIgnoreCase(role);

            if (isAdmin) {
                if (cle.isEmpty()) {
                    lblMsg.setText("Veuillez saisir la clé d'accès ");
                    return;
                }
                if (!isCleValide(cle)) {
                    lblMsg.setText("Clé d'accès invalide !");
                    return;
                }
            } else {
                // Non-admin : pas obligatoire
                if (cle.isEmpty()) {
                    cle = CLE_DEFAUT_NON_ADMIN; // ⚠️ DB NOT NULL
                } else if (!isCleValide(cle)) {
                    lblMsg.setText("Clé d'accès invalide.");
                    return;
                }
            }

            // Not Robot
            if (cbNotRobot == null || !cbNotRobot.isSelected()) {
                lblMsg.setText("Veuillez confirmer que vous n'êtes pas un robot.");
                return;
            }

            // Email déjà utilisé ?
            if (service.findByEmail(email) != null) {
                lblMsg.setText("Cet email est déjà utilisé.");
                return;
            }

            utilisateur u = new utilisateur();
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            u.setMot_de_passe(pass);
            u.setTelephone(tel);
            u.setRole(role);
            u.setStatut("actif");
            u.setCle_acces(cle);

            service.inscrire(u);

            utilisateur userFromDB = service.findByEmail(email);
            if (userFromDB == null) {
                lblMsg.setText("Inscription OK, mais impossible de charger le profil.");
                return;
            }

            Session.setCurrentUser(userFromDB);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/profil.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());

            ProfilController controller = loader.getController();
            controller.setUser(userFromDB);

            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setTitle("SkillSwap - Profil");
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void goLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/login.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1000);
            scene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/style.css").toExternalForm());

            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setTitle("SkillSwap - Connexion");
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Erreur chargement connexion.");
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }
}