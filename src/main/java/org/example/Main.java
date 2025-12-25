package org.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;
    private Camera camera;
    private World world;
    private Texture atlas;
    private double lastX, lastY;
    private boolean firstMouse = true;
    private Block.BlockType selectedBlockType = Block.BlockType.GRASS;
    private int screenWidth, screenHeight;

    private int fps = 0;
    private int frameCount = 0;
    private double lastFpsTime = 0;

    public void run() {
        init();
        loop();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("Failed to init GLFW");

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        screenWidth = vidmode.width();
        screenHeight = vidmode.height();
        lastX = screenWidth / 2.0;
        lastY = screenHeight / 2.0;

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        window = glfwCreateWindow(screenWidth, screenHeight, "MineClone 0.1", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create window");

        glfwSetWindowPos(window, 0, 0);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_TEXTURE_2D);

        atlas = new Texture("atlas.png");
        atlas.bind();

        world = new World();
        camera = new Camera(0, 3, 0);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (firstMouse) {
                lastX = xpos;
                lastY = ypos;
                firstMouse = false;
            }
            double xoffset = xpos - lastX;
            double yoffset = lastY - ypos;
            lastX = xpos;
            lastY = ypos;
            camera.processMouse(xoffset, yoffset);
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                int[] targetPos = getTargetBlock();
                if (targetPos != null) {
                    if (button == GLFW_MOUSE_BUTTON_LEFT) {
                        world.removeBlock(targetPos[0], targetPos[1], targetPos[2]);
                    } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                        world.addBlock(targetPos[3], targetPos[4], targetPos[5], selectedBlockType);
                    }
                }
            }
        });

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) selectedBlockType = Block.BlockType.GRASS;
                else if (key == GLFW_KEY_2) selectedBlockType = Block.BlockType.DIRT;
                else if (key == GLFW_KEY_3) selectedBlockType = Block.BlockType.STONE;
                else if (key == GLFW_KEY_4) selectedBlockType = Block.BlockType.WOOD;
                else if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(window, true);
            }
        });
    }

    private void loop() {
        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);
        lastFpsTime = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            frameCount++;
            double currentTime = glfwGetTime();
            if (currentTime - lastFpsTime >= 1.0) {
                fps = frameCount;
                frameCount = 0;
                lastFpsTime = currentTime;
                glfwSetWindowTitle(window, "MineClone 0.1 | FPS: " + fps);
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            camera.processInput(window, world);
            
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            float aspect = (float) screenWidth / screenHeight;
            float fov = 70.0f;
            float near = 0.1f;
            float far = 100.0f;
            float top = near * (float) Math.tan(Math.toRadians(fov) / 2);
            float bottom = -top;
            float right = top * aspect;
            float left = -right;
            glFrustum(left, right, bottom, top, near, far);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            camera.apply();

            world.render();
            renderHUD();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void renderHUD() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, screenWidth, screenHeight, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glColor4f(1.0f, 1.0f, 1.0f, 0.8f);
        glLineWidth(2.0f);

        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        float size = 10.0f;

        glBegin(GL_LINES);
        glVertex2f(centerX - size, centerY);
        glVertex2f(centerX + size, centerY);
        glVertex2f(centerX, centerY - size);
        glVertex2f(centerX, centerY + size);
        glEnd();

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        drawText("FPS: " + fps, 10, 25, 2.0f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    private void drawText(String text, float x, float y, float scale) {
        float charWidth = 8 * scale;
        for (int i = 0; i < text.length(); i++) {
            drawChar(text.charAt(i), x + i * charWidth, y, scale);
        }
    }

    private void drawChar(char c, float x, float y, float scale) {
        float s = scale;
        glBegin(GL_LINES);
        switch (c) {
            case 'F':
                glVertex2f(x, y); glVertex2f(x, y + 10*s);
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y + 5*s); glVertex2f(x + 4*s, y + 5*s);
                break;
            case 'P':
                glVertex2f(x, y); glVertex2f(x, y + 10*s);
                glVertex2f(x, y); glVertex2f(x + 5*s, y);
                glVertex2f(x + 5*s, y); glVertex2f(x + 5*s, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 5*s, y + 5*s);
                break;
            case 'S':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x + 6*s, y + 5*s); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case ':':
                glVertex2f(x + 2*s, y + 2*s); glVertex2f(x + 2*s, y + 3*s);
                glVertex2f(x + 2*s, y + 7*s); glVertex2f(x + 2*s, y + 8*s);
                break;
            case ' ': break;
            case '0':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 10*s);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '1':
                glVertex2f(x + 3*s, y); glVertex2f(x + 3*s, y + 10*s);
                break;
            case '2':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x, y + 10*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '3':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '4':
                glVertex2f(x, y); glVertex2f(x, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '5':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 5*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x + 6*s, y + 5*s); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '6':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 10*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x + 6*s, y + 5*s); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '7':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '8':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 10*s);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
            case '9':
                glVertex2f(x, y); glVertex2f(x + 6*s, y);
                glVertex2f(x, y); glVertex2f(x, y + 5*s);
                glVertex2f(x + 6*s, y); glVertex2f(x + 6*s, y + 10*s);
                glVertex2f(x, y + 5*s); glVertex2f(x + 6*s, y + 5*s);
                glVertex2f(x, y + 10*s); glVertex2f(x + 6*s, y + 10*s);
                break;
        }
        glEnd();
    }

    private int[] getTargetBlock() {
        float x = camera.getX();
        float y = camera.getY();
        float z = camera.getZ();

        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        float dx = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float dy = (float) (-Math.sin(Math.toRadians(pitch)));
        float dz = (float) (-Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        float step = 0.1f;
        float maxDistance = 5.0f;

        for (float dist = 0; dist < maxDistance; dist += step) {
            float checkX = x + dx * dist;
            float checkY = y + dy * dist;
            float checkZ = z + dz * dist;

            int blockX = Math.round(checkX);
            int blockY = Math.round(checkY);
            int blockZ = Math.round(checkZ);

            for (Block block : world.getBlocks()) {
                if (block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
                    float prevX = x + dx * (dist - step);
                    float prevY = y + dy * (dist - step);
                    float prevZ = z + dz * (dist - step);

                    return new int[]{
                        blockX, blockY, blockZ,
                        Math.round(prevX), Math.round(prevY), Math.round(prevZ)
                    };
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
