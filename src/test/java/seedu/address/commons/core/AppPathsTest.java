package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppPathsTest {
    @TempDir
    public Path testFolder;

    @Test
    public void resolveHome_jarLocation_usesJarDirectory() throws Exception {
        Path jarDirectory = Files.createDirectory(testFolder.resolve("jar-home"));
        Path jar = Files.createFile(jarDirectory.resolve("addressbook.jar"));
        Path otherWorkingDirectory = Files.createDirectory(testFolder.resolve("other-working-directory"));

        assertEquals(jarDirectory, AppPaths.resolveHome(jar, otherWorkingDirectory));
    }

    @Test
    public void resolveHome_classesDirectory_usesWorkingDirectory() throws Exception {
        Path classesDirectory = Files.createDirectory(testFolder.resolve("classes"));

        assertEquals(testFolder, AppPaths.resolveHome(classesDirectory, testFolder));
    }
}
