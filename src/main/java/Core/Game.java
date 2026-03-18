package Core;
import Entity.Player;
import UI.HudRenderer;
import World.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.sql.Time;

import static org.lwjgl.glfw.GLFW.*;


public class Game {

    private Window window;
    private Renderer renderer;
    private World world;
    private Player player;
    private HudRenderer hudRenderer;

    private final float MAXIMUM_DELTA_TIME = 0.05f;


    private void init(){
        window = new Window(1920,1080, "VoxelEngine");
        window.create();

        glfwMakeContextCurrent(window.getWindowId());
        GL.createCapabilities();

        player = new Player(new Vector3f(0, 150, 0), new Vector3f(0.75f, 1.7f, 0.75f ));
        world = new World("MyWorld", player, 2);
        player.getCamera().setRaycast(new Raycast(world, 4));
        player.setPosition(new Vector3f(0,world.getSurfaceHeight(new Vector3i(0,0,0))+1,0));
        renderer = new Renderer(world, player.getCamera());
        hudRenderer = new HudRenderer(player);

        renderer.init();
        hudRenderer.init();

        Input.init(window.getWindowId());
    }
    private void loop(){
        float lastFrameTime = (float) glfwGetTime();
        glfwSwapInterval(0);

        int[] width = new int[1];
        int[] height = new int[1];
        org.lwjgl.glfw.GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);

        int printInterval = 60;
        int frameCount = 0;

        while (!window.ShouldClose()) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = Math.min(currentTime - lastFrameTime, MAXIMUM_DELTA_TIME);
            lastFrameTime = currentTime;


            window.update();
            world.update();
            player.update(deltaTime, world.getChunkManager());
            player.render();
            renderer.update(deltaTime);
            hudRenderer.render(width[0], height[0], deltaTime);
            glfwSwapBuffers(window.getWindowId());
            glfwPollEvents();
        }
    }
    private void cleanup(){
        window.cleanup();
        renderer.cleanup();
        hudRenderer.cleanup();
    }

    public void run(){
       init();
       loop();
       cleanup();
    }
}
