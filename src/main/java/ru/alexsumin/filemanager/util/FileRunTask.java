package ru.alexsumin.filemanager.util;

import javafx.concurrent.Task;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by alex on 27.08.17.
 */
public class FileRunTask extends Task<Void> {

    Path forRun;

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
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(forRun.toFile());
                    Desktop.getDesktop().open(forRun.toFile());
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return null;
    }
}
