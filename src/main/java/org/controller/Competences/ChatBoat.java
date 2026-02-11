package org.controller.Competences;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatBoat {

    @FXML private VBox messagesBox;
    @FXML private TextField tfMessage;
    @FXML private ScrollPane scroll;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        addBotMessage("Salut 👋 Je suis ton chatbot pro. Dis-moi ce que tu veux améliorer : CV, entretien, communication, gestion du stress, organisation, conflit, carrière…");

        // Entrée = envoyer / Shift+Entrée = ne rien faire (tu peux changer si tu veux multiline)
        tfMessage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (e.isShiftDown()) return;
                e.consume();
                onSend();
            }
        });

        // ScrollPane transparent (au cas où)
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
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    // -----------------------------
    // BOT LOGIQUE (mots-clés pro)
    // -----------------------------
    private String generateProfessionalReply(String input) {
        String t = input.toLowerCase().trim();

        // salutations
        if (t.matches(".*\\b(salut|bonjour|bonsoir|cc|hey)\\b.*")) {
            return "Salut ! Dis-moi ton objectif : trouver un job, améliorer ton CV, préparer un entretien, mieux communiquer au travail…";
        }

        // CV
        if (containsAny(t, "cv", "resume", "curriculum")) {
            return """
                    Pour améliorer ton CV :
                    1) Mets un titre clair (poste visé)
                    2) Résumé en 3 lignes (profil + forces + objectif)
                    3) Expériences avec verbes d’action + résultats chiffrés
                    4) Compétences triées (techniques / soft skills)
                    5) 1 page si possible
                    Tu veux un CV pour quel domaine et quel niveau (junior/stage/confirmé) ?
                    """;
        }

        // Entretien
        if (containsAny(t, "entretien", "interview", "recruteur")) {
            return """
                    Pour un entretien :
                    - Prépare 5 histoires STAR (Situation, Tâche, Action, Résultat)
                    - Fais une présentation 60 secondes (qui tu es, ce que tu sais faire, ce que tu veux)
                    - Prépare des questions au recruteur (équipe, missions, attentes 3 mois)
                    Donne-moi le poste visé et je te propose une liste de questions + réponses.
                    """;
        }

        // Stress
        if (containsAny(t, "stress", "angoisse", "pression", "panic", "peur")) {
            return """
                    Pour gérer le stress au travail :
                    - Clarifie ce qui est sous ton contrôle (priorités, plan)
                    - Découpe en micro-tâches (10–20 min)
                    - Respiration 4-4-6 (4s inspire, 4s pause, 6s expire) x 5
                    Si tu veux, dis-moi la situation exacte (deadline, conflit, surcharge) et je t’aide à faire un plan.
                    """;
        }

        // Conflit
        if (containsAny(t, "conflit", "dispute", "problème", "tension", "collègue", "manager")) {
            return """
                    Gestion de conflit (méthode simple) :
                    1) Décrire le fait sans jugement (“Quand X arrive…”)
                    2) Exprimer l’impact (“ça me bloque / ça retarde…”)
                    3) Proposer une solution (“Je propose qu’on…”)
                    4) Valider un accord (qui fait quoi / quand)
                    Tu veux gérer un conflit avec un collègue ou un manager ?
                    """;
        }

        // Organisation / productivité
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

        // Compétences / carrière
        if (containsAny(t, "carri", "promotion", "évolution", "salaire", "compétence", "skill")) {
            return """
                    Pour évoluer :
                    - Choisis 1 compétence “levier” (ex: communication, leadership, data, dev…)
                    - Fais un plan 30 jours (apprendre + livrer une preuve)
                    - Demande du feedback (1 question précise)
                    Dans quel domaine tu travailles et quel objectif (poste/salaire/changement) ?
                    """;
        }

        // défaut
        return """
                Je peux t’aider sur :
                - CV / Lettre / LinkedIn
                - Entretien (questions + réponses)
                - Organisation / productivité
                - Communication / conflits
                - Stress / posture pro
                Dis-moi ton contexte (étudiant, stage, job) et ton objectif.
                """;
    }

    private boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
