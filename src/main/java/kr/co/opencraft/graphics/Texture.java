package kr.co.opencraft.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_GENERATE_MIPMAP;

public class Texture {

    private final int id;
    private final int width;
    private final int height;

    public Texture(String path) {
        // 리소스 파일을 바이트로 읽기
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture file: " + path, e);
        }

        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(fileBytes.length);
        imageBuffer.put(fileBytes);
        imageBuffer.flip();

        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);

        // STB 이미지 디코딩 (RGBA 강제)
        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, x, y, comp, 4);
        if (image == null) {
            throw new RuntimeException("Failed to decode texture: " + STBImage.stbi_failure_reason());
        }

        width = x.get(0);
        height = y.get(0);

        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);

        // 간단한 필터/랩핑 설정
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);

        STBImage.stbi_image_free(image);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
        glDeleteTextures(id);
    }
}
