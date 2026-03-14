package UI;

import Entity.Player;
import org.joml.Vector3f;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextPosition {
    private NVGColor color;
    private float fontSize;
    private Player player;

    public TextPosition(float fontSize, Player player) {
        this.fontSize = fontSize;
        this.player = player;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg) {
        Vector3f pos = player.getPosition();
        String text = String.format("X: %.1f  Y: %.1f  Z: %.1f", pos.x, pos.y, pos.z);

        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 35, text);
    }
}