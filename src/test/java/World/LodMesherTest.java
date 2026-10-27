package World;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LodMesherTest {

    private static class FlatGenerator extends TerrainGenerator {
        private final int height;

        FlatGenerator(int height) {
            super(0);
            this.height = height;
        }

        @Override
        public int getHeight(int x, int z) {
            return height;
        }

        @Override
        public Column describeColumn(int worldX, int worldZ, int height, int slope) {
            return new Column(height, Block.GRASS, 4);
        }
    }

    private static Vector3f corner(int vertex) {
        return new Vector3f(Chunk.unpackX(vertex), Chunk.unpackY(vertex), Chunk.unpackZ(vertex));
    }

    private static Vector3f normal(int[] data, int quad) {
        Vector3f a = corner(data[quad * 4]);
        Vector3f b = corner(data[quad * 4 + 1]);
        Vector3f c = corner(data[quad * 4 + 2]);
        return new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
    }

    @Test
    void voxelHeightRoundsUp() {
        assertEquals(32, LodMesher.voxelHeight(64, 2));
        assertEquals(33, LodMesher.voxelHeight(65, 2));
        assertEquals(17, LodMesher.voxelHeight(65, 4));
        assertEquals(1, LodMesher.voxelHeight(0, 8));
        assertEquals(1, LodMesher.voxelHeight(1, 16));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void flatTerrainHasTopFacesAndOneVoxelSkirt(int level) {
        int scale = 1 << level;
        int[] data = LodMesher.buildVertexData(new LodNode(level, 0, 0), new FlatGenerator(64));

        int top = 0, side = 0;
        for (int quad = 0; quad < data.length / 4; quad++) {
            Vector3f n = normal(data, quad);
            if (n.y > 0.5f) top++;
            else if (Math.abs(n.y) < 0.5f) side++;
            else fail("unexpected bottom face");
        }

        assertEquals(16 * 16, top);
        assertEquals(4 * 16, side);
        for (int vertex : data) {
            assertTrue(Chunk.unpackY(vertex) <= LodMesher.voxelHeight(64, scale));
        }
    }

    @Test
    void verticesStayInsideRegionGrid() {
        int[] data = LodMesher.buildVertexData(new LodNode(3, -2, 5), new TerrainGenerator(9));

        for (int vertex : data) {
            assertTrue(Chunk.unpackX(vertex) >= 0 && Chunk.unpackX(vertex) <= 16);
            assertTrue(Chunk.unpackZ(vertex) >= 0 && Chunk.unpackZ(vertex) <= 16);
            assertTrue(Chunk.unpackY(vertex) >= 0 && Chunk.unpackY(vertex) <= 256 / 8 + 1);
        }
    }

    @Test
    void topSurfaceNeverSitsBelowSampledHeight() {
        TerrainGenerator generator = new TerrainGenerator(4);
        LodNode node = new LodNode(2, 1, 1);
        int scale = node.voxelScale();
        int[] data = LodMesher.buildVertexData(node, generator);

        for (int quad = 0; quad < data.length / 4; quad++) {
            if (normal(data, quad).y < 0.5f) continue;
            Vector3f a = corner(data[quad * 4]);
            Vector3f c = corner(data[quad * 4 + 2]);
            int i = (int) Math.min(a.x, c.x);
            int k = (int) Math.min(a.z, c.z);
            int worldX = node.minChunkX() * 16 + i * scale + scale / 2;
            int worldZ = node.minChunkZ() * 16 + k * scale + scale / 2;
            assertTrue(a.y * scale >= generator.getHeight(worldX, worldZ), "column " + i + "," + k);
        }
    }

    @Test
    void facesWindOutwardFromTheirColumn() {
        int[] data = LodMesher.buildVertexData(new LodNode(2, 0, 0), new TerrainGenerator(11));

        for (int quad = 0; quad < data.length / 4; quad++) {
            Vector3f faceCenter = new Vector3f();
            for (int i = 0; i < 4; i++) faceCenter.add(corner(data[quad * 4 + i]));
            faceCenter.div(4);
            Vector3f n = normal(data, quad);
            Vector3f inside = new Vector3f(faceCenter).sub(new Vector3f(n).mul(0.5f));
            Vector3f outside = new Vector3f(faceCenter).add(new Vector3f(n).mul(0.5f));
            assertEquals(1f, n.length(), 1e-5f);
            assertTrue(Math.floor(inside.x) != Math.floor(outside.x)
                    || Math.floor(inside.y) != Math.floor(outside.y)
                    || Math.floor(inside.z) != Math.floor(outside.z));
        }
    }

    @Test
    void steepTerrainExposesSideFaces() {
        TerrainGenerator stairs = new TerrainGenerator(0) {
            @Override
            public int getHeight(int x, int z) {
                return 40 + Math.floorDiv(x, 2) * 2;
            }
        };

        int[] data = LodMesher.buildVertexData(new LodNode(1, 0, 0), stairs);
        int sides = 0;
        for (int quad = 0; quad < data.length / 4; quad++) {
            if (Math.abs(normal(data, quad).y) < 0.5f) sides++;
        }

        assertTrue(sides > 4 * 16, "only " + sides + " side faces");
    }

    @Test
    void usesGeneratorSurfaceBlocks() {
        TerrainGenerator sandy = new FlatGenerator(64) {
            @Override
            public Column describeColumn(int worldX, int worldZ, int height, int slope) {
                return new Column(height, Block.SAND, 5);
            }
        };
        int[] grass = LodMesher.buildVertexData(new LodNode(1, 0, 0), new FlatGenerator(64));
        int[] sand = LodMesher.buildVertexData(new LodNode(1, 0, 0), sandy);

        Set<Float> grassU = new HashSet<>(), sandU = new HashSet<>();
        for (int vertex : grass) grassU.add(Chunk.unpackU(vertex));
        for (int vertex : sand) sandU.add(Chunk.unpackU(vertex));

        assertNotEquals(grassU, sandU);
    }

    @Test
    void sameNodeBuildsIdenticalMesh() {
        TerrainGenerator generator = new TerrainGenerator(21);
        LodNode node = new LodNode(4, -1, 0);

        assertArrayEquals(LodMesher.buildVertexData(node, generator), LodMesher.buildVertexData(node, generator));
    }

    @Test
    void lodMeshIsMuchSmallerThanFullDetailForSameArea() {
        TerrainGenerator generator = new TerrainGenerator(2);
        LodNode node = new LodNode(2, 0, 0);
        int lodQuads = LodMesher.buildVertexData(node, generator).length / 4;

        ChunkManager chunkManager = new ChunkManager(2);
        for (int x = -1; x <= 4; x++) for (int z = -1; z <= 4; z++) chunkManager.loadChunkAsync(new org.joml.Vector3i(x, 0, z));
        int fullQuads = 0;
        for (int x = 0; x < 4; x++) for (int z = 0; z < 4; z++) {
            fullQuads += chunkManager.getChunk(new org.joml.Vector3i(x, 0, z)).buildVertexData().length / 4;
        }

        assertTrue(lodQuads * 4 < fullQuads, lodQuads + " LOD quads vs " + fullQuads + " full quads");
    }
}
