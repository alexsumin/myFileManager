package ru.alexsumin.filemanager.view;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import ru.alexsumin.filemanager.model.MyTreeCell;
import ru.alexsumin.filemanager.util.DirectoryBeforeFileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainWindowController {

    private static final ExecutorService EXEC = Executors.newCachedThreadPool((Runnable r) -> {
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
    private void deleteFile() {
        if (selectedItem != null) {
            try {
                FileUtils.deleteDirectory(selectedItem.getValue());
            } catch (Exception e) {
                //TODO: окошко с ошибкой
                e.printStackTrace();
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
                        throw new IOException("File already exists");
                    }
                    if (copiedFile.isDirectory()) {
                        FileUtils.copyDirectoryToDirectory(copiedFile, target);
                    } else {
                        FileUtils.copyFileToDirectory(copiedFile, target);

                    }
                }
                addNewItemAfterIO();
            } catch (Exception e) {
                e.printStackTrace();
                //TODO: окно с ошибкой
            }
            target = null;
        }
    }

    private void addNewItemAfterIO() {
        TreeItemWithLoading addItem = new TreeItemWithLoading(new File("" + target + File.separator + tempFile));
        if (selectedItem.isLeaf) selectedItem.setLeaf(false);
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
                if (selectedItem.isLeaf) selectedItem.setLeaf(false);
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
                //TODO: окно с ошибкой
                e.printStackTrace();
            }
        }
        return newDir;
    }


    public static class TreeItemWithLoading extends TreeItem<File> {

        private final BooleanProperty loading = new SimpleBooleanProperty(false);

        private boolean isLeaf = true;
        private boolean isFirstTimeLeaf = true;
        private int newDirCount = 0;


        public TreeItemWithLoading(File value) {
            super(value);


            expandedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasExpanded, Boolean isNowExpanded) -> {
                if (isNowExpanded) {
                    loadChildrenLazily();
                } else {
                    clearChildren();
                }
            });


        }

        public void setFirstTimeLeaf(boolean firstTimeLeaf) {
            isFirstTimeLeaf = firstTimeLeaf;
        }

        public int getNewDirCount() {
            return ++this.newDirCount;
        }

        public final BooleanProperty loadingProperty() {
            return this.loading;
        }

        public final boolean isLoading() {
            return this.loadingProperty().get();
        }

        public final void setLoading(final boolean loading) {
            this.loadingProperty().set(loading);
        }

        @Override
        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                File f = (File) getValue();
                isLeaf = f.isFile();
            }

            return isLeaf;
        }

        public void setLeaf(boolean leaf) {
            isLeaf = leaf;
        }

        private void loadChildrenLazily() {
            setLoading(true);
            Task<List<TreeItemWithLoading>> loadTask = new Task<List<TreeItemWithLoading>>() {

                @Override
                protected List<TreeItemWithLoading> call() throws Exception {

                    ObservableList<TreeItemWithLoading> children = FXCollections.observableArrayList();
                    File f = TreeItemWithLoading.this.getValue();

                    if (f != null && f.isDirectory()) {
                        File[] files = f.listFiles();
                        if (files != null) {
                            for (File childFile : files) {
                                TreeItemWithLoading t = new TreeItemWithLoading(childFile);
                                children.add(t);
                            }
                        }
                    }
                    FXCollections.sort(children, new DirectoryBeforeFileComparator());

                    //Thread.sleep(2000);
                    return children;
                }
            };


            loadTask.setOnSucceeded(event -> {
                List<TreeItemWithLoading> children = loadTask.getValue();
                isLeaf = children.size() == 0;
                getChildren().setAll(children);
                setLoading(false);

            });

            EXEC.submit(loadTask);
        }

        private void clearChildren() {
            getChildren().clear();
        }


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
