package ru.alexsumin.filemanager.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.alexsumin.filemanager.view.FileManagerController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class MyTreeCell extends TreeCell<Path> {
    private final ImageView FILE = new ImageView(new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false));
    private final ImageView FOLDER = new ImageView(new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false));
    private final ImageView FOLDER_OPEN = new ImageView(new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false));
    private final ImageView PC = new ImageView(new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false));
    private final ImageView IMAGE = new ImageView(new Image(getClass().getResourceAsStream("/images/pictureFile.png"), 30, 30, false, false));
    private final ImageView ERROR = new ImageView(new Image(getClass().getResourceAsStream("/images/lock.png"), 30, 30, false, false));


    public MyTreeCell() {

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(20);

        ChangeListener<Boolean> loadingChangeListener =
                (ObservableValue<? extends Boolean> obs, Boolean wasLoading, Boolean isNowLoading) -> {
                    if (isNowLoading) {
                        this.setGraphic(progressIndicator);
                    } else {
                        setImageForNode();
                    }
                };

        this.treeItemProperty().addListener(
                (obs, oldItem, newItem) -> {
                    if (oldItem != null) {
                        TreeItemWithLoading oldLazyTreeItem = (TreeItemWithLoading) oldItem;
                        oldLazyTreeItem.loadingProperty().removeListener(loadingChangeListener);
                    }
                    if (newItem != null) {
                        TreeItemWithLoading newLazyTreeItem = (TreeItemWithLoading) newItem;
                        newLazyTreeItem.loadingProperty().addListener(loadingChangeListener);
                        if (newLazyTreeItem.isLoading()) {
                            this.setGraphic(progressIndicator);
                        } else {
                            setImageForNode();
                        }
                    }
                });

        this.itemProperty().addListener(
                (ObservableValue<? extends Path> obs, Path oldItem, Path newItem) -> {
                    if (newItem == null) {
                        this.setText(null);
                        this.setGraphic(null);
                    } else {
                        this.setPrefHeight(40);
                        this.setText(getString());
                    }
                });
    }


    private void setImageForNode() {
        if (this.getTreeItem().equals(FileManagerController.root)) {
            this.setGraphic(PC);
            return;
        }
        try {
            Path path = getTreeItem().getValue();
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                if (getTreeItem().isExpanded()) {
                    this.setGraphic(FOLDER_OPEN);
                } else {
                    this.setGraphic(FOLDER);
                }
            } else {
                String pic = Files.probeContentType(path);
                if (pic != null && pic.startsWith("image")) {
                    if (Files.size(path) < 5 * 1024 * 1024) {
                        Image image = new Image("file:" + path);
                        if (image.isError()) {
                            this.setGraphic(IMAGE);
                        } else {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(40);
                            imageView.setFitWidth(40);
                            imageView.setPreserveRatio(true);
                            this.setGraphic(imageView);
                        }
                    } else {
                        this.setGraphic(IMAGE);
                    }
                } else {
                    this.setGraphic(FILE);
                }

            }
        } catch (IOException e) {
            this.setGraphic(ERROR);
        }
    }

    @Override
    public void startEdit() {

    }


    private String getString() {
        return ((TreeItemWithLoading) getTreeItem()).toString();
    }

}
