/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.util;

import com.google.common.io.ByteStreams;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class WindowUtils {

    /**
     * Credit goes to Forge's ClientVisualization for most of the following:
     */
    public static void setWindowIcon(final long windowRef, final InputStream stream) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer iconWidth = stack.mallocInt(1);
            IntBuffer iconHeight = stack.mallocInt(1);
            IntBuffer iconChannels = stack.mallocInt(1);
            GLFWImage.Buffer glfwImages = GLFWImage.mallocStack(1, stack);

            try {
                byte[] icon = ByteStreams.toByteArray(stream);
                ByteBuffer iconBuf = stack.malloc(icon.length);
                iconBuf.put(icon);
                iconBuf.position(0);
                ByteBuffer imgBuffer = STBImage.stbi_load_from_memory(iconBuf, iconWidth, iconHeight, iconChannels, 4);
                if (imgBuffer == null) {
                    throw new NullPointerException("Failed to load window icon");
                }

                glfwImages.position(0);
                glfwImages.width(iconWidth.get(0));
                glfwImages.height(iconHeight.get(0));
                imgBuffer.position(0);
                glfwImages.pixels(imgBuffer);
                glfwImages.position(0);
                GLFW.glfwSetWindowIcon(windowRef, glfwImages);
                STBImage.stbi_image_free(imgBuffer);
            } catch (IOException | NullPointerException var29) {
                System.err.println("Failed to load spongie logo");
            }
        }
    }

    private WindowUtils() {
    }

}
