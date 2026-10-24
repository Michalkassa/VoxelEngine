package Storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileCreatorTest {

    private String worldName;

    @BeforeEach
    void setUp() {
        worldName = StorageTestSupport.uniqueWorldName();
    }

    @AfterEach
    void tearDown() {
        StorageTestSupport.deleteWorld(worldName);
    }

    @Test
    void worldDoesNotExistBeforeCreation() {
        assertFalse(FileCreator.worldExists(worldName));
    }

    @Test
    void createWorldDirectoryCreatesFolder() throws Exception {
        FileCreator.createWorldDirectory(worldName);

        assertTrue(Files.isDirectory(StorageTestSupport.worldPath(worldName)));
        assertTrue(FileCreator.worldExists(worldName));
    }

    @Test
    void createWorldDirectoryIsIdempotent() throws Exception {
        FileCreator.createWorldDirectory(worldName);

        assertDoesNotThrow(() -> FileCreator.createWorldDirectory(worldName));
        assertTrue(FileCreator.worldExists(worldName));
    }

    @Test
    void worldExistsIsFalseForPlainFile() throws Exception {
        Files.createDirectories(StorageTestSupport.worldPath(worldName).getParent());
        Files.createFile(StorageTestSupport.worldPath(worldName));

        assertFalse(FileCreator.worldExists(worldName));
    }

    @Test
    void saveSaveFileCreatesWorldFolderAndJson() throws Exception {
        FileCreator.saveSaveFile(worldName, new Save("12345"));

        Path saveFile = StorageTestSupport.worldPath(worldName).resolve("save.json");
        assertTrue(Files.isRegularFile(saveFile));
        assertTrue(FileCreator.worldExists(worldName));
    }

    @Test
    void saveSaveFileWritesSeedAsJson() throws Exception {
        FileCreator.saveSaveFile(worldName, new Save("98765"));

        String json = Files.readString(StorageTestSupport.worldPath(worldName).resolve("save.json"));
        assertTrue(json.contains("\"seed\": \"98765\""), json);
    }

    @Test
    void saveSaveFileOverwritesPreviousSave() throws Exception {
        FileCreator.saveSaveFile(worldName, new Save("1"));
        FileCreator.saveSaveFile(worldName, new Save("2"));

        String json = Files.readString(StorageTestSupport.worldPath(worldName).resolve("save.json"));
        assertTrue(json.contains("\"2\""), json);
        assertFalse(json.contains("\"1\""), json);
    }
}
