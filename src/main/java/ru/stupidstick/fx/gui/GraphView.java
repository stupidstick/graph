package ru.stupidstick.fx.gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.stupidstick.Main;

import java.io.IOException;


public class GraphView extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // create the scene
        stage.setTitle("Network view");

        Parent root = FXMLLoader.load(Main.class.getResource("/graph.fxml"));

        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
