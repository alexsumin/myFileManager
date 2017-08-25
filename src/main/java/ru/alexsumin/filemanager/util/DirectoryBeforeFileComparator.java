package ru.alexsumin.filemanager.util;

import ru.alexsumin.filemanager.model.TreeItemWithLoading;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Comparator;

public class DirectoryBeforeFileComparator implements Comparator<TreeItemWithLoading> {


    @Override
    public int compare(TreeItemWithLoading t1, TreeItemWithLoading t2) {
        if (Files.isDirectory(t1.getValue(), LinkOption.NOFOLLOW_LINKS) &&
                !Files.isDirectory(t2.getValue(), LinkOption.NOFOLLOW_LINKS)) {

            return -1;
        }
        if (!Files.isDirectory(t1.getValue(), LinkOption.NOFOLLOW_LINKS) &&
                Files.isDirectory(t2.getValue(), LinkOption.NOFOLLOW_LINKS)) {

            return 1;
        }

        return t1.getValue().compareTo(t2.getValue());
    }
}