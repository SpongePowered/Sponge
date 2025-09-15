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
package org.spongepowered.common.applaunch.test;

import java.lang.reflect.Field;

/**
 * Caution: this class exists in multiple classloaders
 */
@SuppressWarnings("unused")
public class TestGameAccess {
    private static Runnable shutdownGame;

    public static ClassLoader getGameClassLoader() {
        return TestGameAccess.shutdownGame == null ? null : TestGameAccess.shutdownGame.getClass().getClassLoader();
    }

    public static void shutdownGame() {
        try {
            if (TestGameAccess.shutdownGame != null) {
                TestGameAccess.shutdownGame.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void set(final String name, final Object value) {
        final Class<?> cl;
        try {
            cl = ClassLoader.getSystemClassLoader().loadClass(TestGameAccess.class.getName());
        } catch (final ClassNotFoundException e) {
            return;
        }
        try {
            final Field field = cl.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void setup(final Runnable shutdownGame) {
        TestGameAccess.set("shutdownGame", shutdownGame);
    }
}
