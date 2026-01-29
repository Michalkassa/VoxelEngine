
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

public class Window {

    private static long WINDOW_ID;

    public static void run(int width, int height) {
        // Initialize GLFW
        if (!glfwInit()) {
            throw new RuntimeException("GLFW unable to initialize");
        }

        WINDOW_ID = glfwCreateWindow(width, height, "Display", 0, 0);
        glfwMakeContextCurrent(WINDOW_ID);

        GL.createCapabilities();

        while (!glfwWindowShouldClose(WINDOW_ID)) {
            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glClearColor(1f, 1f, 1f, 1f);

            glfwSwapBuffers(WINDOW_ID);
        }

        glfwDestroyWindow(WINDOW_ID);
        glfwTerminate();
    }

    public static void main(String[] args) {
        Window.run(1280, 720);
    }
}
