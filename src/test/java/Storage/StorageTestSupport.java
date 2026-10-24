package Storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

final class StorageTestSupport {

    private StorageTestSupport() {
    }

    static String uniqueWorldName() {
        return "junit-" + UUID.randomUUID();
    }

    static Path worldPath(String worldName) {
        return Paths.get("saves", worldName);
    }

    static void deleteWorld(String worldName) {
        Path root = worldPath(worldName);
        if (!Files.exists(root)) return;
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
