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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class FilterFactory {

    private final AtomicInteger id = new AtomicInteger();
    private final MethodHandles.Lookup lookup;
    private final LoadingCache<Method, MethodHandles.Lookup> cache = Caffeine.newBuilder()
            .weakValues().build(this::createClass);

    public FilterFactory(final MethodHandles.Lookup lookup) {
        this.lookup = checkNotNull(lookup, "lookup");
    }

    public Class<? extends EventFilter> createFilter(final Method method) {
        final MethodHandles.Lookup lookup = this.cache.get(method);
        return  lookup == null ? null : lookup.lookupClass().asSubclass(EventFilter.class);
    }

    MethodHandles.Lookup createClass(final Method method) throws IllegalAccessException {
        final Class<?> handle = method.getDeclaringClass();
        final String name = "Filter_" + method.getName() + this.id.incrementAndGet();
        final byte[] cls = FilterGenerator.getInstance().generateClass(handle, name, method);
        if (cls == null) {
            return null;
        }
        return MethodHandles.privateLookupIn(handle, this.lookup)
            .defineHiddenClass(cls, true, MethodHandles.Lookup.ClassOption.NESTMATE);
    }

}
