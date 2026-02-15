package org.controller.Competences;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.model.Competences.Competence;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Dashboard {

    @FXML
    private PieChart pieChart;

    @FXML
    private Label lblTop;

    public void setData(List<Competence> competences) {

        if (competences == null || competences.isEmpty()) {
            lblTop.setText("Aucune donnée.");
            return;
        }

        Map<String, Long> counts =
                competences.stream()
                        .collect(Collectors.groupingBy(
                                c -> {
                                    String t = c.getType();
                                    return (t == null || t.isBlank())
                                            ? "Non défini"
                                            : t;
                                },
                                Collectors.counting()
                        ));

        long total = competences.size();

        Map<String, Long> sorted =
                counts.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        var pieData = FXCollections.<PieChart.Data>observableArrayList();

        for (var e : sorted.entrySet()) {
            double pct = (e.getValue() * 100.0) / total;

            pieData.add(new PieChart.Data(
                    e.getKey() + String.format(Locale.US, " (%.1f%%)", pct),
                    e.getValue()
            ));
        }

        pieChart.setTitle("Répartition des compétences");
        pieChart.setData(pieData);

        // type dominant
        var top = sorted.entrySet().iterator().next();
        double topPct = (top.getValue() * 100.0) / total;

        lblTop.setText(
                "Type dominant : "
                        + top.getKey()
                        + String.format(Locale.US, " (%.1f%%)", topPct)
        );
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) pieChart.getScene().getWindow();
        stage.close();
    }
}
