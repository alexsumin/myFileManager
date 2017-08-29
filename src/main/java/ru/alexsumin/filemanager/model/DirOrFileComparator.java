package ru.alexsumin.filemanager.model;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Comparator;


public class DirOrFileComparator implements Comparator<TreeItemWithLoading> {


    @Override
    public int compare(TreeItemWithLoading t1, TreeItemWithLoading t2) {

        boolean firstIsDir = Files.isDirectory(t1.getValue(), LinkOption.NOFOLLOW_LINKS);
        boolean secondIsDir = Files.isDirectory(t2.getValue(), LinkOption.NOFOLLOW_LINKS);

        if (firstIsDir && !secondIsDir) {
            return -1;
        }
        if (!firstIsDir && secondIsDir) {
            return 1;
        }

        return t1.getValue().compareTo(t2.getValue());
    }
}