package Core;

import static org.lwjgl.glfw.GLFW.*;


public class Window {

    private static long WINDOW_ID;
    private final int WIDTH;
    private final int HEIGHT;
    private final String TITLE;

    public Window(int width, int height, String title){
        this.WIDTH = width;
        this.HEIGHT = height;
        this.TITLE = title;
    }

    public void create() {
        if (!glfwInit()) {
            throw new RuntimeException("GLFW unable to initialize");
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        WINDOW_ID = glfwCreateWindow(WIDTH, HEIGHT, TITLE, 0, 0);
    }

    public void update(){

    }

    public boolean ShouldClose(){
        return glfwWindowShouldClose(WINDOW_ID);
    }


    public void cleanup(){
        glfwDestroyWindow(WINDOW_ID);
        glfwTerminate();
    }

    public long getWindowId(){
        return WINDOW_ID;
    }


}

