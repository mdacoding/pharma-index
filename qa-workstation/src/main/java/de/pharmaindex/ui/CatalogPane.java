package de.pharmaindex.ui;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CatalogPane extends BorderPane {

    private final CatalogApiClient client;
    private final TableView<ProductRow> table = new TableView<>();
    private final TableView<RevisionRow> revisions = new TableView<>();
    private final Label detail = new Label("Präparat auswählen, um Stammdaten und Revisionen zu sehen.");
    private final Label status = new Label("Bereit");

    public CatalogPane(CatalogApiClient client) {
        this.client = client;
        setPadding(new Insets(16));

        TextField search = new TextField();
        search.setPromptText("Suche nach Name, Wirkstoff oder PZN …");
        HBox.setHgrow(search, Priority.ALWAYS);
        Button load = new Button("Suchen");
        load.getStyleClass().add("primary");
        load.setOnAction(event -> reload(search.getText()));
        search.setOnAction(event -> reload(search.getText()));
        HBox toolbar = new HBox(10, search, load);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
                column("PZN", "pzn", 90),
                column("Handelsname", "name", 260),
                column("Wirkstoff", "ingredient", 140),
                column("ATC", "atc", 80),
                column("Kapitel", "atcGroup", 180),
                column("Status", "status", 90)
        );
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row != null) {
                showDetail(row);
            }
        });

        detail.setWrapText(true);
        detail.getStyleClass().add("detail-text");
        revisions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        revisions.getColumns().addAll(
                revisionColumn("Typ", "changeType", 90),
                revisionColumn("Name", "name", 200),
                revisionColumn("ATC", "atcCode", 80),
                revisionColumn("Status", "status", 90),
                revisionColumn("Zeit", "changedAt", 160)
        );
        Label historyTitle = new Label("Stammdaten-Revisionen");
        historyTitle.getStyleClass().add("section-title");
        VBox right = new VBox(10, detail, historyTitle, revisions);
        VBox.setVgrow(revisions, Priority.ALWAYS);
        right.setPadding(new Insets(0, 0, 0, 8));
        right.setPrefWidth(420);

        SplitPane split = new SplitPane(table, right);
        split.setDividerPositions(0.62);
        status.getStyleClass().add("status");
        VBox box = new VBox(10, toolbar, split, status);
        VBox.setVgrow(split, Priority.ALWAYS);
        setCenter(box);
        reload("");
    }

    private void showDetail(ProductRow row) {
        detail.setText("""
                PZN %s
                %s
                Hersteller: %s
                Wirkstoff: %s
                ATC: %s (%s)
                Stärke / Form: %s · %s
                Packung: %s
                AVP: %s EUR · Rx: %s
                """.formatted(
                row.getPzn(),
                row.getName(),
                row.getManufacturer(),
                nullToDash(row.getIngredient()),
                nullToDash(row.getAtc()),
                nullToDash(row.getAtcGroup()),
                nullToDash(row.getStrength()),
                nullToDash(row.getForm()),
                nullToDash(row.getPackageSize()),
                row.getPrice(),
                row.isRx() ? "ja" : "nein"
        ));
        Task<RevisionDto[]> task = new Task<>() {
            @Override
            protected RevisionDto[] call() throws Exception {
                return client.revisions(row.getPzn());
            }
        };
        task.setOnSucceeded(event -> revisions.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(task.getValue()).map(RevisionRow::from).toList()
        )));
        task.setOnFailed(event -> status.setText("Historie: " + task.getException().getMessage()));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void reload(String query) {
        status.setText("Lade Katalog …");
        Task<PageResponse<ProductDto>> task = new Task<>() {
            @Override
            protected PageResponse<ProductDto> call() throws Exception {
                return client.search(query);
            }
        };
        task.setOnSucceeded(event -> {
            var page = task.getValue();
            table.setItems(FXCollections.observableArrayList(
                    page.content().stream().map(ProductRow::from).toList()
            ));
            status.setText(page.totalElements() + " Präparate geladen");
        });
        task.setOnFailed(event -> status.setText("Fehler: " + task.getException().getMessage()));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static TableColumn<ProductRow, String> column(String title, String property, double width) {
        TableColumn<ProductRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private static TableColumn<RevisionRow, String> revisionColumn(String title, String property, double width) {
        TableColumn<RevisionRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    public static class ProductRow {
        private final String pzn;
        private final String name;
        private final String manufacturer;
        private final String ingredient;
        private final String atc;
        private final String atcGroup;
        private final String strength;
        private final String form;
        private final String packageSize;
        private final String price;
        private final boolean rx;
        private final String status;

        public ProductRow(String pzn, String name, String manufacturer, String ingredient, String atc, String atcGroup,
                          String strength, String form, String packageSize, String price, boolean rx, String status) {
            this.pzn = pzn;
            this.name = name;
            this.manufacturer = manufacturer;
            this.ingredient = ingredient;
            this.atc = atc;
            this.atcGroup = atcGroup;
            this.strength = strength;
            this.form = form;
            this.packageSize = packageSize;
            this.price = price;
            this.rx = rx;
            this.status = status;
        }

        static ProductRow from(ProductDto dto) {
            return new ProductRow(
                    dto.pzn(),
                    dto.name(),
                    dto.manufacturer(),
                    dto.activeIngredient(),
                    dto.atcCode(),
                    dto.atcGroup(),
                    dto.strength(),
                    dto.form(),
                    dto.packageSize(),
                    dto.pharmacyPrice() == null ? "—" : dto.pharmacyPrice().toPlainString(),
                    dto.prescriptionRequired(),
                    dto.status()
            );
        }

        public String getPzn() { return pzn; }
        public String getName() { return name; }
        public String getManufacturer() { return manufacturer; }
        public String getIngredient() { return ingredient; }
        public String getAtc() { return atc; }
        public String getAtcGroup() { return atcGroup; }
        public String getStrength() { return strength; }
        public String getForm() { return form; }
        public String getPackageSize() { return packageSize; }
        public String getPrice() { return price; }
        public boolean isRx() { return rx; }
        public String getStatus() { return status; }
    }

    public static class RevisionRow {
        private final String changeType;
        private final String name;
        private final String atcCode;
        private final String status;
        private final String changedAt;

        public RevisionRow(String changeType, String name, String atcCode, String status, String changedAt) {
            this.changeType = changeType;
            this.name = name;
            this.atcCode = atcCode;
            this.status = status;
            this.changedAt = changedAt;
        }

        static RevisionRow from(RevisionDto dto) {
            return new RevisionRow(
                    dto.changeType(),
                    dto.name(),
                    dto.atcCode(),
                    dto.status(),
                    dto.changedAt() == null ? "" : dto.changedAt().toString()
            );
        }

        public String getChangeType() { return changeType; }
        public String getName() { return name; }
        public String getAtcCode() { return atcCode; }
        public String getStatus() { return status; }
        public String getChangedAt() { return changedAt; }
    }
}
