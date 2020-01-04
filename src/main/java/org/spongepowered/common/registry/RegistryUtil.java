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
package org.spongepowered.common.registry;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public final class RegistryUtil {

    public static <T extends CatalogType, E> void generateRegistry(SpongeCatalogRegistry registry, CatalogKey key, Class<T> catalogClass, Stream<E> valueStream, boolean generateSuppliers) {
        registry.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), generateSuppliers);
    }

    public static <T extends CatalogType, E> void generateSuppliers(SpongeCatalogRegistry registry, Class<T> catalogClass, Function<E, String> suggestedNameSupplier, Stream<E> valueStream) {
        valueStream.forEach(value -> registry.registerSupplier(catalogClass, suggestedNameSupplier.apply(value), () -> (T) value));
    }
}
