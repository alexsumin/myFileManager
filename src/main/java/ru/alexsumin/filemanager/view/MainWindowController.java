package ru.alexsumin.filemanager.view;


import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import ru.alexsumin.filemanager.model.MyTreeCell;
import ru.alexsumin.filemanager.model.TreeItemWithLoading;
import ru.alexsumin.filemanager.util.FileCopyTask;
import ru.alexsumin.filemanager.util.FileDeleteTask;
import ru.alexsumin.filemanager.util.FileRenameTask;
import ru.alexsumin.filemanager.util.FileRunTask;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainWindowController {

    public static final ExecutorService EXEC = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    public static List<TreeItemWithLoading> systemDirectories = new ArrayList();
    public static TreeItemWithLoading root;
    public boolean isWindows;
    @FXML
    private TreeView<Path> treeView = new TreeView<>();
    @FXML
    private Button copyButton = new Button();
    @FXML
    private Button cutButton = new Button();
    @FXML
    private Button pasteButton = new Button();
    @FXML
    private Button renameButton = new Button();
    @FXML
    private Button deleteButton = new Button();
    @FXML
    private Button newDirButton = new Button();
    @FXML
    private Button openButton = new Button();
    private Path copiedFile;
    private Path target;
    private Path tempFile;
    private boolean isCutted;
    private TreeItemWithLoading selectedItem;
    @FXML
    private ArrayList<Button> buttons = new ArrayList();

    @FXML
    private void initialize() {
        isWindows = isWindows();
        treeView.setCellFactory(param -> new MyTreeCell());
        configureTreeView(treeView);
        disableButtons();
        pasteButton.setDisable(true);
        openButton.setDisable(true);
    }

    private void configureTreeView(TreeView treeView) {

        if (isWindows()) {
            String hostName = "This PC";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException x) {
            }
            root = new TreeItemWithLoading(Paths.get(hostName));

            Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();

            for (Path name : rootDirectories) {

                TreeItemWithLoading systemNode = new TreeItemWithLoading(name);
                root.getChildren().add(systemNode);
                systemDirectories.add(systemNode);
            }
        } else {
            root = new TreeItemWithLoading(Paths.get("/"));
        }


        treeView.setRoot(root);
        //root.setExpanded(true);
        treeView.setEditable(false);
        EventDispatcher treeOriginal = treeView.getEventDispatcher();
        treeView.setEventDispatcher(new CellEventDispatcher(treeOriginal));

        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    openButton.setDisable(false);
                    selectedItem = (TreeItemWithLoading) newValue;
                    if (selectedItem == null | !isEditableItem(selectedItem)) {
                        disableButtons();

                        pasteButton.setDisable(true);
                    } else {
                        enableButtons();
                        pasteButton.setDisable(false);
                    }
                });


        treeView.setOnMouseClicked(t -> {
            if (t.getClickCount() == 2 && selectedItem != null) {
                if (isWindows() && selectedItem == root) {
                    openFile();
                }
            }
        });

    }

    private void disableButtons() {
        buttons.stream().forEach(button -> button.setDisable(true));
    }

    private void enableButtons() {
        buttons.stream().forEach(button -> button.setDisable(false));
    }

    private boolean isEditableItem(TreeItemWithLoading item) {

        if (item.equals(root)) {
            return false;
        }
        if (isWindows()) {
            for (TreeItemWithLoading t : systemDirectories) {
                if (item.equals(t))
                    return false;
            }
        }
        return true;

    }

    private void expandTreeView(TreeItemWithLoading item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
        }
    }

    private void collapseTreeView(TreeItemWithLoading item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(false);
        }
    }

    @FXML
    private void openRenameDialog() {

        TextInputDialog dialog = new TextInputDialog(selectedItem.getValue().getFileName().toString());
        dialog.setTitle("Renaming file");

        dialog.setHeaderText("Rename file: " + selectedItem.getValue().toString());
        dialog.setContentText("Please enter new name: ");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/view/BasicApplication.css").toExternalForm());

        dialog.showAndWait().ifPresent(newName -> {
            if (newName != null) {
                renameFile(newName);

            }
        });


    }

    private void renameFile(String newName) {

        Path newPath = Paths.get(selectedItem.getValue().getParent() + File.separator + newName);
        FileRenameTask fileRenameTask = new FileRenameTask(selectedItem.getValue(), newPath);

        fileRenameTask.setOnSucceeded(event -> selectedItem.setValue(newPath));
        fileRenameTask.setOnFailed(event -> showExceptionDialog(fileRenameTask.getException()));
        EXEC.submit(fileRenameTask);


    }

    private void showExceptionDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Exception Dialog");
        alert.setHeight(400);
        alert.setHeaderText("Ooops, Exception here");
        alert.setContentText(throwable.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
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

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    @FXML
    private void deleteFile() {
        if (selectedItem == null) {
            return;
        }
        FileDeleteTask deleteTask = new FileDeleteTask(selectedItem.getValue());

        deleteTask.setOnSucceeded(event -> selectedItem.getParent().getChildren().remove(selectedItem));
        deleteTask.setOnFailed(evt -> showExceptionDialog(deleteTask.getException()));
        EXEC.submit(deleteTask);

    }


    @FXML
    private void openFile() {
        if (selectedItem == null) {
            return;
        }
        if (!selectedItem.isLeaf()) {
            if (!selectedItem.isExpanded()) {
                expandTreeView(selectedItem);
            } else {
                collapseTreeView(selectedItem);
            }
        } else {
            FileRunTask fileRunTask = new FileRunTask(selectedItem.getValue());
            fileRunTask.setOnFailed(event -> showExceptionDialog(fileRunTask.getException()));
            EXEC.submit(fileRunTask);
        }


    }

    @FXML
    private void copyFile() {
        if (selectedItem != null) {
            copiedFile = selectedItem.getValue();
        }
        isCutted = false;
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
            target = (Files.isDirectory(selectedItem.getValue()) ? selectedItem.getValue() : selectedItem.getParent().getValue());
            tempFile = copiedFile.getFileName();
            FileCopyTask copyTask = new FileCopyTask(copiedFile, target, isCutted);

            copyTask.setOnSucceeded(event -> {
                if (isCutted) isCutted = false;
                addNewItemAfterIO(target + File.separator + tempFile);

            });
            copyTask.setOnFailed(evt -> showExceptionDialog(copyTask.getException()));
            EXEC.submit(copyTask);

        }
    }

    private void addNewItemAfterIO(String newName) {
        TreeItemWithLoading newItem = new TreeItemWithLoading(Paths.get(newName));
        if (!Files.isDirectory(selectedItem.getValue(), LinkOption.NOFOLLOW_LINKS)) {
            TreeItemWithLoading parent = (TreeItemWithLoading) selectedItem.getParent();
            parent.getChildren().add(newItem);
        } else if (selectedItem.isExpanded()) {
            selectedItem.getChildren().add(newItem);
        }
    }


    @FXML
    private void createNewItemDirectory() {
        if (selectedItem != null && selectedItem.getValue() != null) {
            String newDirectory = createDirectory();
            if (newDirectory != null) {
                TreeItemWithLoading addItem = new TreeItemWithLoading(Paths.get(newDirectory));
                if (selectedItem.isLeaf()) selectedItem.setLeaf(false);
                if (selectedItem.isExpanded()) {
                    selectedItem.getChildren().add(addItem);
                }
            }
        }
    }

    private String createDirectory() {
        Path parent = selectedItem.getValue().getParent();
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
                break;
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
