package org.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Camera {
    private float x, y, z;
    private float pitch, yaw;

    private float moveSpeed = 4.3f;
    private float mouseSensitivity = 0.12f;
    private float gravity = 18.0f;
    private float velocityY = 0;
    private boolean onGround = false;

    private float playerWidth = 0.6f;
    private float playerHeight = 1.8f;
    private float eyeHeight = 1.62f;

    private long lastTime;
    private long lastActionTime = 0;
    private float interactionRange = 5.0f;

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0;
        this.yaw = -90;
        this.lastTime = System.nanoTime();
        if (this.y < 20) this.y = 20;
    }

    public void processInput(long window, World world) {
        long currentTime = System.nanoTime();
        float dt = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;
        if (dt > 0.1f) dt = 0.1f;

        float dx = 0, dz = 0;
        float speed = moveSpeed * dt;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            dx += Math.sin(Math.toRadians(yaw)) * speed;
            dz -= Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            dx -= Math.sin(Math.toRadians(yaw)) * speed;
            dz += Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            dx -= Math.cos(Math.toRadians(yaw)) * speed;
            dz -= Math.sin(Math.toRadians(yaw)) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            dx += Math.cos(Math.toRadians(yaw)) * speed;
            dz += Math.sin(Math.toRadians(yaw)) * speed;
        }

        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround) {
            velocityY = 6.5f;
            onGround = false;
        }

        if (!checkCollision(x + dx, y, z, world)) x += dx;
        if (!checkCollision(x, y, z + dz, world)) z += dz;

        velocityY -= gravity * dt;
        if (velocityY < -20) velocityY = -20;

        float nextY = y + velocityY * dt;

        if (velocityY < 0) {
            if (checkCollision(x, nextY, z, world)) {
                y = (float) Math.floor(y - playerHeight/2) + 0.5f + playerHeight/2 + 0.0001f;
                velocityY = 0;
                onGround = true;
            } else {
                y = nextY;
                onGround = false;
            }
        } else if (velocityY > 0) {
            if (checkCollision(x, nextY + 0.1f, z, world)) {
                velocityY = 0;
            } else {
                y = nextY;
                onGround = false;
            }
        }
    }

    public void processBlockInteraction(long window, World world) {
        if (System.currentTimeMillis() - lastActionTime < 200) return;

        boolean leftClick = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
        boolean rightClick = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS;

        if (!leftClick && !rightClick) return;

        float step = 0.05f;
        float xDir = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float yDir = (float) (-Math.sin(Math.toRadians(pitch)));
        float zDir = (float) (-Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        float eyeY = y - playerHeight / 2 + eyeHeight;
        float currX = x;
        float currY = eyeY;
        float currZ = z;

        int prevX = (int) Math.round(currX);
        int prevY = (int) Math.round(currY);
        int prevZ = (int) Math.round(currZ);

        for (float d = 0; d < interactionRange; d += step) {
            currX += xDir * step;
            currY += yDir * step;
            currZ += zDir * step;

            int bx = (int) Math.round(currX);
            int by = (int) Math.round(currY);
            int bz = (int) Math.round(currZ);

            Block target = world.getBlockAt(bx, by, bz);

            if (target != null) {
                if (leftClick) {
                    world.removeBlock(bx, by, bz);
                    lastActionTime = System.currentTimeMillis();
                } else if (rightClick) {
                    if (!checkPlayerInBlock(prevX, prevY, prevZ)) {
                        world.addBlock(prevX, prevY, prevZ, Block.BlockType.STONE);
                        lastActionTime = System.currentTimeMillis();
                    }
                }
                return;
            }
            prevX = bx;
            prevY = by;
            prevZ = bz;
        }
    }

    private boolean checkPlayerInBlock(int bx, int by, int bz) {
        float pMinX = x - playerWidth/2;
        float pMaxX = x + playerWidth/2;
        float pMinY = y - playerHeight/2;
        float pMaxY = y + playerHeight/2;
        float pMinZ = z - playerWidth/2;
        float pMaxZ = z + playerWidth/2;

        return pMaxX > bx - 0.5f && pMinX < bx + 0.5f &&
                pMaxY > by - 0.5f && pMinY < by + 0.5f &&
                pMaxZ > bz - 0.5f && pMinZ < bz + 0.5f;
    }

    private boolean checkCollision(float px, float py, float pz, World world) {
        for (Block block : world.getBlocks()) {
            if (Math.abs(block.getX() - px) > 2 || Math.abs(block.getY() - py) > 3 || Math.abs(block.getZ() - pz) > 2) continue;

            float bx = block.getX();
            float by = block.getY();
            float bz = block.getZ();

            if (px + playerWidth/2 > bx - 0.5f && px - playerWidth/2 < bx + 0.5f &&
                    pz + playerWidth/2 > bz - 0.5f && pz - playerWidth/2 < bz + 0.5f &&
                    py + playerHeight/2 > by - 0.5f && py - playerHeight/2 < by + 0.5f) {
                return true;
            }
        }
        return false;
    }

    public void processMouse(double xoffset, double yoffset) {
        yaw += xoffset * mouseSensitivity;
        pitch -= yoffset * mouseSensitivity;
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    public void apply() {
        float eyeY = y - playerHeight / 2 + eyeHeight;
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        glTranslatef(-x, -eyeY, -z);
    }

    public float getX() { return x; }
    public float getY() { return y - playerHeight / 2 + eyeHeight; }
    public float getZ() { return z; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
}
