package ru.stupidstick.fx.controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import ru.stupidstick.fx.graph.VisEdge;
import ru.stupidstick.fx.graph.VisGraph;
import ru.stupidstick.fx.graph.VisNode;
import ru.stupidstick.graph.MatrixGraph;
import ru.stupidstick.graph.Vertex;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;

public class GraphController implements Initializable {

    @FXML
    private Pane sceneContainer;

    @FXML
    private TextField nameField;

    @FXML
    private TextField dataField;

    @FXML
    private TextField fromField;

    @FXML
    private TextField toField;

    @FXML
    private TextField maxHeightField;

    private Browser browser;

    private List<Vertex<String, String>> vertices = new ArrayList<>();

    private MatrixGraph<String, String, String> graph = new MatrixGraph<>(true);


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 0; i < 7; i++) {
            var vertex = graph.insertVertex(String.valueOf(i), String.valueOf(i));
            vertices.add(vertex);
        }
        graph.insertEdge(vertices.get(0), vertices.get(1));
        graph.insertEdge(vertices.get(1), vertices.get(2));
        graph.insertEdge(vertices.get(2), vertices.get(3));
        graph.insertEdge(vertices.get(3), vertices.get(1));
        graph.insertEdge(vertices.get(3), vertices.get(4));
        graph.insertEdge(vertices.get(4), vertices.get(5));
        graph.insertEdge(vertices.get(5), vertices.get(6));
        graph.insertEdge(vertices.get(6), vertices.get(4));
        updateBrowser();
    }

    @FXML
    public void addVertex() {
        String name = nameField.getText();
        String data = dataField.getText();
        if (name.isEmpty() || data.isEmpty()) {
            return;
        }
        vertices.add(graph.insertVertex(name, data));
        updateBrowser();
    }

    @FXML
    public void addEdge() {
        String from = fromField.getText();
        String to = toField.getText();
        String data = dataField.getText();
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }
        Vertex<String, String> v1 = vertices.stream().filter(v -> v.getName().equals(from)).findFirst().orElse(null);
        Vertex<String, String> v2 = vertices.stream().filter(v -> v.getName().equals(to)).findFirst().orElse(null);
        if (v1 == null || v2 == null) {
            return;
        }
        var edge = graph.insertEdge(v1, v2);
        edge.setData(data);
        updateBrowser();
    }

    @FXML
    public void removeVertex() {
        String name = nameField.getText();
        if (name.isEmpty()) {
            return;
        }
        var vertex = vertices.stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);
        if (vertex == null) {
            return;
        }
        graph.removeVertex(vertex);
        vertices.remove(vertex);
        updateBrowser();
    }

    @FXML
    public void deleteEdge() {
        String from = fromField.getText();
        String to = toField.getText();
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }
        Vertex<String, String> v1 = vertices.stream().filter(v -> v.getName().equals(from)).findFirst().orElse(null);
        Vertex<String, String> v2 = vertices.stream().filter(v -> v.getName().equals(to)).findFirst().orElse(null);
        if (v1 == null || v2 == null) {
            return;
        }
        graph.deleteEdge(v1, v2);
        updateBrowser();
    }

    @FXML
    public void ostav() {
        int maxHeight = Integer.parseInt(maxHeightField.getText());
        if (maxHeight < 2)
            return;

        Stage stage = new Stage();
        Map<Vertex<String, String>, VisNode> vertexMap = new LinkedHashMap<>();

        var ostavEdges = graph.findSpanningTreeWithHeightLimit(maxHeight);
        ostavEdges.forEach(e -> {
            vertexMap.put(e.getFrom(), new VisNode(new Random().nextInt(), e.getFrom().toString()));
            vertexMap.put(e.getTo(), new VisNode(new Random().nextInt(), e.getTo().toString()));

        });
        VisGraph visGraph = new VisGraph();
        visGraph.addNodes(vertexMap.values().toArray(VisNode[]::new));
        visGraph.addEdges(ostavEdges.stream().map(edge -> new VisEdge(
                vertexMap.get(edge.getFrom()),
                vertexMap.get(edge.getTo()),
                "to",
                edge.getData()
        )).toArray(VisEdge[]::new));

        Browser browser = new Browser(visGraph);
        Scene scene = new Scene(browser, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void updateBrowser() {
        browser = new Browser(graph.toVisGraph());
        sceneContainer.getChildren().setAll(browser);
    }



}

class Browser extends Region {

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();
    private final VisGraph graph;

    public Browser(VisGraph g) {
        this.graph = g;
        getStyleClass().add("browser");

        String tmpDir = "tmp/" + UUID.randomUUID();
        try {
            FileUtils.cleanDirectory(new File("tmp"));
            File file = new File(tmpDir + "/baseGraph.html");
            file.mkdirs();
            File directory = new File(tmpDir + "/graphLibraries");
            Files.copy(Path.of("src/main/resources/baseGraph.html"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileUtils.copyDirectory(new File("src/main/resources/graphLibraries"), directory);
        } catch (Exception e) {
            e.printStackTrace();
        }


        webEngine.load("file:/" + Paths.get("").toAbsolutePath() + "/" + tmpDir + "/baseGraph.html");

        getChildren().add(webView);
        setGraph();

    }

    private void setGraph() {

        String script = "setTheData(" + graph.getNodesJson() + "," + graph.getEdgesJson() + ")";
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED)
                webEngine.executeScript(script);
        });
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
}
