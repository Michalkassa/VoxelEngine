package World;

import java.util.ArrayList;
import java.util.List;

public final class LodSelection {

    public static final int MAX_LEVEL = 4;

    private LodSelection() {
    }

    public static List<LodNode> select(int playerChunkX, int playerChunkZ, int detailDistance, int renderDistance) {
        return select(playerChunkX, playerChunkZ, detailDistance, renderDistance, 1);
    }

    // Quadtree over the render area: level 1 starts at detailDistance, and each coarser level starts at
    // detailDistance * spread * 2^(level - 1) chunks, so a larger spread keeps finer detail for longer
    public static List<LodNode> select(int playerChunkX, int playerChunkZ, int detailDistance, int renderDistance, int spread) {
        List<LodNode> selected = new ArrayList<>();
        int rootSize = 1 << MAX_LEVEL;
        int minRootX = Math.floorDiv(playerChunkX - renderDistance, rootSize);
        int maxRootX = Math.floorDiv(playerChunkX + renderDistance, rootSize);
        int minRootZ = Math.floorDiv(playerChunkZ - renderDistance, rootSize);
        int maxRootZ = Math.floorDiv(playerChunkZ + renderDistance, rootSize);

        for (int x = minRootX; x <= maxRootX; x++) {
            for (int z = minRootZ; z <= maxRootZ; z++) {
                collect(new LodNode(MAX_LEVEL, x, z), playerChunkX, playerChunkZ, detailDistance, renderDistance, spread, selected);
            }
        }
        return selected;
    }

    public static int splitDistance(int level, int detailDistance) {
        return splitDistance(level, detailDistance, 1);
    }

    public static int splitDistance(int level, int detailDistance, int spread) {
        if (level == 1) return detailDistance;
        return (detailDistance << (level - 1)) * spread;
    }

    private static void collect(LodNode node, int playerChunkX, int playerChunkZ, int detailDistance,
                                int renderDistance, int spread, List<LodNode> selected) {
        int distance = node.distanceInChunks(playerChunkX, playerChunkZ);
        if (distance > renderDistance) return;

        if (node.level() == 0 || distance >= splitDistance(node.level(), detailDistance, spread)) {
            selected.add(node);
            return;
        }

        int childLevel = node.level() - 1;
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                LodNode child = new LodNode(childLevel, node.x() * 2 + dx, node.z() * 2 + dz);
                collect(child, playerChunkX, playerChunkZ, detailDistance, renderDistance, spread, selected);
            }
        }
    }
}
