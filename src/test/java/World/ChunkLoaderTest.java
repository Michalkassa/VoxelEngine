package World;

import Entity.Player;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkLoaderTest {

    private static final long TIMEOUT_MS = 10_000;

    private ChunkManager chunkManager;
    private Player player;
    private ChunkLoader loader;

    @BeforeEach
    void setUp() {
        chunkManager = new ChunkManager(42);
        player = new Player(new Vector3f(8, 100, 8), new Vector3f(0.75f, 1.7f, 0.75f));
    }

    @AfterEach
    void tearDown() {
        if (loader != null) loader.stopLoading();
    }

    private static void waitUntil(BooleanSupplier condition, String message) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) fail(message);
            Thread.sleep(10);
        }
    }

    private static Set<Vector3i> square(int centreX, int centreZ, int radius) {
        Set<Vector3i> positions = new HashSet<>();
        for (int x = centreX - radius; x <= centreX + radius; x++) {
            for (int z = centreZ - radius; z <= centreZ + radius; z++) {
                positions.add(new Vector3i(x, 0, z));
            }
        }
        return positions;
    }

    @Test
    void usesMoreThanOneWorkerWhenCoresAllow() {
        assertTrue(ChunkLoader.WORKER_COUNT >= 1);
        if (Runtime.getRuntime().availableProcessors() > 3) {
            assertTrue(ChunkLoader.WORKER_COUNT > 1);
        }
    }

    @Test
    void loadsEveryChunkWithinRenderDistance() throws InterruptedException {
        loader = new ChunkLoader(chunkManager, player, 4);
        loader.start();

        Set<Vector3i> expected = square(0, 0, 4);
        waitUntil(() -> chunkManager.getLoadedChunks().equals(expected), "chunks around the player did not all load");
    }

    @Test
    void followsThePlayerAndUnloadsChunksLeftBehind() throws InterruptedException {
        loader = new ChunkLoader(chunkManager, player, 2);
        loader.start();
        waitUntil(() -> chunkManager.getLoadedChunks().equals(square(0, 0, 2)), "first area did not load");

        player.setPosition(new Vector3f(10 * Chunk.CHUNK_SIZE + 8, 100, 8));

        Set<Vector3i> expected = square(10, 0, 2);
        waitUntil(() -> chunkManager.getLoadedChunks().equals(expected), "area around the new position did not load");
    }

    @Test
    void stopLoadingEndsTheLoaderThread() throws InterruptedException {
        loader = new ChunkLoader(chunkManager, player, 2);
        loader.start();

        loader.stopLoading();
        loader.join(TIMEOUT_MS);

        assertFalse(loader.isAlive());
    }

    @Test
    void concurrentLoadsOfTheSameChunkKeepOneChunk() throws Exception {
        int threads = 8;
        Vector3i position = new Vector3i(3, 0, -2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Chunk>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Chunk> load = () -> {
                    start.await();
                    chunkManager.loadChunkAsync(position);
                    return chunkManager.getChunk(position);
                };
                results.add(pool.submit(load));
            }
            start.countDown();

            Chunk first = results.get(0).get();
            for (Future<Chunk> result : results) {
                assertSame(first, result.get());
            }
            assertEquals(Set.of(position), chunkManager.getLoadedChunks());
        } finally {
            pool.shutdownNow();
        }
    }
}
