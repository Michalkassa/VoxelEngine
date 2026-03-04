package Storage;

import Exceptions.NullSaveFileLoadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLoader {

    private static final String SAVES_DIR = "saves";
    private static final Gson gson = new GsonBuilder().create();

    public static Save LoadSave(String worldName) throws IOException, NullSaveFileLoadException {
        Path worldPath = Paths.get(SAVES_DIR, worldName);

        if (!Files.exists(worldPath)) {
            throw new IOException("World directory does not exist: " + worldName);
        }

        Path saveFilePath = Paths.get(SAVES_DIR, worldName, "save.json");

        if (!Files.exists(saveFilePath)) {
            throw new NullSaveFileLoadException("No save file found for world: " + worldName);
        }

        try (FileReader reader = new FileReader(saveFilePath.toFile())) {
            Save save = gson.fromJson(reader, Save.class);

            if (save == null) {
                throw new NullSaveFileLoadException("Save file is corrupted or empty");
            }

            return save;
        } catch (IOException e) {
            throw new IOException("Failed to read save file: " + e.getMessage());
        }
    }
}