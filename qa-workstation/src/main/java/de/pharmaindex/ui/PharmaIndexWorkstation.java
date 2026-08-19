package de.pharmaindex.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PharmaIndexWorkstation extends Application {

    static final String API_URL = System.getProperty("pharma.index.url", "http://localhost:8080");
    static final String API_KEY = System.getProperty("pharma.index.key", "demo-partner-key");

    @Override
    public void start(Stage stage) {
        CatalogApiClient client = new CatalogApiClient(API_URL, API_KEY);

        Label brand = new Label("PharmaIndex");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("Stammdatenproduktion  ·  Matching  ·  Qualitätssicherung");
        subtitle.getStyleClass().add("subtitle");
        VBox brandBox = new VBox(2, brand, subtitle);

        Label connection = new Label("API  " + API_URL);
        connection.getStyleClass().add("connection");
        HBox header = new HBox(16, brandBox, connection);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(18, 24, 18, 24));

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("main-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Dashboard", new DashboardPane(client)));
        tabs.getTabs().add(new Tab("Katalog", new CatalogPane(client)));
        tabs.getTabs().add(new Tab("Matching", new MatchingPane(client)));
        tabs.getTabs().add(new Tab("Qualitätssicherung", new QualityPane(client)));

        Label footer = new Label("Synthetische Demodaten  ·  kein medizinischer Rat  ·  Portfolio-Projekt");
        footer.getStyleClass().add("footer");
        footer.setPadding(new Insets(8, 24, 10, 24));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabs);
        root.setBottom(footer);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/workstation.css").toExternalForm());
        stage.setTitle("PharmaIndex – QA-Workstation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
