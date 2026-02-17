package org.controller.Competences.front;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ChatBoatFrontController {

    @FXML private VBox messagesBox;
    @FXML private TextField tfMessage;
    @FXML private ScrollPane scroll;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {

        addBotMessage("""
Bienvenue sur SkillSwap.

Je suis là pour vous accompagner dans vos questions professionnelles.

Vous pouvez me consulter concernant :
• CV et préparation aux entretiens
• Gestion du stress
• Organisation et productivité
• Gestion des conflits
• Orientation et évolution de carrière

Je peux également fournir les compétences requises par domaine.
Exemple : "compétences industrie"
""");

        tfMessage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (e.isShiftDown()) return;
                e.consume();
                onSend();
            }
        });

        if (scroll != null) {
            scroll.setFitToWidth(true);
        }
    }

    @FXML
    public void onSend() {
        String text = tfMessage.getText();
        if (text == null || text.trim().isEmpty()) return;

        tfMessage.clear();
        addUserMessage(text);

        String reply = generateProfessionalReply(text);
        addBotMessage(reply);
    }

    @FXML
    public void onClear() {
        messagesBox.getChildren().clear();
        addBotMessage("Ok, on repart à zéro. Quel est ton besoin professionnel ?");
    }

    @FXML
    private void onClose(javafx.event.ActionEvent e) {
        Node src = (Node) e.getSource();
        Stage stage = (Stage) src.getScene().getWindow();
        stage.close();
    }

    // =========================
    // UI - bubbles
    // =========================
    private void addUserMessage(String msg) {
        messagesBox.getChildren().add(bubble(msg, true));
        autoScroll();
    }

    private void addBotMessage(String msg) {
        messagesBox.getChildren().add(bubble(msg, false));
        autoScroll();
    }

    private HBox bubble(String msg, boolean isUser) {
        String time = LocalTime.now().format(timeFmt);

        Label textLabel = new Label(msg);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(360);
        textLabel.getStyleClass().add("bubble");
        textLabel.getStyleClass().add(isUser ? "bubble-user" : "bubble-bot");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("bubble-time");

        VBox content = new VBox(4);
        content.getChildren().addAll(textLabel, timeLabel);

        HBox row = new HBox(content);
        row.getStyleClass().add("bubble-row");
        row.setFillHeight(true);
        row.setStyle(isUser ? "-fx-alignment: CENTER_RIGHT;" : "-fx-alignment: CENTER_LEFT;");

        return row;
    }

    private void autoScroll() {
        if (scroll == null) return;
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    // =========================
    // BOT LOGIC (copie du back)
    // =========================
    private String generateProfessionalReply(String input) {
        String t = normalize(input);

        if (t.matches(".*\\b(salut|bonjour|bonsoir|cc|hey)\\b.*")) {
            return "Salut ! Dis-moi ton objectif : CV, entretien, stress, organisation… ou 'compétences + domaine' (ex: compétences industrie).";
        }

        String domain = detectDomain(t);
        boolean asksSkills = containsAny(t, "competence", "competences", "skills", "exige", "exigee", "exigence", "domaine", "secteur");

        if (domain != null && (asksSkills || isDomainOnly(t))) {
            return skillsByDomain(domain);
        }

        if (asksSkills && domain == null) {
            return """
Tu veux les compétences pour quel domaine ?
Business, Management, Créatif, Santé, Éducation, Juridique, Industrie, Agriculture,
Tourisme, Sport, Art, Transport, Énergie, Médias, Sécurité, Immobilier, Culture, Environnement.
Exemple : "compétences tourisme"
""";
        }

        if (containsAny(t, "cv", "resume", "curriculum")) {
            return """
Pour améliorer ton CV :
1) Mets un titre clair (poste visé)
2) Résumé en 3 lignes (profil + forces + objectif)
3) Expériences : verbes d’action + résultats chiffrés
4) Compétences triées (techniques / soft skills)
5) 1 page si possible
Tu veux un CV pour quel domaine et quel niveau (junior/stage/confirmé) ?
""";
        }

        if (containsAny(t, "entretien", "interview", "recruteur")) {
            return """
Pour un entretien :
- Prépare 5 histoires STAR (Situation, Tâche, Action, Résultat)
- Fais une présentation 60 secondes (qui tu es, ce que tu sais faire, ce que tu veux)
- Prépare des questions au recruteur (équipe, missions, attentes à 3 mois)
Donne-moi le poste visé et je te propose une liste de questions + réponses.
""";
        }

        if (containsAny(t, "stress", "angoisse", "pression", "panic", "peur")) {
            return """
Pour gérer le stress au travail :
- Clarifie ce qui est sous ton contrôle (priorités, plan)
- Découpe en micro-tâches (10–20 min)
- Respiration 4-4-6 (4s inspire, 4s pause, 6s expire) x 5
Dis-moi la situation (deadline, conflit, surcharge) et je t’aide à faire un plan.
""";
        }

        if (containsAny(t, "conflit", "dispute", "probleme", "tension", "collegue", "manager")) {
            return """
Gestion de conflit (méthode simple) :
1) Décrire le fait sans jugement (“Quand X arrive…”)
2) Exprimer l’impact (“ça me bloque / ça retarde…”)
3) Proposer une solution (“Je propose qu’on…”)
4) Valider un accord (qui fait quoi / quand)
Tu veux gérer un conflit avec un collègue ou un manager ?
""";
        }

        if (containsAny(t, "organis", "priorit", "productiv", "deadline", "planning", "charge")) {
            return """
Organisation efficace :
- Liste tout (brain dump)
- Classe en Urgent/Important
- 3 priorités max par jour
- Bloque des créneaux (deep work 45 min)
Dis-moi tes tâches et ta deadline, je te fais un planning.
""";
        }

        if (containsAny(t, "carri", "promotion", "evolution", "salaire")) {
            return """
Pour évoluer :
- Choisis 1 compétence “levier” (ex: communication, leadership, data, dev…)
- Fais un plan 30 jours (apprendre + livrer une preuve)
- Demande du feedback (1 question précise)
Dans quel domaine tu travailles et quel objectif (poste/salaire/changement) ?
""";
        }

        return """
Je peux t’aider sur :
- CV / Lettre / LinkedIn
- Entretien (questions + réponses)
- Organisation / productivité
- Communication / conflits
- Stress / posture pro
- Compétences par domaine (ex: "industrie")
""";
    }

    private String detectDomain(String t) {
        if (containsAny(t, "business", "commerce", "marketing", "vente", "commercial")) return "BUSINESS";
        if (containsAny(t, "management", "manager", "leadership", "gestion d'equipe", "gestion equipe")) return "MANAGEMENT";
        if (containsAny(t, "creatif", "créatif", "design", "graphisme", "ux", "ui", "contenu", "content")) return "CREATIF";
        if (containsAny(t, "sante", "santé", "medical", "médical", "hopital", "hôpital", "infirm")) return "SANTE";
        if (containsAny(t, "education", "éducation", "enseign", "prof", "pedagog", "pédagog")) return "EDUCATION";
        if (containsAny(t, "juridique", "droit", "avocat", "loi", "contrat")) return "JURIDIQUE";
        if (containsAny(t, "industrie", "usine", "production", "maintenance", "qualite", "qualité", "lean", "5s")) return "INDUSTRIE";
        if (containsAny(t, "agriculture", "ferme", "elevage", "élevage", "culture")) return "AGRICULTURE";
        if (containsAny(t, "tourisme", "hotel", "hôtel", "voyage", "guide")) return "TOURISME";
        if (containsAny(t, "sport", "coach", "entrain", "entraînement", "fitness")) return "SPORT";
        if (containsAny(t, "art", "artiste", "peinture", "musique", "photo", "photographie")) return "ART";
        if (containsAny(t, "transport", "logistique", "livraison", "chauffeur")) return "TRANSPORT";
        if (containsAny(t, "energie", "énergie", "electricite", "électricité", "solaire", "gaz")) return "ENERGIE";
        if (containsAny(t, "medias", "médias", "journalisme", "montage", "reseaux", "réseaux", "presse")) return "MEDIAS";
        if (containsAny(t, "securite", "sécurité", "surveillance", "agent de securite", "agent de sécurité")) return "SECURITE";
        if (containsAny(t, "immobilier", "immo", "agent immobilier", "location", "vente immo")) return "IMMOBILIER";
        if (containsAny(t, "culture", "culturel", "musee", "musée", "evenement", "événement", "patrimoine")) return "CULTURE";
        if (containsAny(t, "environnement", "ecologie", "écologie", "climat", "rse", "durable")) return "ENVIRONNEMENT";
        return null;
    }

    private boolean isDomainOnly(String t) {
        return detectDomain(t) != null && t.split("\\s+").length <= 2;
    }

    private String skillsByDomain(String domain) {
        return switch (domain) {
            case "BUSINESS" -> """
Compétences exigées — Business :
- Analyse de marché, stratégie, segmentation, proposition de valeur
- Vente / négociation, relation client, CRM
- KPI, reporting, notions finance (marge, ROI)
Outils : Excel/Sheets, PowerPoint, CRM (Salesforce/HubSpot)
""";
            case "MANAGEMENT" -> """
Compétences exigées — Management :
- Leadership, coaching, feedback
- Planification, priorisation, délégation
- Gestion de conflit, conduite de réunion, décision
Outils : Jira/Trello, Notion, méthodes Agile
""";
            case "INDUSTRIE" -> """
Compétences exigées — Industrie :
- Process, qualité (5S/Lean/ISO), sécurité
- Respect procédures, traçabilité, amélioration continue
Outils : ERP/MES, checklists, tableaux de suivi
""";
            default -> "Je n’ai pas reconnu le domaine. Dis : Business, Industrie, etc.";
        };
    }

    private boolean containsAny(String text, String... keys) {
        for (String k : keys) if (text.contains(k)) return true;
        return false;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String lower = s.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("’", "'")
                .trim();
    }
}
