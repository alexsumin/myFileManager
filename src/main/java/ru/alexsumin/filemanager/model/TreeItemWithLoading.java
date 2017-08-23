package ru.alexsumin.filemanager.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import ru.alexsumin.filemanager.util.DirectoryBeforeFileComparator;
import ru.alexsumin.filemanager.view.MainWindowController;

import java.io.File;
import java.util.List;

public class TreeItemWithLoading extends TreeItem<File> {

    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    private boolean isLeaf = true;
    private boolean isFirstTimeLeaf = true;
    private byte newDirCount = 0;


    public TreeItemWithLoading(File value) {
        super(value);


        expandedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasExpanded, Boolean isNowExpanded) -> {
            if (!this.equals(MainWindowController.root)) {
                if (isNowExpanded) {
                    loadChildrenLazily();
                } else {
                    clearChildren();
                }
            }
        });


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

                Thread.sleep(2000);
                return children;
            }
        };


        loadTask.setOnSucceeded(event -> {
            List<TreeItemWithLoading> children = loadTask.getValue();
            getChildren().setAll(children);
            setLoading(false);

        });

        MainWindowController.EXEC.submit(loadTask);
    }

    private void clearChildren() {
        getChildren().clear();
    }


}
