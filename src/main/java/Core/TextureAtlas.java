package Core;

import java.lang.reflect.Array;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class TextureAtlas {

    private static final int TEXTURE_SIZE = 32;
    private static final int  RELATIVE_ATLAS_WIDTH = 4;
    private static final int RELATIVE_ATLAS_HEIGHT= 4;

    public enum BlockTextures {
        GRASS,
        DIRT,
        SAND,
        SNOW,
        WOOD,
        LEAF,
        STONE,
    }


    // TODO temporary solution
    //FirstIndex = enum from BlockTextures
    //SecondIndex : 0 = top , 1 = side , 2 = bottom

    public static final Map<BlockTextures, int[]> TEXTURE_ATLAS_RELATIVE_INDEX = new HashMap<>() {{
        put(BlockTextures.GRASS, new int[]{ 10, 7, 6 });
    }};

    private static final float[] CUBE_TEXTURE_MAPPING = {
            0f, 0f,
            0f, 1f,
            1f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f
    };

    public static float[][] topTextureMapping(BlockTextures BlockTexture) {
        int index = TEXTURE_ATLAS_RELATIVE_INDEX.get(BlockTexture)[0];
        int tileX = index % RELATIVE_ATLAS_WIDTH;
        int tileY = index / RELATIVE_ATLAS_WIDTH;

        float uMin = (float) tileX / RELATIVE_ATLAS_WIDTH;
        float uMax = (float) (tileX + 1) / RELATIVE_ATLAS_WIDTH;

        float vMin = 1.0f - (float) (tileY + 1) / RELATIVE_ATLAS_HEIGHT;
        float vMax = 1.0f - (float) tileY / RELATIVE_ATLAS_HEIGHT;

        return new float[][]{
                { uMin, vMin },
                { uMin, vMax },
                { uMax, vMax },
                { uMax, vMax },
                { uMax, vMin },
                { uMin, vMin }
        };
    }



    public static float[][] sideTextureMapping(BlockTextures BlockTexture) {
        int index = TEXTURE_ATLAS_RELATIVE_INDEX.get(BlockTexture)[1];
        int tileX = index % RELATIVE_ATLAS_WIDTH;
        int tileY = index / RELATIVE_ATLAS_WIDTH;

        float uMin = (float) tileX / RELATIVE_ATLAS_WIDTH;
        float uMax = (float) (tileX + 1) / RELATIVE_ATLAS_WIDTH;

        float vMin = 1.0f - (float) (tileY + 1) / RELATIVE_ATLAS_HEIGHT;
        float vMax = 1.0f - (float) tileY / RELATIVE_ATLAS_HEIGHT;

        return new float[][]{
                { uMin, vMin },
                { uMin, vMax },
                { uMax, vMax },
                { uMax, vMax },
                { uMax, vMin },
                { uMin, vMin }
        };
    }


    public static float[][] bottomTextureMapping(BlockTextures BlockTexture) {
        int index = TEXTURE_ATLAS_RELATIVE_INDEX.get(BlockTexture)[2];
        int tileX = index % RELATIVE_ATLAS_WIDTH;
        int tileY = index / RELATIVE_ATLAS_WIDTH;

        float uMin = (float) tileX / RELATIVE_ATLAS_WIDTH;
        float uMax = (float) (tileX + 1) / RELATIVE_ATLAS_WIDTH;

        float vMin = 1.0f - (float) (tileY + 1) / RELATIVE_ATLAS_HEIGHT;
        float vMax = 1.0f - (float) tileY / RELATIVE_ATLAS_HEIGHT;

        return new float[][]{
                { uMin, vMin },
                { uMin, vMax },
                { uMax, vMax },
                { uMax, vMax },
                { uMax, vMin },
                { uMin, vMin }
        };
    }












}
