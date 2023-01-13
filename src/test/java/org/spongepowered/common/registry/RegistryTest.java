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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.common.launch.SpongeExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(SpongeExtension.class)
public class RegistryTest {

    private static Stream<Registry<?>> streamRegistries(final ResourceKey root) {
        return Stream.concat(Sponge.game().streamRegistries(root), Sponge.server().streamRegistries(root)).distinct();
    }

    private static Stream<Registry<?>> streamRegistries() {
        return Stream.concat(RegistryTest.streamRegistries(RegistryRoots.MINECRAFT), RegistryTest.streamRegistries(RegistryRoots.SPONGE));
    }

    @TestFactory
    public Stream<DynamicTest> generateRegistryFindTests() {
        return RegistryTest.streamRegistries().flatMap((Registry<?> registry) -> registry.streamEntries().map((RegistryEntry<?> entry) -> {
            final String name = "Find key " + entry.key() + " in registry " + registry.type().location() + " in " + registry.type().root();
            return dynamicTest(name, () -> {
                final Optional<? extends RegistryEntry<?>> entryByKey = registry.findEntry(entry.key());
                assertTrue(entryByKey.isPresent());
                assertEquals(entryByKey.get().value(), registry.value(entry.key())); // same key -> same value
            });
        }));
    }

    private static boolean isPublicStatic(Field field) {
        return Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers());
    }

    private static Stream<Field> streamDefaultedReferenceFields() {
        return Arrays.stream(RegistryTypes.class.getDeclaredFields())
                .filter(RegistryTest::isPublicStatic)
                .flatMap(registryField -> {
                    final Class<?> valueType = TypeToken.of(((ParameterizedType) registryField.getGenericType()).getActualTypeArguments()[0]).getRawType();

                    final CatalogedBy catalogedBy = valueType.getAnnotation(CatalogedBy.class);
                    if (catalogedBy == null) {
                        return Stream.empty();
                    }

                    return Arrays.stream(catalogedBy.value());
                })
                .flatMap(container -> Arrays.stream(container.getDeclaredFields()))
                .filter(field -> isPublicStatic(field) && DefaultedRegistryReference.class.isAssignableFrom(field.getType()));
    }

    @Disabled
    @TestFactory
    public Stream<DynamicTest> generateDefaultedReferenceTests() {
        return RegistryTest.streamDefaultedReferenceFields().map(field -> {
            final String name = "Field " + field.getDeclaringClass().getSimpleName() + "#" + field.getName();
            return dynamicTest(name, () -> {
                final DefaultedRegistryReference<?> ref = (DefaultedRegistryReference<?>) field.get(null);
                assertNotNull(ref.get());
            });
        });
    }

    private static Stream<Class<?>> streamApiInterfaces(final Object instance) {
        final Collection<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = instance.getClass();
        while (current != null) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return interfaces.stream()
                .filter(clazz -> clazz.getName().startsWith("org.spongepowered.api."));
    }

    @Disabled
    @TestFactory
    public Stream<DynamicTest> generateMethodTests() {
        return RegistryTest.streamRegistries().flatMap(Registry::streamEntries)
                .flatMap((RegistryEntry<?> entry) -> {
                    final Object value = entry.value();
                    return streamApiInterfaces(value).flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                            .filter(m -> !Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0)
                            .map(m -> {
                                final String name = "Invoke " + m.getDeclaringClass().getSimpleName() + "#" + m.getName() + " on " + entry.key();
                                return dynamicTest(name, () -> {
                                    final Object result = m.invoke(value);
                                    assertNotNull(result);
                                });
                            });
                });
    }
}
