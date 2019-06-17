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

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RegistryTestUtil {

    static Iterable<Object[]> generateRegistryTestObjects() {

        final ArrayList<Object[]> array = new ArrayList<>();
        for (Map.Entry<Class<? extends CatalogType>, CatalogRegistryModule<?>> entry : SpongeGameRegistry.REGISTRY_MAP.entrySet()) {
            for (CatalogType catalogType : entry.getValue().getAll()) {
                array.add(new Object[]{entry.getKey().getSimpleName(), entry.getKey(), entry.getValue(), catalogType, catalogType.getId()});
            }
        }
        return array;
    }

    static Iterable<Object[]> generateCatalogContainerTestObjects() {

        final ArrayList<Object[]> objects = new ArrayList<>();
        for (Map.Entry<Class<? extends CatalogType>, CatalogRegistryModule<?>> entry : SpongeGameRegistry.REGISTRY_MAP.entrySet()) {
            final Class<? extends CatalogType> key = entry.getKey();
            final CatalogedBy catalogedBy = key.getAnnotation(CatalogedBy.class);
            if (catalogedBy != null) {
                for (Class<?> containerClass : catalogedBy.value()) {
                    for (Field field : containerClass.getFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            objects.add(new Object[] {field.getName(), entry.getKey(), containerClass, entry.getValue(), field});
                        }
                    }
                }
            }
        }

        return objects;
    }

    static Iterable<Object[]> generateCatalogTypeMethodTestObjects() {

        final ArrayList<Object[]> array = new ArrayList<>();
        for (Map.Entry<Class<? extends CatalogType>, CatalogRegistryModule<?>> entry : SpongeGameRegistry.REGISTRY_MAP.entrySet()) {
            for (CatalogType catalogType : entry.getValue().getAll()) {
                for (Method method : getTestableApiMethods(getApplicableApiCatalogTypeInterfaces(catalogType))) {
                    array.add(new Object[] {entry.getKey().getSimpleName(), entry.getKey(), catalogType, catalogType.getId(), method,
                            method.getDeclaringClass().getSimpleName() + "#" + method.getName() + "()", catalogType.getClass().getName()});
                }
            }
        }
        return array;
    }

    static Stream<Class<?>> getApplicableApiInterfaces(Object instance) {
        Collection<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = instance.getClass();
        while (current != null) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return interfaces.stream()
                .filter(clazz -> clazz.getName().startsWith("org.spongepowered.api."));
    }

    static Stream<Class<?>> getApplicableApiCatalogTypeInterfaces(Object instance) {
        return getApplicableApiInterfaces(instance)
                .filter(CatalogType.class::isAssignableFrom)
                .map(clazz -> clazz.<CatalogType>asSubclass(CatalogType.class));
    }

    static Set<Method> getTestableApiMethods(Stream<Class<?>> clazzes) {
        return clazzes.map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(m -> m.getParameters().length == 0)
                .collect(Collectors.toSet());
    }

}
