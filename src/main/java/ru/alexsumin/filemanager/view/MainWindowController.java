package ru.alexsumin.filemanager.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.alexsumin.filemanager.model.MyTreeCell;
import ru.alexsumin.filemanager.util.DirectoryBeforeFileComparator;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindowController {

    private static final ExecutorService EXEC = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    @FXML
    TreeView<File> treeView = new TreeView<>();
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image pc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);
    private File currentFile;
    private File copiedFile;
    private TreeItem selectedCell;

    @FXML
    private void initialize() {
        //treeView.setCellFactory(param -> this.createTreeCell());
        treeView.setCellFactory(param -> new MyTreeCell());
        TreeItemWithLoading root = new TreeItemWithLoading(new File(System.getProperty("user.home")));
        treeView.setRoot(root);
        treeView.setEditable(false);
        EventDispatcher treeOriginal = treeView.getEventDispatcher();
        treeView.setEventDispatcher(new CellEventDispatcher(treeOriginal));

        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    selectedCell = newValue;
                });


    }



    public static class TreeItemWithLoading extends TreeItem<File> {

        private final BooleanProperty loading = new SimpleBooleanProperty(false);

        private boolean isLeaf = true;
        private boolean isFirstTimeLeaf = true;

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

                    Thread.sleep(2000);
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
