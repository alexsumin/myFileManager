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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

public class TreeItemWithLoading extends TreeItem<Path> {

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private boolean isLeaf = true;
    private boolean isFirstTimeLeaf = true;
    private byte newDirCount = 0;

    public TreeItemWithLoading(Path value) {
        super(value);

        expandedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasExpanded, Boolean isNowExpanded) -> {
            if (isNowExpanded) {
                loadChildrenLazily();
            } else {
                clearChildren();
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
        if (!isLeaf)
            this.loadingProperty().set(loading);
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            Path path = getValue();
            isLeaf = !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
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
                Path path = TreeItemWithLoading.this.getValue();

                if (path != null && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    try (DirectoryStream<Path> dirs = Files.newDirectoryStream(path)) {
                        for (Path dir : dirs) {
                            TreeItemWithLoading item = new TreeItemWithLoading(dir);
                            children.add(item);
                        }
                    } catch (IOException ex) {
                        //TODO: придумать что тут делать, видимо isProtected
                    }
                }
                FXCollections.sort(children, new DirectoryBeforeFileComparator());

                //Thread.sleep(2000);
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

    @Override
    public String toString() {
        if (getValue().getFileName() == null) {
            return getValue().toString();
        } else {
            return getValue().getFileName().toString();
        }
    }
}
