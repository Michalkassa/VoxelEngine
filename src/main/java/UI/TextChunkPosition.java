package UI;

import Entity.Player;
import World.Chunk;
import org.joml.Vector3f;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextChunkPosition {
    private NVGColor color;
    private float fontSize;
    private Player player;

    public TextChunkPosition(float fontSize, Player player) {
        this.fontSize = fontSize;
        this.player = player;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg) {
        Vector3f pos = player.getPosition();
        int chunkX = Math.floorDiv((int) pos.x, Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv((int) pos.z, Chunk.CHUNK_SIZE);
        String text = String.format("Chunk: %d, %d", chunkX, chunkZ);

        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 85, text);
    }
}