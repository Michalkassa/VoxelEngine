package Storage;

import Exceptions.NullSaveFileLoadException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    @Test
    void storesWorldName() {
        assertEquals("MyWorld", new SaveManager("MyWorld").worldName);
    }

    @Test
    void hasNoSaveUntilOneIsLoaded() {
        assertNull(new SaveManager("MyWorld").getSave());
    }

    @Test
    void loadSaveStoresSave() {
        SaveManager manager = new SaveManager("MyWorld");
        Save save = new Save("1");

        manager.LoadSave(save);

        assertSame(save, manager.getSave());
    }

    @Test
    void loadSaveReplacesPreviousSave() {
        SaveManager manager = new SaveManager("MyWorld");
        Save second = new Save("2");

        manager.LoadSave(new Save("1"));
        manager.LoadSave(second);

        assertSame(second, manager.getSave());
    }

    @Test
    void nullSaveFileLoadExceptionKeepsMessage() {
        Exception error = new NullSaveFileLoadException("broken save");

        assertEquals("broken save", error.getMessage());
    }
}
