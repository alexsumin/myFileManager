package ru.alexsumin.filemanager.util;

import javafx.concurrent.Task;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class FileRunTask extends Task<Void> {

    private Path forRun;

    public FileRunTask(Path forRun) {
        this.forRun = forRun;
    }

    @Override
    protected Void call() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {

            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + forRun.toAbsolutePath());
            } catch (IOException e) {
                throw e;
            }
        } else {
            if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(forRun.toFile());
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return null;
    }
}
