package Core;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;


public class Game {

    private Window window;
    private Renderer renderer;

    private void init(){
        window = new Window(720,720, "VoxelEngine");
        window.create();

        glfwMakeContextCurrent(window.getWindowId());
        GL.createCapabilities();

        renderer = new Renderer();
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
            renderer.update(deltaTime);
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
