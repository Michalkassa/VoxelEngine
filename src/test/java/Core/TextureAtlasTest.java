package Core;

import World.Block;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class TextureAtlasTest {

    private static final float EPS = 1e-6f;
    private static final float TILE = 0.25f;

    private static void assertTileBounds(float[][] mapping, float uMin, float vMin) {
        assertEquals(6, mapping.length);
        for (float[] uv : mapping) {
            assertEquals(2, uv.length);
            assertTrue(Math.abs(uv[0] - uMin) < EPS || Math.abs(uv[0] - (uMin + TILE)) < EPS, "u out of tile: " + uv[0]);
            assertTrue(Math.abs(uv[1] - vMin) < EPS || Math.abs(uv[1] - (vMin + TILE)) < EPS, "v out of tile: " + uv[1]);
        }
    }

    @Test
    void grassUsesDifferentTilesForTopSideAndBottom() {
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.GRASS), 0.5f, 0.25f);
        assertTileBounds(TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.GRASS), 0.75f, 0.5f);
        assertTileBounds(TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.GRASS), 0.5f, 0.5f);
    }

    @Test
    void tileIndexMapsRowsFromTopOfAtlas() {
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.SAND), 0.75f, 0.75f);
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.SNOW), 0.25f, 0.5f);
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.STONE), 0.25f, 0.25f);
    }

    @Test
    void topAndBottomMappingsMatchCubeFaceWindingOrder() {
        float[][] top = TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.DIRT);

        assertArrayEquals(new float[]{0.5f, 0.5f}, top[0], EPS);
        assertArrayEquals(new float[]{0.5f, 0.75f}, top[1], EPS);
        assertArrayEquals(new float[]{0.75f, 0.75f}, top[2], EPS);
        assertArrayEquals(top[2], top[3], EPS);
        assertArrayEquals(new float[]{0.75f, 0.5f}, top[4], EPS);
        assertArrayEquals(top[0], top[5], EPS);
        assertArrayEquals(top, TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.DIRT));
    }

    @Test
    void sideMappingIsMirroredHorizontally() {
        float[][] side = TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.DIRT);

        assertArrayEquals(new float[]{0.75f, 0.5f}, side[0], EPS);
        assertArrayEquals(new float[]{0.5f, 0.5f}, side[1], EPS);
        assertArrayEquals(new float[]{0.5f, 0.75f}, side[2], EPS);
        assertArrayEquals(side[2], side[3], EPS);
        assertArrayEquals(new float[]{0.75f, 0.75f}, side[4], EPS);
        assertArrayEquals(side[0], side[5], EPS);
    }

    @ParameterizedTest
    @EnumSource(value = Block.class, names = "AIR", mode = EnumSource.Mode.EXCLUDE)
    void everySolidBlockHasMatchingTextureByOrdinal(Block block) {
        TextureAtlas.BlockTextures texture = TextureAtlas.BlockTextures.values()[block.ordinal() - 1];

        assertEquals(block.name(), texture.name());
        assertNotNull(TextureAtlas.TEXTURE_ATLAS_RELATIVE_INDEX.get(texture), "no atlas entry for " + texture);
    }

    @Test
    void everyAtlasEntryHasThreeIndicesInsideAtlas() {
        TextureAtlas.TEXTURE_ATLAS_RELATIVE_INDEX.forEach((texture, indices) -> {
            assertEquals(3, indices.length, texture.name());
            for (int index : indices) {
                assertTrue(index >= 0 && index < 16, texture + " index " + index);
            }
        });
    }

    private static void withoutAtlasEntry(TextureAtlas.BlockTextures texture, Runnable check) {
        int[] saved = TextureAtlas.TEXTURE_ATLAS_RELATIVE_INDEX.remove(texture);
        try {
            check.run();
        } finally {
            TextureAtlas.TEXTURE_ATLAS_RELATIVE_INDEX.put(texture, saved);
        }
    }

    @Test
    void woodAndLeafHaveTheirOwnTextures() {
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.WOOD), 0f, 0f);
        assertTileBounds(TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.WOOD), 0.25f, 0.75f);
        assertTileBounds(TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.WOOD), 0f, 0f);
        assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.LEAF), 0f, 0.25f);
    }

    @Test
    void topMappingFallsBackToDirtForUnmappedTexture() {
        float[][] dirt = TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.DIRT);

        withoutAtlasEntry(TextureAtlas.BlockTextures.WOOD, () ->
                assertArrayEquals(dirt, TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.WOOD)));
    }

    @Test
    void sideMappingFallsBackToDirtForUnmappedTexture() {
        float[][] dirt = TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.DIRT);

        withoutAtlasEntry(TextureAtlas.BlockTextures.WOOD, () ->
                assertArrayEquals(dirt, TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.WOOD)));
    }

    @Test
    void bottomMappingFallsBackToDirtForUnmappedTexture() {
        float[][] dirt = TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.DIRT);

        withoutAtlasEntry(TextureAtlas.BlockTextures.WOOD, () ->
                assertArrayEquals(dirt, TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.WOOD)));
    }

    @Test
    void fallbackUsesTileZeroWhenDirtIsAlsoUnmapped() {
        withoutAtlasEntry(TextureAtlas.BlockTextures.DIRT, () ->
                withoutAtlasEntry(TextureAtlas.BlockTextures.WOOD, () -> {
                    assertTileBounds(TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.WOOD), 0f, 0.75f);
                    assertTileBounds(TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.WOOD), 0f, 0.75f);
                    assertTileBounds(TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.WOOD), 0f, 0.75f);
                }));
    }
}
