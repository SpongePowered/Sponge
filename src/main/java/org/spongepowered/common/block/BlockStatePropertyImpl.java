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
package org.spongepowered.common.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.state.BooleanStateProperty;
import org.spongepowered.api.state.EnumStateProperty;
import org.spongepowered.api.state.IntegerStateProperty;
import org.spongepowered.common.SpongeCommon;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class BlockStatePropertyImpl {

    @Nullable private static Map<String, Property<?>> PROPERTIES;

    private static Map<Property<?>, String> vanillaProperties() {
        final Map<Property<?>, String> vanillaMap = new IdentityHashMap<>();
        for (Field field : BlockStateProperties.class.getDeclaredFields()) {
            try {
                final Object property = field.get(null);
                if (property instanceof Property<?>) {
                    vanillaMap.put(((Property<?>) property), field.getName());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return vanillaMap;
    }

    private static Map<String, Property<?>> properties() {
        if (BlockStatePropertyImpl.PROPERTIES != null) {
            return BlockStatePropertyImpl.PROPERTIES;
        }
        // get properties used in block states
        final Map<Property<?>, String> vanillaMap = BlockStatePropertyImpl.vanillaProperties();
        final Map<String, Property<?>> propertyUsages = new HashMap<>();
        final Map<String, Integer> propertyCount = new HashMap<>();


        for (final Block block : SpongeCommon.vanillaRegistry(Registries.BLOCK)) {
            for (final Property<?> property : block.defaultBlockState().getProperties()) {
                String name = vanillaMap.computeIfAbsent(property, p -> {
                    // attempt to handle non-vanilla properties:
                    int cnt = propertyCount.computeIfAbsent(property.getName(), n -> 0) + 1;
                    return property.getName() + "_" + cnt;
                });
                propertyUsages.put(name, property);
            }
        }
        BlockStatePropertyImpl.PROPERTIES = propertyUsages;
        return propertyUsages;
    }

    public static class BooleanFactoryImpl implements BooleanStateProperty.Factory {

        @Override
        public BooleanStateProperty of(String name) {
            return (BooleanStateProperty) BlockStatePropertyImpl.properties().get(name);
        }
    }

    public static class IntegerFactoryImpl implements IntegerStateProperty.Factory {

        @Override
        public IntegerStateProperty of(String name) {
            return (IntegerStateProperty) BlockStatePropertyImpl.properties().get(name);
        }
    }


    public static class EnumFactoryImpl implements EnumStateProperty.Factory {

        @SuppressWarnings("unchecked")
        @Override
        public EnumStateProperty<?> of(String name) {
            return (EnumStateProperty<?>) BlockStatePropertyImpl.properties().get(name);
        }
    }

    private BlockStatePropertyImpl() {
    }
}
