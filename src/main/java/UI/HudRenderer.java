package UI;

import Entity.Player;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL46.*;


public class HudRenderer {
    private long vg;

    private Player player;

    private Crosshair crosshair;
    private TextFPS textFPS;
    private TextPosition textPosition;
    public TextVelocity textVelocity;
    public TextChunkPosition textChunkPosition;

    public HudRenderer(Player player) {
        this.player = player;
    }

    public void init() {
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        nvgCreateFont(vg, "helvetica", "/System/Library/Fonts/Helvetica.ttc");
        crosshair = new Crosshair(6f, 1f);
        textFPS = new TextFPS(20f);
        textPosition = new TextPosition(20, player);
        textVelocity = new TextVelocity(20, player);
        textChunkPosition = new TextChunkPosition(20, player);
    }

    public void render(int width, int height, float dt) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        nvgBeginFrame(vg, width, height, 1);
        //render ui here
        crosshair.render(vg, width / 2f, height / 2f);
        textFPS.render(vg, dt);
        textPosition.render(vg);
        textVelocity.render(vg);
        textChunkPosition.render(vg);
        //stop rendering ui here
        nvgEndFrame(vg);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glDisable(GL_BLEND);
    }

    public void cleanup() {
        nvgDelete(vg);
    }


}