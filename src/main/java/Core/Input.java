package Core;

import org.lwjgl.glfw.GLFW;

public class Input {

    private static final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    private static float mouseX, mouseY;
    private static float deltaX, deltaY;
    private static float lastX, lastY;
    private static boolean firstMouse = true;

    public static void init(long window){

        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (key < 0) return;
            keys[key] = action != GLFW.GLFW_RELEASE;
        });

        GLFW.glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {

            if (firstMouse) {
                lastX = (float) xpos;
                lastY = (float) ypos;
                firstMouse = false;
                return;
            }

            mouseX = (float) xpos;
            mouseY = (float) ypos;

            deltaX = (float) xpos - lastX;
            deltaY = (float) ypos - lastY;

            lastX = (float) xpos;
            lastY = (float) ypos;
        });
    }

    public static boolean isKeyDown(int key) {
        return keys[key];
    }

    public static float getMouseDeltaX(){
        float dx = deltaX;
        deltaX = 0;
        return dx;
    }
    public static float getMouseDeltaY(){
        float dy = deltaY;
        deltaY = 0;
        return dy;
    }
}
