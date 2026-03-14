package UI;

import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class Crosshair {
    private final float length;
    private final float thickness;

    private NVGColor color;
    private boolean visible;

    public Crosshair(float length, float thickness) {
        this.length = length;
        this.thickness = thickness;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 0.5f, color);
        this.visible = true;
    }

    public void render(long vg, float cx, float cy) {
        if (!visible) return;

        nvgBeginPath(vg);

        nvgRect(vg, cx - length, cy - thickness / 2, length * 2, thickness);
        nvgRect(vg, cx - thickness / 2, cy - length, thickness, length * 2);

        nvgFillColor(vg, color);
        nvgFill(vg);
    }

}