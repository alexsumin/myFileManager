import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import ru.alexsumin.filemanager.tasks.FileCopyTask;
import ru.alexsumin.filemanager.tasks.FileDeleteTask;
import ru.alexsumin.filemanager.tasks.FileRenameTask;
import ru.alexsumin.filemanager.view.FileManagerController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FilesTest {
    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();
    private File folder1 = new File("forTest/folder1");
    private File folder2 = new File("forTest/folder2");
    private File folder3 = new File("forTest/folder3");
    private File folder4 = new File("forTest/folder1/folder4");
    private File folder5 = new File("forTest/folder2/folder5");
    private File file1 = new File("forTest/sampleFile1");
    private File file2 = new File("forTest/folder1/sampleFile2");
    private File file3 = new File("forTest/folder2/sampleFile3");
    private File file4 = new File("forTest/folder3/sampleFile4");
    private File file5 = new File("forTest/folder1/folder4/sampleFile5");

    @Before
    public void createTestFiles() throws IOException {

        ArrayList<File> directories = new ArrayList<>(Arrays.asList(
                folder1, folder2, folder3, folder4, folder5));

        ArrayList<File> files = new ArrayList<>(Arrays.asList(
                file1, file2, file3, file4, file5));

        directories.stream().forEach(file -> file.mkdirs());

        for (File f : files) {
            f.createNewFile();
        }

    }


    @Test
    public void renameTaskTest() throws InterruptedException {
        File renamedDir = new File("forTest/rename");
        File renamedFile = new File("forTest/folder2/newName");

        FileRenameTask renameTask1 = new FileRenameTask(Paths.get(folder3.getPath()), Paths.get(renamedDir.getPath()));
        FileRenameTask renameTask2 = new FileRenameTask(Paths.get(file3.getPath()), Paths.get(renamedFile.getPath()));

        FileManagerController.EXEC.submit(renameTask1);
        FileManagerController.EXEC.submit(renameTask2);

        Thread.sleep(1000);

        assertEquals(folder3.exists(), false);
        assertEquals(renamedDir.exists(), true);

        assertEquals(file3.exists(), false);
        assertEquals(renamedFile.exists(), true);

    }


    @Test
    public void deleteTaskTest() throws InterruptedException {

        assertEquals(folder1.exists(), true);
        assertEquals(file1.exists(), true);

        FileDeleteTask fileDeleteTask1 = new FileDeleteTask(folder1.toPath());
        FileDeleteTask fileDeleteTask2 = new FileDeleteTask(file1.toPath());

        FileManagerController.EXEC.submit(fileDeleteTask1);
        FileManagerController.EXEC.submit(fileDeleteTask2);

        Thread.sleep(1000);

        assertEquals(folder1.exists(), false);
        assertEquals(file1.exists(), false);


    }


    @Test
    public void fileCopyTask() throws InterruptedException {
        assertEquals(folder2.exists(), true);
        assertEquals(folder5.exists(), true);

        Path target1 = Paths.get(folder5.getPath() + folder2.getName());
        Path target2 = Paths.get(folder5.getPath() + file2.getName());


        FileCopyTask fileCopyTask1 = new FileCopyTask(folder2.toPath(), target1, false);
        FileCopyTask fileCopyTask2 = new FileCopyTask(file2.toPath(), target2, true);

        FileManagerController.EXEC.submit(fileCopyTask1);
        FileManagerController.EXEC.submit(fileCopyTask2);

        Thread.sleep(1000);
        assertEquals(target1.toFile().exists(), true);
        assertEquals(target2.toFile().exists(), true);
        assertEquals(file2.exists(), false);


    }

    @After
    public void deleteAllTestFiles() throws IOException {
        FileUtils.deleteDirectory(new File("forTest"));
    }


}
