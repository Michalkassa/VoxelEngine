package Core;
import Entity.Player;
import World.World;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;


public class Game {

    private Window window;
    private Renderer renderer;
    private World world;
    private Player player;


    private void init(){
        window = new Window(1920,1080, "VoxelEngine");
        window.create();

        glfwMakeContextCurrent(window.getWindowId());
        GL.createCapabilities();

        player = new Player(new Vector3f(0, 5, 0));
        world = new World("myWorld", player.getCamera(), 1);
        renderer = new Renderer(world, player.getCamera());

        renderer.init();

        Input.init(window.getWindowId());
    }
    private void loop(){
        float lastFrameTime = 0;
        while (!window.ShouldClose()) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            window.update();
            player.update(deltaTime, world.getChunkManager().getChunk(player.getPosition()));
            renderer.update(deltaTime);
            player.render();
            glfwPollEvents();
            glfwSwapBuffers(window.getWindowId());
        }
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
