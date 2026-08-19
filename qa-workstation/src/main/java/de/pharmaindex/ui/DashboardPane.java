package de.pharmaindex.ui;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardPane extends BorderPane {

    private final CatalogApiClient client;
    private final Label status = new Label("Kennzahlen der Datenproduktion");
    private final GridPane kpis = new GridPane();
    private final PieChart severityChart = new PieChart();
    private final BarChart<String, Number> atcChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    public DashboardPane(CatalogApiClient client) {
        this.client = client;
        setPadding(new Insets(16));

        Button refresh = new Button("Aktualisieren");
        refresh.getStyleClass().add("primary");
        refresh.setOnAction(event -> reload());

        kpis.setHgap(12);
        kpis.setVgap(12);
        severityChart.setTitle("Offene Findings");
        severityChart.setLegendVisible(true);
        severityChart.setLabelsVisible(true);
        atcChart.setTitle("Präparate nach ATC-Kapitel");
        atcChart.setLegendVisible(false);
        atcChart.setCategoryGap(12);
        atcChart.getYAxis().setLabel("Anzahl");
        HBox charts = new HBox(16, severityChart, atcChart);
        HBox.setHgrow(severityChart, Priority.ALWAYS);
        HBox.setHgrow(atcChart, Priority.ALWAYS);
        status.getStyleClass().add("status");

        VBox box = new VBox(14, refresh, kpis, charts, status);
        VBox.setVgrow(charts, Priority.ALWAYS);
        setCenter(box);
        reload();
    }

    private void reload() {
        status.setText("Lade Betriebskennzahlen …");
        Task<DashboardDto> task = new Task<>() {
            @Override
            protected DashboardDto call() throws Exception {
                return client.dashboard();
            }
        };
        task.setOnSucceeded(event -> render(task.getValue()));
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void render(DashboardDto data) {
        kpis.getChildren().clear();
        kpis.add(kpi("Präparate", String.valueOf(data.productCount()), "im Katalog"), 0, 0);
        kpis.add(kpi("Aktiv", String.valueOf(data.activeCount()), "lieferfähig"), 1, 0);
        kpis.add(kpi("Offene Findings", String.valueOf(data.openFindings()), "QA-Backlog"), 2, 0);
        kpis.add(kpi("Errors", String.valueOf(data.errorFindings()), "sofort prüfen"), 3, 0);
        kpis.add(kpi("Matching-Index", String.valueOf(data.matchingIndexSize()), "Trigramme im RAM"), 4, 0);

        severityChart.setData(FXCollections.observableArrayList(
                data.bySeverity().stream()
                        .map(item -> new PieChart.Data(item.name() + " (" + item.count() + ")", item.count()))
                        .toList()
        ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.byAtcChapter().stream().limit(8).forEach(item -> {
            String label = item.name().length() > 18 ? item.name().substring(0, 16) + "…" : item.name();
            series.getData().add(new XYChart.Data<>(label, item.count()));
        });
        atcChart.getData().setAll(series);
        status.setText("Index " + data.matchingIndexSize() + " · Imports " + data.importJobs()
                + " · Warnings " + data.warningFindings() + " · Infos " + data.infoFindings());
    }

    private static VBox kpi(String title, String value, String hint) {
        Label caption = new Label(title);
        caption.getStyleClass().add("kpi-title");
        Label amount = new Label(value);
        amount.getStyleClass().add("kpi-value");
        Label sub = new Label(hint);
        sub.getStyleClass().add("kpi-hint");
        VBox card = new VBox(4, caption, amount, sub);
        card.getStyleClass().add("kpi-card");
        return card;
    }
}
