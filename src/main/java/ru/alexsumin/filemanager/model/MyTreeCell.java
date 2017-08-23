package ru.alexsumin.filemanager.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.alexsumin.filemanager.view.MainWindowController;

import java.io.File;
import java.text.SimpleDateFormat;

public class MyTreeCell extends TreeCell<File> {
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image pc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);

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
                            this.setGraphic(null);
                            setImageForNode();
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
                            this.setText(getItemName(this));
                        }
                    }
                });

    }


    private String getItemName(MyTreeCell cell) {
        String name;

        for (TreeItemWithLoading item : MainWindowController.systemDirectories) {
            if (cell.getTreeItem().equals(item)) {

                return item.getValue().getPath();
            }
        }
        name = cell.getItem().getName();
        return name;
    }

    private void setImageForNode() {
        String pic;
        if (this.getTreeItem().equals(MainWindowController.root)) {
            this.setGraphic(new ImageView(pc));
            return;
        } else if (this.getTreeItem().getValue().isDirectory()) {
            if (this.getTreeItem().isExpanded()) this.setGraphic(new ImageView(folderOpened));
            else this.setGraphic(new ImageView(folder));
        } else {

            pic = this.getTreeItem().getValue().getAbsolutePath();

            Image image = new Image("file:" + pic);
            if (image.isError()) {
                this.setGraphic(new ImageView(picFile));
            } else {
                ImageView imageView = new ImageView();
                imageView.setImage(image);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                this.setGraphic(imageView);
            }
        }
    }

    @Override
    public void startEdit() {

    }


    private void setTooltipForFile(MyTreeCell cell) {
        File file = getItem();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (getItem().isDirectory()) {
            cell.setTooltip(new Tooltip("Type: \t" + "folder" +
                    "\nFiles: \t" + numberFiles(file) + "\n" + "Modified: \t" + sdf.format(file.lastModified())));
        } else {
            cell.setTooltip(new Tooltip("Type: \t" + "file" +
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
