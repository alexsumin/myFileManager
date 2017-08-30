package ru.alexsumin.filemanager.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class MyTreeCell extends TreeCell<Path> {

    private static final Image FILE = new Image(MyTreeCell.class.getResourceAsStream("/images/file.png"), 30, 30, false, false);
    private static final Image FOLDER = new Image(MyTreeCell.class.getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    private static final Image FOLDER_OPEN = new Image(MyTreeCell.class.getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    private static final Image IMAGE = new Image(MyTreeCell.class.getResourceAsStream("/images/pictureFile.png"), 30, 30, false, false);
    private static final Image ERROR = new Image(MyTreeCell.class.getResourceAsStream("/images/lock.png"), 30, 30, false, false);


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
        try {
            Path path = getTreeItem().getValue();
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                if (getTreeItem().isExpanded()) {
                    this.setGraphic(new ImageView(FOLDER_OPEN));
                } else {
                    this.setGraphic(new ImageView(FOLDER));
                }
            } else {
                String pic = Files.probeContentType(path);
                if (pic != null && pic.startsWith("image")) {
                    if (Files.size(path) < 5 * 1024 * 1024) {
                        Image image = new Image("file:" + path);
                        if (image.isError()) {
                            this.setGraphic(new ImageView(IMAGE));
                        } else {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(40);
                            imageView.setFitWidth(40);
                            imageView.setPreserveRatio(true);
                            this.setGraphic(imageView);
                        }
                    } else {
                        this.setGraphic(new ImageView(IMAGE));
                    }
                } else {
                    this.setGraphic(new ImageView(FILE));
                }

            }
        } catch (IOException e) {
            this.setGraphic(new ImageView(ERROR));
        }
    }

    @Override
    public void startEdit() {

    }


    private String getString() {
        return ((TreeItemWithLoading) getTreeItem()).toString();
    }

}
