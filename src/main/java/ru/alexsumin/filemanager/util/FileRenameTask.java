package ru.alexsumin.filemanager.util;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileRenameTask extends Task<Void> {

    Path source;
    Path target;

    public FileRenameTask(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    protected Void call() throws Exception {
        try {
            Files.move(source,
                    target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw e;
        }
        return null;
    }
}
