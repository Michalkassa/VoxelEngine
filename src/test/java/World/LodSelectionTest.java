package World;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LodSelectionTest {

    private static final int DETAIL = 8;

    private static Map<Long, LodNode> coverage(List<LodNode> nodes) {
        Map<Long, LodNode> covered = new HashMap<>();
        for (LodNode node : nodes) {
            for (int x = node.minChunkX(); x <= node.maxChunkX(); x++) {
                for (int z = node.minChunkZ(); z <= node.maxChunkZ(); z++) {
                    LodNode previous = covered.put(key(x, z), node);
                    assertNull(previous, "chunk " + x + "," + z + " covered by " + previous + " and " + node);
                }
            }
        }
        return covered;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    @Test
    void nodeGeometry() {
        LodNode node = new LodNode(2, -1, 3);

        assertEquals(4, node.sizeInChunks());
        assertEquals(4, node.voxelScale());
        assertEquals(-4, node.minChunkX());
        assertEquals(-1, node.maxChunkX());
        assertEquals(12, node.minChunkZ());
        assertEquals(15, node.maxChunkZ());
        assertTrue(node.containsChunk(-4, 12));
        assertTrue(node.containsChunk(-1, 15));
        assertFalse(node.containsChunk(0, 12));
    }

    @Test
    void nodeDistanceIsZeroInsideAndChebyshevOutside() {
        LodNode node = new LodNode(1, 0, 0);

        assertEquals(0, node.distanceInChunks(1, 1));
        assertEquals(3, node.distanceInChunks(4, 0));
        assertEquals(5, node.distanceInChunks(-2, 6));
    }

    @ParameterizedTest
    @CsvSource({"0,0,32", "5,-3,64", "-40,17,128", "100,100,48"})
    void selectionCoversRenderAreaExactlyOnce(int px, int pz, int renderDistance) {
        Map<Long, LodNode> covered = coverage(LodSelection.select(px, pz, DETAIL, renderDistance));

        for (int x = px - renderDistance; x <= px + renderDistance; x++) {
            for (int z = pz - renderDistance; z <= pz + renderDistance; z++) {
                assertTrue(covered.containsKey(key(x, z)), "chunk " + x + "," + z + " not covered");
            }
        }
    }

    @Test
    void nothingIsSelectedFarBeyondRenderDistance() {
        int renderDistance = 40;
        int limit = renderDistance + (1 << LodSelection.MAX_LEVEL);

        for (LodNode node : LodSelection.select(0, 0, DETAIL, renderDistance)) {
            assertTrue(node.distanceInChunks(0, 0) <= renderDistance, node.toString());
            assertTrue(Math.abs(node.minChunkX()) <= limit && Math.abs(node.minChunkZ()) <= limit, node.toString());
        }
    }

    @Test
    void chunksNearPlayerAreFullDetail() {
        Map<Long, LodNode> covered = coverage(LodSelection.select(3, -7, DETAIL, 64));

        for (int x = 3 - (DETAIL - 1); x <= 3 + DETAIL - 1; x++) {
            for (int z = -7 - (DETAIL - 1); z <= -7 + DETAIL - 1; z++) {
                assertEquals(0, covered.get(key(x, z)).level(), "chunk " + x + "," + z);
            }
        }
    }

    @Test
    void fullDetailChunksStayInsideLoadedRadius() {
        for (LodNode node : LodSelection.select(-11, 20, DETAIL, 128)) {
            if (node.level() == 0) {
                assertTrue(node.distanceInChunks(-11, 20) <= DETAIL, node.toString());
            }
        }
    }

    @Test
    void detailNeverIncreasesWithDistance() {
        List<LodNode> nodes = LodSelection.select(0, 0, DETAIL, 128);

        for (LodNode near : nodes) {
            for (LodNode far : nodes) {
                if (far.distanceInChunks(0, 0) >= 2 * near.distanceInChunks(0, 0) + (2 << near.level())) {
                    assertTrue(far.level() >= near.level(), near + " vs " + far);
                }
            }
        }
    }

    @Test
    void distantTerrainUsesCoarsestLevel() {
        Map<Long, LodNode> covered = coverage(LodSelection.select(0, 0, DETAIL, 128));

        assertEquals(LodSelection.MAX_LEVEL, covered.get(key(120, 0)).level());
        assertEquals(LodSelection.MAX_LEVEL, covered.get(key(-100, 110)).level());
    }

    @Test
    void largeRenderDistanceNeedsFarFewerNodesThanChunks() {
        int renderDistance = 128;
        List<LodNode> nodes = LodSelection.select(0, 0, DETAIL, renderDistance);
        int chunks = (2 * renderDistance + 1) * (2 * renderDistance + 1);

        assertTrue(nodes.size() < chunks / 50, nodes.size() + " nodes for " + chunks + " chunks");
    }

    @Test
    void neighbouringNodesDifferByAtMostOneLevel() {
        Map<Long, LodNode> covered = coverage(LodSelection.select(0, 0, DETAIL, 96));

        for (Map.Entry<Long, LodNode> entry : covered.entrySet()) {
            int x = (int) (entry.getKey() >> 32);
            int z = (int) (long) entry.getKey();
            LodNode east = covered.get(key(x + 1, z));
            LodNode south = covered.get(key(x, z + 1));
            if (east != null) assertTrue(Math.abs(east.level() - entry.getValue().level()) <= 1);
            if (south != null) assertTrue(Math.abs(south.level() - entry.getValue().level()) <= 1);
        }
    }

    @Test
    void splitDistanceDoublesEachLevel() {
        assertEquals(DETAIL, LodSelection.splitDistance(1, DETAIL));
        assertEquals(2 * DETAIL, LodSelection.splitDistance(2, DETAIL));
        assertEquals(8 * DETAIL, LodSelection.splitDistance(4, DETAIL));
    }

    @ParameterizedTest
    @CsvSource({"2,0,0", "3,7,-9", "4,-30,12"})
    void spreadSelectionCoversAreaOnceWithGradualLevels(int spread, int px, int pz) {
        int renderDistance = 128;
        Map<Long, LodNode> covered = coverage(LodSelection.select(px, pz, DETAIL, renderDistance, spread));

        for (int x = px - renderDistance; x <= px + renderDistance; x++) {
            for (int z = pz - renderDistance; z <= pz + renderDistance; z++) {
                LodNode node = covered.get(key(x, z));
                assertNotNull(node, "chunk " + x + "," + z + " not covered");
                LodNode east = covered.get(key(x + 1, z));
                LodNode south = covered.get(key(x, z + 1));
                if (east != null) assertTrue(Math.abs(east.level() - node.level()) <= 1);
                if (south != null) assertTrue(Math.abs(south.level() - node.level()) <= 1);
            }
        }
    }

    @Test
    void fullDetailAreaIsSixteenBySixteenChunksWhateverTheSpread() {
        for (int spread = 1; spread <= 4; spread++) {
            long fullDetail = LodSelection.select(0, 0, DETAIL, 128, spread).stream()
                    .filter(node -> node.level() == 0)
                    .count();
            assertEquals(16 * 16, fullDetail, "spread " + spread);
        }
    }

    @Test
    void spreadPushesCoarserLevelsFurtherOut() {
        assertEquals(DETAIL, LodSelection.splitDistance(1, DETAIL, 4));
        assertEquals(64, LodSelection.splitDistance(2, DETAIL, 4));
        assertEquals(128, LodSelection.splitDistance(3, DETAIL, 4));
        assertEquals(LodSelection.splitDistance(3, DETAIL), LodSelection.splitDistance(3, DETAIL, 1));

        Map<Long, LodNode> covered = coverage(LodSelection.select(0, 0, DETAIL, 128, 4));
        assertEquals(1, covered.get(key(40, 0)).level());
        assertEquals(2, covered.get(key(100, 0)).level());
    }
}
