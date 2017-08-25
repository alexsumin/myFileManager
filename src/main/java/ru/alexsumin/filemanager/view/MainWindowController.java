package ru.alexsumin.filemanager.view;


import javafx.application.Platform;
import javafx.event.ActionEvent;
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


    }


    private void configureTreeView(TreeView treeView) {

        if (isWindows()) {
            String hostName = "This PC";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException x) {
            }
            //root = new TreeItemWithLoading(new File(hostName));
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
        root.setExpanded(true);
        treeView.setEditable(false);
        EventDispatcher treeOriginal = treeView.getEventDispatcher();
        treeView.setEventDispatcher(new CellEventDispatcher(treeOriginal));


        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    selectedItem = (TreeItemWithLoading) newValue;

                    if (selectedItem != null)

                        if (!isEditableItem(selectedItem)) {

                            buttons.stream().forEach(button -> button.setDisable(true));
                            pasteButton.setDisable(true);

                        } else {
                            buttons.stream().forEach(button -> button.setDisable(false));
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

        TextInputDialog dialog = new TextInputDialog(selectedItem.getValue().toString());
        dialog.setTitle("Renaming file");

        dialog.setHeaderText("Rename file: " + selectedItem.getValue().toString());
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
//        String newPath = selectedItem.getParent().getValue().getAbsolutePath() + File.separator + newName;
//        try {
//            Files.move(Paths.get(selectedItem.getValue().getAbsolutePath()),
//                    Paths.get(newPath), StandardCopyOption.ATOMIC_MOVE);
//        } catch (IOException e) {
//            showExceptionDialog(e);
//        }
//        selectedItem.getParent().getChildren().remove(selectedItem);
//        addNewItemAfterIO(newPath);

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

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    @FXML
    private void deleteFile() {
//        if (selectedItem != null) {
//            try {
//                FileUtils.deleteDirectory(selectedItem.getValue());
//            } catch (IOException e) {
//                showExceptionDialog(e);
//            }
//            selectedItem.getParent().getChildren().remove(selectedItem);
//        }

    }

    @FXML
    private void openFile() {
//        if (!selectedItem.getValue().isDirectory()) {
//            if (!SystemUtils.IS_OS_WINDOWS) {
//
//                Runtime runtime = Runtime.getRuntime();
//                try {
//                    runtime.exec("xdg-open " + selectedItem.getValue().getAbsolutePath());
//                } catch (IOException e) {
//                    showExceptionDialog(e);
//                }
//            } else {
//                if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
//                    Desktop desktop = Desktop.getDesktop();
//                    try {
//                        desktop.open(selectedItem.getValue());
//                        Desktop.getDesktop().open(selectedItem.getValue());
//                    } catch (IOException e) {
//                        showExceptionDialog(e);
//                    }
//                }
//            }
//        } else {
//            if (!selectedItem.isExpanded()) {
//                expandTreeView(selectedItem);
//            } else {
//                collapseTreeView(selectedItem);
//            }
//        }
    }


    @FXML
    private void copyFile() {
        if (selectedItem != null) {
//            copiedFile = selectedItem.getValue();
        }
    }

    @FXML
    private void cutFile() {
//        if (selectedItem != null) {
//            copiedFile = selectedItem.getValue();
//        }
//        isCutted = true;
//        selectedItem.getParent().getChildren().remove(selectedItem);
    }

    @FXML
    private void pasteFile() {

//        if (selectedItem != null && copiedFile != null) {
//            target = (selectedItem.getValue().isDirectory()) ? selectedItem.getValue() : selectedItem.getParent().getValue();
//            tempFile = copiedFile.getName();
//            try {
//                if (isCutted) {
//                    if (copiedFile.isDirectory()) {
//                        FileUtils.moveDirectoryToDirectory(copiedFile, target, false);
//                        FileUtils.deleteDirectory(copiedFile);
//                    } else {
//                        FileUtils.moveFileToDirectory(copiedFile, target, false);
//                        copiedFile.delete();
//                    }
//                    isCutted = false;
//                    copiedFile = null;
//                } else {
//                    if (new File("" + target + File.separator + copiedFile.getName()).exists()) {
//                        throw new IOException();
//                    }
//                    if (copiedFile.isDirectory()) {
//                        FileUtils.copyDirectoryToDirectory(copiedFile, target);
//                    } else {
//                        FileUtils.copyFileToDirectory(copiedFile, target);
//
//                    }
//                }
//                addNewItemAfterIO("" + target + File.separator + tempFile);
//            } catch (IOException e) {
//                showExceptionDialog(e);
//            }
//            target = null;
//        }
    }

    private void addNewItemAfterIO(String newName) {
        TreeItemWithLoading addItem = new TreeItemWithLoading(Paths.get(newName));
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
                TreeItemWithLoading addItem = new TreeItemWithLoading(Paths.get(newD));
                if (selectedItem.isLeaf()) selectedItem.setLeaf(false);
                if (selectedItem.isExpanded()) {
                    selectedItem.getChildren().add(addItem);
                }
            }
        }
    }

    private String createDirectory() {
        File file = new File(selectedItem.getValue().toString());
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
