package UI;

import Entity.GameMode;
import Entity.Player;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextHealth {
    private NVGColor color;
    private float fontSize;
    private Player player;

    public TextHealth(float fontSize, Player player) {
        this.fontSize = fontSize;
        this.player = player;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg) {
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 135, String.format("Health: %.0f / %.0f", player.getHealth(), Player.MAX_HEALTH));
    }
}
