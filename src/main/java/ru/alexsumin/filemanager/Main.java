package ru.alexsumin.filemanager;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static final Image APP = new Image(Main.class.getResourceAsStream("/images/app.png"));

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/FileManager.fxml"));
        primaryStage.setTitle("File Manager");
        primaryStage.setScene(new Scene(root, 640, 600));
        primaryStage.getIcons().add(APP);
        primaryStage.show();
    }
}
