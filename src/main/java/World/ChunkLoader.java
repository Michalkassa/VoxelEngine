package World;

import Entity.Player;
import org.joml.Vector3i;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkLoader extends Thread {

    private ChunkManager chunkManager;
    private Player player;
    private int renderDistance;

    private Queue<Vector3i> chunksToLoad;
    private Queue<Vector3i> chunksToUnload;

    private final Lock chunksToLoadLock = new ReentrantLock();

    private final int LOADING_DELAY = 100;

    private volatile boolean running;

    public ChunkLoader(ChunkManager chunkManager, Player player, int renderDistance) {
        this.chunkManager = chunkManager;
        this.player = player;
        this.renderDistance = renderDistance;
        this.chunksToLoad = new ConcurrentLinkedQueue<>();
        this.chunksToUnload = new ConcurrentLinkedQueue<>();
        this.running = true;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Vector3i playerChunkPos = getPlayerChunkPosition();
                updateChunkQueues(playerChunkPos);
                processChunkLoading();
                processChunkUnloading();

                if (chunksToLoad.isEmpty()) {
                    Thread.sleep(LOADING_DELAY);
                }else{
                    Thread.sleep(LOADING_DELAY/2);
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

    public void notifyLoadChunks() {
        synchronized (chunksToLoadLock) {
            chunksToLoadLock.notify();
        }
    }

    private Vector3i getPlayerChunkPosition() {
        return new Vector3i(
                Math.floorDiv((int) player.getPosition().x, Chunk.CHUNK_SIZE),
                0,
                Math.floorDiv((int) player.getPosition().z, Chunk.CHUNK_SIZE)
        );
    }

    private void updateChunkQueues(Vector3i playerChunkPos) {
        chunksToLoad.clear();
        chunksToUnload.clear();

        for (int radius = 0; radius <= renderDistance; radius++) {
            for (int x = playerChunkPos.x - radius; x <= playerChunkPos.x + radius; x++) {
                for (int z = playerChunkPos.z - radius; z <= playerChunkPos.z + radius; z++) {
                    if (Math.abs(x - playerChunkPos.x) == radius || Math.abs(z - playerChunkPos.z) == radius) {
                        Vector3i chunkPos = new Vector3i(x, 0, z);

                        if (!chunkManager.isChunkLoaded(chunkPos)) {
                            chunksToLoad.add(chunkPos);
                        }
                    }
                }
            }
        }

        for (Vector3i loadedChunk : chunkManager.getLoadedChunks()) {
            int deltaX = Math.abs(loadedChunk.x - playerChunkPos.x);
            int deltaZ = Math.abs(loadedChunk.z - playerChunkPos.z);

            if (deltaX > renderDistance || deltaZ > renderDistance) {
                chunksToUnload.add(loadedChunk);
            }
        }
    }

    private void processChunkLoading() {
        int chunksLoadedThisFrame = 0;
        int maxChunksPerFrame = 2;

        while (!chunksToLoad.isEmpty() && chunksLoadedThisFrame < maxChunksPerFrame) {
            Vector3i chunkPos = chunksToLoad.poll();
            if (chunkPos != null) {
                chunkManager.loadChunkAsync(chunkPos);  // Changed method
                chunksLoadedThisFrame++;
            }
        }
    }

    private void processChunkUnloading() {
        int chunksUnloadedThisFrame = 0;
        int maxChunksPerFrame = 4;

        while (!chunksToUnload.isEmpty() && chunksUnloadedThisFrame < maxChunksPerFrame) {
            Vector3i chunkPos = chunksToUnload.poll();
            if (chunkPos != null) {
                chunkManager.unloadChunk(chunkPos);
                chunksUnloadedThisFrame++;
            }
        }
    }

    public void stopLoading() {
        running = false;
        this.interrupt();
    }

    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
    }
}