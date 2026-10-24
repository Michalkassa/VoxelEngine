package Storage;

import Exceptions.NullSaveFileLoadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileLoaderTest {

    private String worldName;

    @BeforeEach
    void setUp() {
        worldName = StorageTestSupport.uniqueWorldName();
    }

    @AfterEach
    void tearDown() {
        StorageTestSupport.deleteWorld(worldName);
    }

    private static String seedOf(Save save) throws Exception {
        Field field = Save.class.getDeclaredField("seed");
        field.setAccessible(true);
        return (String) field.get(save);
    }

    @Test
    void missingWorldThrowsIOException() {
        IOException error = assertThrows(IOException.class, () -> FileLoader.LoadSave(worldName));

        assertTrue(error.getMessage().contains(worldName));
    }

    @Test
    void worldWithoutSaveFileThrowsNullSaveFileLoadException() throws Exception {
        FileCreator.createWorldDirectory(worldName);

        assertThrows(NullSaveFileLoadException.class, () -> FileLoader.LoadSave(worldName));
    }

    @Test
    void emptySaveFileThrowsNullSaveFileLoadException() throws Exception {
        FileCreator.createWorldDirectory(worldName);
        Files.writeString(StorageTestSupport.worldPath(worldName).resolve("save.json"), "");

        assertThrows(NullSaveFileLoadException.class, () -> FileLoader.LoadSave(worldName));
    }

    @Test
    void loadsSaveWrittenByFileCreator() throws Exception {
        FileCreator.saveSaveFile(worldName, new Save("424242"));

        Save loaded = FileLoader.LoadSave(worldName);

        assertNotNull(loaded);
        assertEquals("424242", seedOf(loaded));
    }

    @Test
    void loadsHandWrittenSaveFile() throws Exception {
        FileCreator.createWorldDirectory(worldName);
        Files.writeString(StorageTestSupport.worldPath(worldName).resolve("save.json"), "{\"seed\":\"777\"}");

        assertEquals("777", seedOf(FileLoader.LoadSave(worldName)));
    }
}
