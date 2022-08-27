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
package org.spongepowered.common.util;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.common.event.gen.DefineableClassLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReferencedDefinableClassLoader extends DefineableClassLoader {
    private final Map<String, byte[]> definedClasses;
    public ReferencedDefinableClassLoader(final ClassLoader parent) {
        super(parent);
        this.definedClasses = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Class<T> defineClass(final String name, final byte[] b) {
        this.definedClasses.put(name, b);
        return super.defineClass(name, b);
    }


    @Nullable
    @Override
    public InputStream getResourceAsStream(final String name) {
        final String normalized = name.replace("/", ".").replace(".class", "");
        if (this.definedClasses.containsKey(normalized)) {
            final byte[] buf = this.definedClasses.get(normalized);
            final byte[] cloned = buf.clone();
            return new ByteArrayInputStream(cloned);
        }
        return super.getResourceAsStream(name);
    }
}
