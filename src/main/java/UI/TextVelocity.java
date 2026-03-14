package UI;

import Entity.Player;
import org.joml.Vector3f;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextVelocity {
    private NVGColor color;
    private float fontSize;
    private Player player;

    public TextVelocity(float fontSize, Player player) {
        this.fontSize = fontSize;
        this.player = player;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg) {
        Vector3f vel = player.getVelocity();
        String text = String.format("VX: %.1f  VY: %.1f  VZ: %.1f", vel.x, vel.y, vel.z);

        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 60, text);
    }
}