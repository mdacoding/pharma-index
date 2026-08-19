package de.pharmaindex.ui;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MatchingPane extends BorderPane {

    private final CatalogApiClient client;
    private final TableView<MatchRow> table = new TableView<>();
    private final Label status = new Label("Tipp: „Paracetmol HEXAL“ oder „Ibuflam 400“ – Tippfehler und Freitext aus der Warenwirtschaft.");

    public MatchingPane(CatalogApiClient client) {
        this.client = client;
        setPadding(new Insets(16));

        TextField query = new TextField();
        query.setPromptText("Freitext aus Warenwirtschaft / Rezeptscan …");
        HBox.setHgrow(query, Priority.ALWAYS);
        Button run = new Button("Matchen");
        run.getStyleClass().add("primary");
        run.setOnAction(event -> match(query.getText()));
        query.setOnAction(event -> match(query.getText()));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
                column("Score", "score", 70),
                column("PZN", "pzn", 90),
                column("Name", "name", 240),
                column("Hersteller", "manufacturer", 120),
                column("ATC", "atc", 80),
                column("Begründung", "explanations", 360)
        );
        status.getStyleClass().add("status");

        HBox toolbar = new HBox(10, query, run);
        VBox box = new VBox(8, toolbar, table, status);
        VBox.setVgrow(table, Priority.ALWAYS);
        setCenter(box);
    }

    private void match(String text) {
        status.setText("Berechne Kandidaten über Trigramm-Index …");
        Task<MatchResponseDto> task = new Task<>() {
            @Override
            protected MatchResponseDto call() throws Exception {
                return client.match(text);
            }
        };
        task.setOnSucceeded(event -> {
            MatchResponseDto result = task.getValue();
            table.setItems(FXCollections.observableArrayList(
                    result.matches().stream().map(MatchRow::from).toList()
            ));
            status.setText(result.matches().size() + " Treffer aus Pool "
                    + result.candidatePoolSize() + " in " + result.durationMs() + " ms");
        });
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private static TableColumn<MatchRow, String> column(String title, String property, double width) {
        TableColumn<MatchRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    public static class MatchRow {
        private final String score;
        private final String pzn;
        private final String name;
        private final String manufacturer;
        private final String atc;
        private final String explanations;

        public MatchRow(String score, String pzn, String name, String manufacturer, String atc, String explanations) {
            this.score = score;
            this.pzn = pzn;
            this.name = name;
            this.manufacturer = manufacturer;
            this.atc = atc;
            this.explanations = explanations;
        }

        static MatchRow from(MatchCandidateDto dto) {
            String why = dto.explanations() == null ? "" : String.join(" · ", dto.explanations());
            return new MatchRow(
                    String.format("%.0f %%", dto.score() * 100),
                    dto.pzn(),
                    dto.name(),
                    dto.manufacturer(),
                    dto.atcCode(),
                    why
            );
        }

        public String getScore() { return score; }
        public String getPzn() { return pzn; }
        public String getName() { return name; }
        public String getManufacturer() { return manufacturer; }
        public String getAtc() { return atc; }
        public String getExplanations() { return explanations; }
    }
}
