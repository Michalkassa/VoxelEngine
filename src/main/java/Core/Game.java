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
        world = new World("MyWorld", player.getCamera(), player, 2);
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


            long t0 = System.nanoTime();
            window.update();
            long t1 = System.nanoTime();
            world.update();
            long t2 = System.nanoTime();
            player.update(deltaTime, world.getChunkManager());
            long t3 = System.nanoTime();
            player.render();
            long t4 = System.nanoTime();
            renderer.update(deltaTime);
            long t5 = System.nanoTime();
            hudRenderer.render(width[0], height[0], deltaTime);
            long t6 = System.nanoTime();
            glfwSwapBuffers(window.getWindowId());
            long t7 = System.nanoTime();
            glfwPollEvents();
            long t8 = System.nanoTime();

            frameCount++;
            if (frameCount % printInterval == 0) {
                System.out.printf(
                        "window: %.2fms  world: %.2fms  player: %.2fms  render: %.2fms  hud: %.2fms  swap: %.2fms  poll: %.2fms  | total: %.2fms%n",
                        (t1-t0)/1e6, (t2-t1)/1e6, (t3-t2)/1e6,
                        (t5-t4)/1e6, (t6-t5)/1e6, (t7-t6)/1e6, (t8-t7)/1e6,
                        (t8-t0)/1e6
                );
            }
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
