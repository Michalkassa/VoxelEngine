package Storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCreator {

    private static final String SAVES_DIR = "saves";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveSaveFile(String worldName, Save save) throws IOException {

        Path worldPath = Paths.get(SAVES_DIR, worldName);
        if (!Files.exists(worldPath)) {
            Files.createDirectories(worldPath);
        }

        // Save to save.json
        Path filePath = Paths.get(SAVES_DIR, worldName, "save.json");

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(save, writer);
        }
    }

    public static boolean worldExists(String worldName) {
        Path worldPath = Paths.get(SAVES_DIR, worldName);
        return Files.exists(worldPath) && Files.isDirectory(worldPath);
    }

    public static void createWorldDirectory(String worldName)throws IOException{
        Path worldPath = Paths.get(SAVES_DIR, worldName);
        if (!Files.exists(worldPath)) {
            Files.createDirectories(worldPath);
        }
    }

}