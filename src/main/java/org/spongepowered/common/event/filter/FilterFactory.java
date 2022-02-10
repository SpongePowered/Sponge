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
package org.spongepowered.common.event.filter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.event.manager.ListenerClassVisitor;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@FunctionalInterface
public interface FilterFactory {

    /**
     * Create a new filter based on the method.
     *
     * <p>The provided {@code lookup } must have
     * {@link MethodHandles.Lookup#hasFullPrivilegeAccess() full privilege access}
     * to the {@link Method#getDeclaringClass() declaring class} of {@code method}.</p>
     *
     * @param method the method to create a filter for
     * @param lookup the lookup to generate the filter class. Must have
     * @return a new filter instance, if any filtering is necessary
     * @throws IllegalAccessException if the provided {@code lookup} does not have full privilege access
     */
    @Nullable EventFilter create(final ListenerClassVisitor.DiscoveredMethod method, final MethodHandles.Lookup lookup) throws IllegalAccessException, ClassNotFoundException;
}
