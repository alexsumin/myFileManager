package ru.alexsumin.filemanager.util;

import ru.alexsumin.filemanager.view.MainWindowController;

import java.util.Comparator;

public class DirectoryBeforeFileComparator implements Comparator<ru.alexsumin.filemanager.view.MainWindowController.TreeItemWithLoading> {


    @Override
    public int compare(MainWindowController.TreeItemWithLoading t1, MainWindowController.TreeItemWithLoading t2) {
        if (t1.getValue().isDirectory() && !t2.getValue().isDirectory()) {
            // directory before non-directory.
            return -1;
        }
        if (!t1.getValue().isDirectory() && t2.getValue().isDirectory()) {
            // non-directory after directory
            return 1;
        }
        // compare two pathnames lexicographically
        return t1.getValue().compareTo(t2.getValue());
    }
}