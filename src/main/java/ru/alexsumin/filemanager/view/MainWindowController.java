package ru.alexsumin.filemanager.view;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import ru.alexsumin.filemanager.model.MyTreeCell;
import ru.alexsumin.filemanager.model.TreeItemWithLoading;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainWindowController {

    public static final ExecutorService EXEC = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image pc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);
    @FXML
    private TreeView<File> treeView = new TreeView<>();
    @FXML
    private Button copyButton = new Button();
    @FXML
    private Button cutButton = new Button();
    @FXML
    private Button pasteButton = new Button();
    @FXML
    private Button renameButton = new Button();
    @FXML
    private Button removeButton = new Button();
    @FXML
    private Button newDirButton = new Button();
    @FXML
    private Button openButton = new Button();
    private File currentFile;
    private File copiedFile;
    private File target;
    private String tempFile;
    private boolean isCutted;
    private TreeItemWithLoading selectedItem;


    @FXML
    private void initialize() {
        treeView.setCellFactory(param -> new MyTreeCell());
        TreeItemWithLoading root = new TreeItemWithLoading(new File(System.getProperty("user.home")));
        treeView.setRoot(root);
        treeView.setEditable(true);
        EventDispatcher treeOriginal = treeView.getEventDispatcher();
        treeView.setEventDispatcher(new CellEventDispatcher(treeOriginal));

        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    selectedItem = (TreeItemWithLoading) newValue;
                });

        treeView.setOnMouseClicked(t -> {
            if (t.getClickCount() == 2 && selectedItem != null) {
                openFile();
            }
        });


    }


    @FXML
    private void openRenameDialog() {

        TextInputDialog dialog = new TextInputDialog(selectedItem.getValue().getName());
        dialog.setTitle("Renaming file");

        dialog.setHeaderText("Rename file: " + selectedItem.getValue().getName());
        dialog.setContentText("Please enter new name: ");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/view/BasicApplication.css").toExternalForm());

        dialog.showAndWait().ifPresent(response -> {
            if (response != null) {
                renameFile(response);

            }
        });


    }

    private void renameFile(String newName) {
        String newPath = selectedItem.getParent().getValue().getAbsolutePath() + File.separator + newName;
        try {
            Files.move(Paths.get(selectedItem.getValue().getAbsolutePath()),
                    Paths.get(newPath), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            showExceptionDialog(e);
        }
        selectedItem.getParent().getChildren().remove(selectedItem);
        addNewItemAfterIO(newPath);

    }

    private void showExceptionDialog(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Exception Dialog");
        alert.setHeight(400);
        alert.setHeaderText("Ooops, Exception here");
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/view/BasicApplication.css").toExternalForm());


        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().expandedProperty().addListener((l) -> {
            Platform.runLater(() -> {
                alert.getDialogPane().requestLayout();
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.sizeToScene();
            });
        });

        alert.showAndWait();
    }

    @FXML
    private void deleteFile() {
        if (selectedItem != null) {
            try {
                FileUtils.deleteDirectory(selectedItem.getValue());
            } catch (IOException e) {
                showExceptionDialog(e);
            }
            selectedItem.getParent().getChildren().remove(selectedItem);
        }

    }

    @FXML
    private void openFile() {
        if (!selectedItem.getValue().isDirectory()) {
            if (!SystemUtils.IS_OS_WINDOWS) {

                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("xdg-open " + selectedItem.getValue().getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            selectedItem.setExpanded(!selectedItem.isExpanded());
        }
    }

    @FXML
    private void copyFile() {
        if (selectedItem != null) {
            copiedFile = selectedItem.getValue();
        }
    }

    @FXML
    private void cutFile() {
        if (selectedItem != null) {
            copiedFile = selectedItem.getValue();
        }
        isCutted = true;
        selectedItem.getParent().getChildren().remove(selectedItem);
    }

    @FXML
    private void pasteFile() {

        if (selectedItem != null && copiedFile != null) {
            target = (selectedItem.getValue().isDirectory()) ? selectedItem.getValue() : selectedItem.getParent().getValue();
            tempFile = copiedFile.getName();
            try {
                if (isCutted) {
                    if (copiedFile.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(copiedFile, target, false);
                        FileUtils.deleteDirectory(copiedFile);
                    } else {
                        FileUtils.moveFileToDirectory(copiedFile, target, false);
                        copiedFile.delete();
                    }
                    isCutted = false;
                    copiedFile = null;
                } else {
                    if (new File("" + target + File.separator + copiedFile.getName()).exists()) {
                        throw new IOException();
                    }
                    if (copiedFile.isDirectory()) {
                        FileUtils.copyDirectoryToDirectory(copiedFile, target);
                    } else {
                        FileUtils.copyFileToDirectory(copiedFile, target);

                    }
                }
                addNewItemAfterIO("" + target + File.separator + tempFile);
            } catch (IOException e) {
                showExceptionDialog(e);
            }
            target = null;
        }
    }

    private void addNewItemAfterIO(String newName) {
        TreeItemWithLoading addItem = new TreeItemWithLoading(new File(newName));
        if (selectedItem.isLeaf()) selectedItem.setLeaf(false);
        if (selectedItem.isExpanded()) {
            selectedItem.getChildren().add(addItem);
        }
    }


    @FXML
    private void createNewItemDirectory(final ActionEvent event) {
        if (selectedItem != null && selectedItem.getValue() != null) {
            String newD = createDirectory();
            if (newD != null) {
                TreeItemWithLoading addItem = new TreeItemWithLoading(new File(newD));
                if (selectedItem.isLeaf()) selectedItem.setLeaf(false);
                if (selectedItem.isExpanded()) {
                    selectedItem.getChildren().add(addItem);
                }
            }
        }
    }

    private String createDirectory() {
        File file = (File) selectedItem.getValue();
        String parent = file.getPath();
        String newDir;
        while (true) {
            newDir = parent + File.separator + "NewDirectory" + String.valueOf(selectedItem.getNewDirCount());
            try {
                Files.createDirectory(Paths.get(newDir));
                break;
            } catch (FileAlreadyExistsException e) {
                continue;
            } catch (IOException e) {
                showExceptionDialog(e);
            }
        }
        return newDir;
    }

    class CellEventDispatcher implements EventDispatcher {

        private final EventDispatcher original;

        public CellEventDispatcher(EventDispatcher original) {
            this.original = original;
        }

        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            if (event instanceof KeyEvent && event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
                if ((((KeyEvent) event).getCode().equals(KeyCode.LEFT) ||
                        ((KeyEvent) event).getCode().equals(KeyCode.RIGHT))) {
                    event.consume();
                }
            }
            return original.dispatchEvent(event, tail);
        }
    }

}
