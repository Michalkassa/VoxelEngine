package UI;
import org.lwjgl.nanovg.NVGColor;


import static org.lwjgl.nanovg.NanoVG.*;

public class TextFPS {

    private NVGColor color;
    private float fontSize;

    private static final int SAMPLE_SIZE = 60;
    private float[] samples = new float[SAMPLE_SIZE];
    private int index = 0;

    public TextFPS(float fontSize) {
        this.fontSize = fontSize;
        this.color = NVGColor.create();
        nvgRGBAf(1f, 1f, 1f, 1f, color);
    }

    public void render(long vg, float deltaTime) {
        samples[index] = deltaTime;
        index = (index + 1) % SAMPLE_SIZE;

        float avg = 0;
        for (float s : samples) avg += s;
        avg /= SAMPLE_SIZE;

        int fps = (int) (1.0f / avg);

        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "helvetica");
        nvgFillColor(vg, color);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, 10, 10, "FPS: " + fps);
    }
}