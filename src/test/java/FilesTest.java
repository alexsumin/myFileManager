import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FilesTest {

    @Test
    public void getFileTypeTest() throws IOException {

        String s1 = "/home/alex/Pictures/Wallpapers/MiNotebookWall.jpg";
        Path pathPic = Paths.get(s1);
        String s2 = Files.probeContentType(pathPic);
        assertNotNull(Files.probeContentType(pathPic));
        System.out.println("Content type of file \"" + pathPic + "\" is " + s2);

        assertEquals(s2.startsWith("image"), true);


    }

    @Test
    public void forRenameTest() {
        String s1 = "/home/alex/Pictures/Wallpapers/MiNotebookWall.jpg";
        Path pathPic = Paths.get(s1);


        System.out.println(pathPic.toAbsolutePath());
        Path parent = pathPic.getParent();
        assertEquals("/home/alex/Pictures/Wallpapers", parent.toString());
    }
}
