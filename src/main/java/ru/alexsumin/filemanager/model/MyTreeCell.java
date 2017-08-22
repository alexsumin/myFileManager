package ru.alexsumin.filemanager.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;

public class MyTreeCell extends TreeCell<File> {
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);

    public MyTreeCell() {

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(20);

        ChangeListener<Boolean> loadingChangeListener =
                (ObservableValue<? extends Boolean> obs, Boolean wasLoading, Boolean isNowLoading) -> {
                    if (isNowLoading) {
                        this.setGraphic(progressIndicator);
                    } else {
                        setImageForNode(this);
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
                            this.setGraphic(null);
                            setImageForNode(this);
                        }
                    }
                });

        this.itemProperty().addListener(
                (ObservableValue<? extends File> obs, File oldItem, File newItem) -> {
                    if (newItem == null) {
                        this.setText(null);
                        this.setGraphic(null);
                    } else {
                        setTooltipForFile(this);
                        this.setPrefHeight(40);
                        //TODO: root folder returns empty name?
                        if (newItem.getAbsolutePath().equals("/")) {
                            this.setText("/");
                        } else {
                            this.setText(newItem.getName());
                        }
                    }
                });

    }


    private void setImageForNode(TreeCell<File> t) {
        String pic = null;
        if (t.getTreeItem().getValue().isDirectory()) {

            if (t.getTreeItem().isExpanded()) t.setGraphic(new ImageView(folderOpened));
            else t.setGraphic(new ImageView(folder));
        } else {
            pic = t.getTreeItem().getValue().getAbsolutePath();
            Image image = new Image("file:" + pic);
            if (image.isError()) {
                t.setGraphic(new ImageView(picFile));
            } else {
                ImageView imageView = new ImageView();
                imageView.setImage(image);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                t.setGraphic(imageView);
            }
        }
    }

    @Override
    public void startEdit() {

    }


    private void setTooltipForFile(MyTreeCell cell) {
        File file = getItem();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String typeDir = "folder";
        String typeFile = "file";

        if (getItem().isDirectory()) {
            cell.setTooltip(new Tooltip("Type: \t" + typeDir +
                    "\nFiles: \t" + numberFiles(file) + "\n" + "Modified: \t" + sdf.format(file.lastModified())));
        } else {
            cell.setTooltip(new Tooltip("Type: \t" + typeFile +
                    "\n" + "Modified: \t" + sdf.format(file.lastModified())));
        }

    }

    private String numberFiles(File file) {
        try {
            return String.valueOf(file.listFiles().length);
        } catch (Exception e) {
            return "unavailable";
        }
    }

}
