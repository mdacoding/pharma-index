package de.pharmaindex.ui;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class QualityPane extends BorderPane {

    private final CatalogApiClient client;
    private final TableView<FindingRow> table = new TableView<>();
    private final Label status = new Label("Offene Findings aus dem Regelwerk – ERROR zuerst prüfen.");

    public QualityPane(CatalogApiClient client) {
        this.client = client;
        setPadding(new Insets(16));

        Button refresh = new Button("Aktualisieren");
        Button scan = new Button("QA-Lauf starten");
        Button resolve = new Button("Als geprüft markieren");
        scan.getStyleClass().add("primary");
        refresh.setOnAction(event -> reload());
        scan.setOnAction(event -> runScan());
        resolve.setOnAction(event -> resolveSelected());

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setRowFactory(view -> new TableRow<>() {
            @Override
            protected void updateItem(FindingRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-error", "row-warning", "row-info");
                if (item != null && !empty) {
                    switch (item.getSeverity()) {
                        case "ERROR" -> getStyleClass().add("row-error");
                        case "WARNING" -> getStyleClass().add("row-warning");
                        default -> getStyleClass().add("row-info");
                    }
                }
            }
        });
        table.getColumns().addAll(
                column("Schwere", "severity", 90),
                column("Typ", "type", 160),
                column("PZN", "pzn", 90),
                column("Präparat", "productName", 240),
                column("Meldung", "message", 420)
        );
        status.getStyleClass().add("status");

        HBox toolbar = new HBox(10, refresh, scan, resolve);
        VBox box = new VBox(8, toolbar, table, status);
        VBox.setVgrow(table, Priority.ALWAYS);
        setCenter(box);
        reload();
    }

    private void resolveSelected() {
        FindingRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Bitte ein Finding auswählen.");
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                client.resolve(selected.getId());
                return null;
            }
        };
        task.setOnSucceeded(event -> reload());
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        start(task);
    }

    private void reload() {
        status.setText("Lade Findings …");
        Task<QualityFindingDto[]> task = new Task<>() {
            @Override
            protected QualityFindingDto[] call() throws Exception {
                return client.findings();
            }
        };
        task.setOnSucceeded(event -> {
            QualityFindingDto[] findings = task.getValue();
            table.setItems(FXCollections.observableArrayList(
                    java.util.Arrays.stream(findings).map(FindingRow::from).toList()
            ));
            status.setText(findings.length + " offene Findings");
        });
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        start(task);
    }

    private void runScan() {
        status.setText("Qualitätssicherung läuft …");
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return client.scan();
            }
        };
        task.setOnSucceeded(event -> reload());
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        start(task);
    }

    private static void start(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private static TableColumn<FindingRow, String> column(String title, String property, double width) {
        TableColumn<FindingRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    public static class FindingRow {
        private final long id;
        private final String severity;
        private final String type;
        private final String pzn;
        private final String productName;
        private final String message;

        public FindingRow(long id, String severity, String type, String pzn, String productName, String message) {
            this.id = id;
            this.severity = severity;
            this.type = type;
            this.pzn = pzn;
            this.productName = productName;
            this.message = message;
        }

        static FindingRow from(QualityFindingDto dto) {
            return new FindingRow(dto.id(), dto.severity(), dto.type(), dto.pzn(), dto.productName(), dto.message());
        }

        public long getId() { return id; }
        public String getSeverity() { return severity; }
        public String getType() { return type; }
        public String getPzn() { return pzn; }
        public String getProductName() { return productName; }
        public String getMessage() { return message; }
    }
}
