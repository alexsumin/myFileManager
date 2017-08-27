package ru.alexsumin.filemanager.util;


import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class FileDeleteTask extends Task<Void> {

    Path forDelete;

    public FileDeleteTask(Path forDelete) {
        this.forDelete = forDelete;
    }

    @Override
    protected Void call() throws Exception {
        try {
            if (Files.isDirectory(forDelete, LinkOption.NOFOLLOW_LINKS)) {
                FileUtils.deleteDirectory(forDelete.toFile());

            } else {
                FileUtils.forceDelete(forDelete.toFile());
            }
        } catch (Exception e) {
            throw e;
        }

        return null;
    }
}
