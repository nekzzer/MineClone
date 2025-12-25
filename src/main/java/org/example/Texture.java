package org.example;

import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private int id;
    private int width, height;

    public Texture(String fileName) {
        ByteBuffer image;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            String filePath = "src/main/resources/" + fileName;
            System.out.println("Попытка загрузить: " + filePath);

            image = stbi_load(filePath, w, h, comp, 4);
            if (image == null) {
                filePath = fileName;
                image = stbi_load(filePath, w, h, comp, 4);

                if (image == null) {
                    throw new RuntimeException("ОШИБКА: Текстура не найдена! " + stbi_failure_reason());
                }
            }

            width = w.get();
            height = h.get();
            System.out.println("Текстура успешно загружена! " + width + "x" + height);
        }

        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        stbi_image_free(image);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}