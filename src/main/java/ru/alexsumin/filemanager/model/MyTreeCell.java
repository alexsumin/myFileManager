package ru.alexsumin.filemanager.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.alexsumin.filemanager.view.MainWindowController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class MyTreeCell extends TreeCell<Path> {
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image picPc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);
    Image picLoading = new Image(getClass().getResourceAsStream("/images/loadingFile.png"), 30, 30, false, false);
    Image picImage = new Image(getClass().getResourceAsStream("/images/pictureFile.png"), 30, 30, false, false);


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
                            //this.setGraphic(null);
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
                        //setTooltipForFile(this);
                        this.setPrefHeight(40);
                        this.setText(getString());
                    }
                });

    }






    private void setImageForNode() {
        String pic;

        if (this.getTreeItem().equals(MainWindowController.root)) {
            this.setGraphic(new ImageView(picPc));
            return;
        }

        this.getTreeItem().setGraphic(new ImageView(picLoading));


        Task<ImageView> graphicTask = new Task<ImageView>() {

            @Override
            protected ImageView call() {


                ImageView imageView = new ImageView(picFile);
                Path path = getTreeItem().getValue();
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    if (getTreeItem().isExpanded()) {
                        imageView = new ImageView(folderOpened);
                    } else {
                        imageView = new ImageView(folder);
                    }
                } else {
                    String s = null;
                    try {
                        s = Files.probeContentType(path);
                        if (s.startsWith("image")) {
                            if (Files.size(path) < 5 * 1024 * 1024) {
                                Image image = new Image("file:" + path);

                                if (image.isError()) {
                                    imageView = new ImageView(picImage);
                                } else {
                                    imageView = new ImageView(image);
                                    imageView.setFitHeight(40);
                                    imageView.setFitWidth(40);
                                    imageView.setPreserveRatio(true);
                                }
                            } else {
                                imageView = new ImageView(picImage);
                            }
                        }
                    } catch (IOException e) {
                        return new ImageView(picFile);
                    }
                }
                return imageView;
            }


        };

        graphicTask.setOnSucceeded(event -> {
            ImageView imageView = graphicTask.getValue();
            setGraphic(imageView);
        });

        MainWindowController.EXEC.submit(graphicTask);
    }

    @Override
    public void startEdit() {

    }


//    private void setTooltipForFile(MyTreeCell cell) {
//        File file = getItem();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//
//        if (getItem().isDirectory()) {
//            cell.setTooltip(new Tooltip("Type: \t" + "folder" +
//                    "\nFiles: \t" + numberFiles(file) + "\n" + "Modified: \t" + sdf.format(file.lastModified())));
//        } else {
//            cell.setTooltip(new Tooltip("Type: \t" + "file" +
//                    "\n" + "Modified: \t" + sdf.format(file.lastModified())));
//        }
//    }

    private String numberFiles(File file) {
        try {
            return String.valueOf(file.listFiles().length);
        } catch (Exception e) {
            return "unavailable";
        }
    }

    private String getString() {
        return ((TreeItemWithLoading) getTreeItem()).toString();
    }

}
