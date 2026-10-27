package World;

import Entity.Player;
import org.joml.Vector3i;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// Decides which chunks to load around the player, nearest first, and generates them on a pool of worker threads
public class ChunkLoader extends Thread {

    // Leave a core for the render thread and one for this thread
    static final int WORKER_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    // Enough queued work to keep every worker busy, but few enough that a moving player's new nearest chunks
    // don't wait behind a long backlog of chunks that were nearest a moment ago
    private static final int MAX_IN_FLIGHT = WORKER_COUNT * 4;
    // A scan stops after this many submissions and starts again from the player's current position
    private static final int SUBMISSIONS_PER_SCAN = MAX_IN_FLIGHT * 4;

    private static final int IDLE_DELAY = 100;

    private final ChunkManager chunkManager;
    private final Player player;
    private volatile int renderDistance;

    private final ExecutorService workers;
    private final Semaphore inFlightPermits = new Semaphore(MAX_IN_FLIGHT);
    private final Set<Vector3i> inFlight = ConcurrentHashMap.newKeySet();

    private volatile boolean running;

    public ChunkLoader(ChunkManager chunkManager, Player player, int renderDistance) {
        super("ChunkLoader");
        this.chunkManager = chunkManager;
        this.player = player;
        this.renderDistance = renderDistance;
        this.running = true;
        this.setDaemon(true);

        AtomicInteger workerNumber = new AtomicInteger();
        this.workers = Executors.newFixedThreadPool(WORKER_COUNT, task -> {
            Thread worker = new Thread(task, "ChunkWorker-" + workerNumber.incrementAndGet());
            worker.setDaemon(true);
            return worker;
        });
    }

    @Override
    public void run() {
        while (running) {
            try {
                Vector3i playerChunkPos = getPlayerChunkPosition();
                unloadFarChunks(playerChunkPos);
                int submitted = submitNearestChunks(playerChunkPos);

                if (submitted == 0) {
                    Thread.sleep(IDLE_DELAY);
                }
            } catch (InterruptedException e) {
                System.out.println("ChunkLoader interrupted");
                break;
            } catch (Exception e) {
                System.err.println("ChunkLoader error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Vector3i getPlayerChunkPosition() {
        return new Vector3i(
                Math.floorDiv((int) player.getPosition().x, Chunk.CHUNK_SIZE),
                0,
                Math.floorDiv((int) player.getPosition().z, Chunk.CHUNK_SIZE)
        );
    }

    // Walks outwards ring by ring and hands missing chunks to the workers, blocking while they are all busy
    private int submitNearestChunks(Vector3i playerChunkPos) throws InterruptedException {
        int submitted = 0;
        int distance = renderDistance;

        for (int radius = 0; radius <= distance; radius++) {
            for (int x = playerChunkPos.x - radius; x <= playerChunkPos.x + radius; x++) {
                for (int z = playerChunkPos.z - radius; z <= playerChunkPos.z + radius; z++) {
                    if (Math.abs(x - playerChunkPos.x) != radius && Math.abs(z - playerChunkPos.z) != radius) continue;

                    Vector3i chunkPos = new Vector3i(x, 0, z);
                    if (chunkManager.isChunkLoaded(chunkPos) || !inFlight.add(chunkPos)) continue;

                    inFlightPermits.acquire();
                    submit(chunkPos);
                    if (++submitted >= SUBMISSIONS_PER_SCAN) return submitted;
                }
            }
        }
        return submitted;
    }

    private void submit(Vector3i chunkPos) {
        workers.execute(() -> {
            try {
                chunkManager.loadChunkAsync(chunkPos);
            } catch (Exception e) {
                System.err.println("Failed to load chunk " + chunkPos + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                inFlight.remove(chunkPos);
                inFlightPermits.release();
            }
        });
    }

    private void unloadFarChunks(Vector3i playerChunkPos) {
        int distance = renderDistance;
        for (Vector3i loadedChunk : chunkManager.getLoadedChunks()) {
            int deltaX = Math.abs(loadedChunk.x - playerChunkPos.x);
            int deltaZ = Math.abs(loadedChunk.z - playerChunkPos.z);

            if (deltaX > distance || deltaZ > distance) {
                chunkManager.unloadChunk(loadedChunk);
            }
        }
    }

    public void stopLoading() {
        running = false;
        this.interrupt();
        workers.shutdownNow();
    }

    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
    }
}
