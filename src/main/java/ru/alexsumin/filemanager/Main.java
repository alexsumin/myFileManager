package ru.alexsumin.filemanager;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.alexsumin.filemanager.view.FileManagerController;

import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private static final Image APP = new Image(Main.class.getResourceAsStream("/images/app.png"));

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/FileManager.fxml"));
        primaryStage.setTitle("File Manager");
        primaryStage.setScene(new Scene(root, 640, 600));
        primaryStage.getIcons().add(APP);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                shutdown();
                Platform.exit();
                System.exit(0);
            }
        });


    }

    private void shutdown() {
        try {
            FileManagerController.EXEC.shutdown();
            FileManagerController.EXEC.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!FileManagerController.EXEC.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            FileManagerController.EXEC.shutdownNow();
        }
    }
}
