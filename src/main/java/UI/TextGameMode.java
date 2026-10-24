package UI;

import Entity.Player;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextGameMode {
    private NVGColor color;
    private float fontSize;
    private Player player;

    public TextGameMode(float fontSize, Player player) {
        this.fontSize = fontSize;
        this.player = player;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg) {
        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 110, "Mode: " + player.getGameMode());
    }
}
