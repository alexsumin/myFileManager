package ru.alexsumin.filemanager.model;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ru.alexsumin.filemanager.view.MainWindowController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

public class MyTreeCell extends TreeCell<File> {
    Image picFile = new Image(getClass().getResourceAsStream("/images/file.png"), 30, 30, false, false);
    Image folder = new Image(getClass().getResourceAsStream("/images/folder.png"), 30, 30, false, false);
    Image folderOpened = new Image(getClass().getResourceAsStream("/images/openedfolder.png"), 30, 30, false, false);
    Image pc = new Image(getClass().getResourceAsStream("/images/pc.png"), 30, 30, false, false);
    private TextField textField;
    private String editingPath;
    private StringProperty messageProp;
    private ContextMenu dirMenu = new ContextMenu();
    private ContextMenu fileMenu = new ContextMenu();


    public MyTreeCell() {


        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(20);

//        EventDispatcher original = this.getEventDispatcher();
//        this.setEventDispatcher(new CellEventDispatcher(original));


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
                        MainWindowController.TreeItemWithLoading oldLazyTreeItem = (MainWindowController.TreeItemWithLoading) oldItem;
                        oldLazyTreeItem.loadingProperty().removeListener(loadingChangeListener);
                    }

                    if (newItem != null) {
                        MainWindowController.TreeItemWithLoading newLazyTreeItem = (MainWindowController.TreeItemWithLoading) newItem;
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
                        this.setText(newItem.getName());
                    }
                });

        this.addEventFilter(MouseEvent.MOUSE_PRESSED, t -> {
            if (t.getButton() == MouseButton.PRIMARY && t.getClickCount() == 2) {
                if (!getItem().isDirectory()) {
                    openFile(getItem());
                }
            }
        });

    }


    private void openFile(File file) {
        if (!file.isDirectory()) {


            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
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


    //@Override
    public void cancelEdit() {
        super.cancelEdit();

        //setText((String) getItem().getCanonicalPath());
        setText((String) getItem().getName());
//        } catch (IOException e) {
//            e.printStackTrace();
        this.setGraphic(null);
        setImageForNode(this);
    }

    @Override
    public void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);

        if (empty) {
            setText(null);
            //setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                //setText(getString());
                //TODO: влияет на отображение файла после переименования
                setText(getItem().getName());
                //setGraphic(getTreeItem().getGraphic());
                //setImageForNode(this);
            }
        }
    }

    private void createTextField() {
        //textField = new TextField(getString());
        textField = new TextField(getItem().getName());

        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    System.out.println(getItem().getParent());
                    commitEdit(getItem().getParent() + File.separator + textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
    }


    public void commitEdit(String newName) {
        // rename the file or directory
        if (editingPath != null) {
            try {
                System.out.println(editingPath);
                System.out.println(getItem().getPath());
                Files.move(Paths.get(editingPath), Paths.get(newName), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                cancelEdit();
                //TODO: вызвать окно с ошибкой
                System.out.println("что-то пошло не так");
            }
        }
        setImageForNode(this);
        this.setText(getItem().getPath());

        super.commitEdit(getItem());
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        if (getItem() == null) {
            editingPath = null;
        } else {
            editingPath = (getItem().getParent() + File.separator + textField.getText());
            System.out.println(editingPath);
        }
    }


    private void setTooltipForFile(MyTreeCell cell) {
        File file = getItem();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy HH:mm");
        String type = (getItem().isDirectory()) ? "folder" : "file";

        this.setTooltip(new Tooltip("File: \t\t" + file.getName() +
                "\nType: \t" + type +
                "\nSize: \t" + fileSize(file) + "\n" + "Modified: \t" + sdf.format(file.lastModified())));
    }


    private String fileSize(File file) {
        long length = 0;
        if (!file.isDirectory()) {
            length = file.length();
        } else {
            try {
                length = Files.walk(file.toPath()).mapToLong(p -> p.toFile().length()).sum();
            } catch (Exception e) {
                return "unavailable";
            }
        }
        if (length > 1024 * 1024) {
            return length / 1024 / 1024 + " Mb";
        } else if (length > 1024) {
            return length / 1024 + " Kb";
        } else {
            return length + " b";
        }


    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }


}
