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
    }
    private void loop(){

        while (!window.ShouldClose()) {
            window.update();
            renderer.update();
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
