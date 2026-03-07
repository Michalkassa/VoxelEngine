package Core;
import Entity.Player;
import World.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;

import java.sql.Time;

import static org.lwjgl.glfw.GLFW.*;


public class Game {

    private Window window;
    private Renderer renderer;
    private World world;
    private Player player;

    private final float MAXIMUM_DELTA_TIME = 0.05f;


    private void init(){
        window = new Window(1280,720, "VoxelEngine");
        window.create();

        glfwMakeContextCurrent(window.getWindowId());
        GL.createCapabilities();

        player = new Player(new Vector3f(0, 20, 0), new Vector3f(0.75f, 1.7f, 0.75f ));
        world = new World("myWorld", player.getCamera(), 1);
        player.setPosition(new Vector3f(0,world.getSurfaceHeight(new Vector3i(0,0,0))+1,0));
        renderer = new Renderer(world, player.getCamera());

        renderer.init();

        Input.init(window.getWindowId());
    }
    private void loop(){
        float lastFrameTime = (float) glfwGetTime();
        long total = 0;
        long count = 0;
        while (!window.ShouldClose()) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = Math.min(currentTime - lastFrameTime, MAXIMUM_DELTA_TIME);

            lastFrameTime = currentTime;

            total += (int) 1/deltaTime;
            count++;



            window.update();
            world.update();
            renderer.update(deltaTime);
            player.update(deltaTime, world.getChunkManager());
            player.render();

            glfwPollEvents();
            glfwSwapBuffers(window.getWindowId());
        }
        System.out.println("Average FPS:" + total/count);
    }
    private void cleanup(){
        window.cleanup();
        renderer.cleanup();
    }

    public void run(){
       init();
       loop();
       cleanup();
    }
}
