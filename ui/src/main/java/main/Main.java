package main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) {
        Parent root = new JanelaChat();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
