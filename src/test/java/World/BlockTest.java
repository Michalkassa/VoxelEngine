package World;

import Entity.GameMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void airIsBlockZero() {
        assertEquals(0, Block.AIR.ordinal());
    }

    @Test
    void blockOrderIsStableForSavedChunks() {
        assertArrayEquals(
                new Block[]{Block.AIR, Block.GRASS, Block.DIRT, Block.STONE, Block.SAND, Block.SNOW, Block.WOOD, Block.LEAF, Block.WATER},
                Block.values());
    }

    @Test
    void everyBlockFitsInAByte() {
        assertTrue(Block.values().length <= Byte.MAX_VALUE);
    }

    @Test
    void gameModesAreSurvivalAndCreative() {
        assertArrayEquals(new GameMode[]{GameMode.SURVIVAL, GameMode.CREATIVE}, GameMode.values());
    }
}
