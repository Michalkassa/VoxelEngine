package Core;

import org.lwjgl.glfw.GLFW;

public class Input {

    private static final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    private static final boolean[] keysPressed = new boolean[GLFW.GLFW_KEY_LAST];
    private static final boolean[] mouseButtons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
    private static final boolean[] mouseButtonsPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];

    private static float mouseX, mouseY;
    private static float deltaX, deltaY;
    private static float lastX, lastY;
    private static boolean firstMouse = true;

    public static void init(long window){

        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (key < 0) return;

            boolean wasDown = keys[key];
            keys[key] = action != GLFW.GLFW_RELEASE;

            if (action == GLFW.GLFW_PRESS && !wasDown) {
                keysPressed[key] = true;
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button < 0 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) return;

            boolean wasDown = mouseButtons[button];
            mouseButtons[button] = action != GLFW.GLFW_RELEASE;

            if (action == GLFW.GLFW_PRESS && !wasDown) {
                mouseButtonsPressed[button] = true;
            }
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

    public static boolean isKeyPressed(int key) {
        boolean pressed = keysPressed[key];
        keysPressed[key] = false;
        return pressed;
    }

    public static boolean isMouseButtonDown(int button) {
        return mouseButtons[button];
    }

    public static boolean isMouseButtonPressed(int button) {
        boolean pressed = mouseButtonsPressed[button];
        mouseButtonsPressed[button] = false;
        return pressed;
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
