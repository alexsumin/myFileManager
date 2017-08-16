package ru.alexsumin.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    Image icoFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image dirFile = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image dirOpenFile = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image pc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);


    @FXML
    public void initialize() {
        treeView.setCellFactory(param -> this.createTreeCell());
        TreeItemWithLoading root = new TreeItemWithLoading(new File(System.getProperty("user.home")));
        treeView.setRoot(root);
        treeView.setEditable(true);


    }

    private TreeCell<File> createTreeCell() {


        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(20);
        //progressIndicator.setStyle(" -fx-progress-color: green;");


        TreeCell<File> cell = new TreeCell<>();
        cell.setPrefHeight(40);
        ChangeListener<Boolean> loadingChangeListener =
                (ObservableValue<? extends Boolean> obs, Boolean wasLoading, Boolean isNowLoading) -> {
                    if (isNowLoading) {
                        cell.setGraphic(progressIndicator);
                    } else {
                        //cell.setGraphic(null);
                        setImageForNode(cell);
                    }
                };

        cell.treeItemProperty().addListener(
                (obs, oldItem, newItem) -> {

                    if (oldItem != null) {
                        TreeItemWithLoading oldLazyTreeItem = (TreeItemWithLoading) oldItem;
                        oldLazyTreeItem.loadingProperty().removeListener(loadingChangeListener);
                    }

                    if (newItem != null) {
                        TreeItemWithLoading newLazyTreeItem = (TreeItemWithLoading) newItem;
                        newLazyTreeItem.loadingProperty().addListener(loadingChangeListener);

                        if (newLazyTreeItem.isLoading()) {

                            cell.setGraphic(progressIndicator);
                        } else {
                            cell.setGraphic(null);
                            setImageForNode(cell);
                        }
                    }
                });


        cell.itemProperty().addListener(
                (ObservableValue<? extends File> obs, File oldItem, File newItem) -> {
                    if (newItem == null) {
                        cell.setText(null);
                        cell.setGraphic(null);
                    } else {
                        cell.setText(newItem.getName());
                    }
                });

        return cell;


    }

    public void setImageForNode(TreeCell<File> t) {
        String pic = null;
        if (t.getTreeItem().getValue().isDirectory()) {

            if (t.getTreeItem().isExpanded()) t.setGraphic(new ImageView(dirOpenFile));
            else t.setGraphic(new ImageView(dirFile));
        } else {
            pic = t.getTreeItem().getValue().getAbsolutePath();
            Image image = new Image("file:" + pic);
            if (image.isError()) {
                t.setGraphic(new ImageView(icoFile));
            } else {
                ImageView imageView = new ImageView();
                imageView.setImage(image);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40); // <-- set size of image
                imageView.setPreserveRatio(true);
                t.setGraphic(imageView);

            }
        }
    }


//    private Node getImage(String itemPath) {
//        String parentPath = getItem().getPath().getParent().toAbsolutePath().toString();
//        Image image = new Image("file:" + parentPath + "/" + itemPath);
//        if (image.isError()) {
//            return null; // <-- if not image
//        }
//        ImageView imageView = new ImageView();
//        imageView.setImage(image);
//        imageView.setFitWidth(10); // <-- set size of image
//        imageView.setPreserveRatio(true);
//        return imageView;
//    }


    public static class TreeItemWithLoading extends TreeItem<File> {

        private final BooleanProperty loading = new SimpleBooleanProperty(false);

        private boolean isLeaf = true;
        //private boolean isFirstTimeChildren = true;
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


}
