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
package org.spongepowered.bootstrap.forge.classloader;

import java.lang.module.ModuleReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class FilteringPassthroughClassLoader extends ClassLoader {

    private final Set<String> filteredPackages = new HashSet<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public FilteringPassthroughClassLoader(final ClassLoader parent, final Collection<ModuleReference> modules) {
        super(parent);
        modules.forEach(m -> this.filteredPackages.addAll(m.descriptor().packages()));
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (!this.filteredPackages.contains(FilteringPassthroughClassLoader.nameToPackage(name))) {
            return super.loadClass(name, resolve);
        }
        throw new ClassNotFoundException(name);
    }

    private static String nameToPackage(final String name) {
        final int index = name.lastIndexOf('.');
        if (index == -1 || index == name.length() - 1) {
            return "";
        }
        return name.substring(0, index);
    }
}
