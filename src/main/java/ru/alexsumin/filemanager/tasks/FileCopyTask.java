package ru.alexsumin.filemanager.tasks;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;


public class FileCopyTask extends Task<Void> {
    private Path source;
    private Path target;
    private boolean forDelete;

    public FileCopyTask(Path source, Path target, Boolean forDelete) {
        this.source = source;
        this.target = target;
        this.forDelete = forDelete;
    }

    @Override
    protected Void call() throws Exception {
        try {
            if (forDelete) {
                if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                    FileUtils.copyDirectoryToDirectory(source.toFile(), target.toFile());
                    FileUtils.deleteDirectory(source.toFile());
                } else {
                    FileUtils.copyFileToDirectory(source.toFile(), target.toFile(), false);
                    Files.delete(source);
                }
            } else {
                if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                    FileUtils.copyDirectoryToDirectory(source.toFile(), target.toFile());
                } else {
                    FileUtils.copyFileToDirectory(source.toFile(), target.toFile());
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
}
